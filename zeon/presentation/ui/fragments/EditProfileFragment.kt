package com.example.zeon.presentation.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.User
import com.example.zeon.data.repository.UserRepository
import com.example.zeon.helpers.UserSession
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditProfileFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var btnChangePhoto: FloatingActionButton
    private lateinit var etUsername: TextInputEditText
    private lateinit var cvBirthDate: MaterialCardView
    private lateinit var tvBirthDate: TextView
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null
    private var selectedBirthDate: Date? = null
    private val userRepository = UserRepository()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadCurrentUser()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        profileImage = view.findViewById(R.id.profile_image)
        btnChangePhoto = view.findViewById(R.id.btn_change_photo)
        etUsername = view.findViewById(R.id.et_username)
        cvBirthDate = view.findViewById(R.id.cv_birth_date)
        tvBirthDate = view.findViewById(R.id.tv_birth_date)
        etHeight = view.findViewById(R.id.et_height)
        etWeight = view.findViewById(R.id.et_weight)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnSave = view.findViewById(R.id.btn_save)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun loadCurrentUser() {
        currentUser = UserSession.getCurrentUser(requireContext())

        if (currentUser != null) {
            populateFields(currentUser!!)
        } else {
            Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
            goBack()
        }
    }

    private fun populateFields(user: User) {
        etUsername.setText(user.username)

        selectedBirthDate = user.birthDate
        tvBirthDate.text = dateFormat.format(user.birthDate)
        tvBirthDate.setTextColor(resources.getColor(R.color.bijela, null))

        if (user.height > 0) {
            etHeight.setText(user.height.toInt().toString())
        }

        if (user.weight > 0) {
            etWeight.setText(user.weight.toInt().toString())
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            goBack()
        }

        btnChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Photo picker coming soon", Toast.LENGTH_SHORT).show()
        }

        cvBirthDate.setOnClickListener {
            showDatePicker()
        }

        btnCancel.setOnClickListener {
            goBack()
        }

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        if (selectedBirthDate != null) {
            calendar.time = selectedBirthDate!!
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedBirthDate = newCalendar.time

                tvBirthDate.text = dateFormat.format(selectedBirthDate!!)
                tvBirthDate.setTextColor(resources.getColor(R.color.bijela, null))
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun saveChanges() {
        val username = etUsername.text.toString().trim()
        val heightStr = etHeight.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        if (heightStr.isEmpty()) {
            etHeight.error = "Height is required"
            etHeight.requestFocus()
            return
        }

        if (weightStr.isEmpty()) {
            etWeight.error = "Weight is required"
            etWeight.requestFocus()
            return
        }

        val height = heightStr.toFloatOrNull()
        val weight = weightStr.toFloatOrNull()

        if (height == null || height <= 0) {
            etHeight.error = "Invalid height"
            etHeight.requestFocus()
            return
        }

        if (weight == null || weight <= 0) {
            etWeight.error = "Invalid weight"
            etWeight.requestFocus()
            return
        }

        if (selectedBirthDate == null) {
            Toast.makeText(requireContext(), "Please select birth date", Toast.LENGTH_SHORT).show()
            return
        }

        performUpdate(username, height, weight, selectedBirthDate!!)
    }

    private fun performUpdate(username: String, height: Float, weight: Float, birthDate: Date) {
        showLoading(true)

        val userId = UserSession.getUserId(requireContext())
        val currentUser = UserSession.getCurrentUser(requireContext())

        val nameSurname = currentUser?.username ?: username
        val email = currentUser?.email ?: ""

        lifecycleScope.launch {
            val result = userRepository.updateUser(
                userId = userId,
                username = username,
                email = email,
                nameSurname = nameSurname,
                height = height,
                weight = weight,
                birthDate = birthDate
            )

            showLoading(false)

            result.onSuccess { updatedUser ->
                UserSession.saveUser(requireContext(), updatedUser)

                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                notifyProfileUpdated()
                goBack()
            }.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    exception.message ?: "Update failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun notifyProfileUpdated() {
        val profileFragment = parentFragmentManager.fragments.find { it is ProfileFragment } as? ProfileFragment
        profileFragment?.refreshUserData()
    }

    private fun goBack() {
        (requireActivity() as HomeActivity).returnToViewPager()
    }

    companion object {
        fun newInstance(): EditProfileFragment {
            return EditProfileFragment()
        }
    }
}