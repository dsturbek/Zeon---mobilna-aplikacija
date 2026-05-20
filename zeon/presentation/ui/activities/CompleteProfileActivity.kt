package com.example.zeon.presentation.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.repository.UserRepository
import com.example.zeon.helpers.UserSession
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CompleteProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePlaceholder: ShapeableImageView
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var etName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var cvBirthDate: MaterialCardView
    private lateinit var tvBirthDate: TextView
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddPhoto: FloatingActionButton

    private var selectedBirthDate: Date? = null
    private val userRepository = UserRepository()
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            android.util.Log.e("IMAGE_PICKER", "Image selected: $uri")
            handleImageSelected(uri)
        } else {
            android.util.Log.e("IMAGE_PICKER", "No image selected")
        }
    }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        initViews()
        setupClickListeners()
        prefillUsername()
    }

    private fun initViews() {
        ivProfilePlaceholder = findViewById(R.id.iv_profile_placeholder)
        etName = findViewById(R.id.et_name)
        etUsername = findViewById(R.id.et_username)
        rgGender = findViewById(R.id.rg_gender)
        rbMale = findViewById(R.id.rb_male)
        rbFemale = findViewById(R.id.rb_female)
        cvBirthDate = findViewById(R.id.cv_birth_date)
        tvBirthDate = findViewById(R.id.tv_birth_date)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        fabAddPhoto = findViewById(R.id.fab_add_photo)
    }

    private fun prefillUsername() {
        val currentUser = UserSession.getCurrentUser(this)
        etUsername.setText(currentUser?.username ?: "")
    }

    private fun setupClickListeners() {
        cvBirthDate.setOnClickListener {
            showDatePicker()
        }

        fabAddPhoto.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun openImagePicker() {
        android.util.Log.e("IMAGE_PICKER", "Opening image picker...")

        // Launch Photo Picker
        pickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private fun handleImageSelected(uri: Uri) {
        try {
            ivProfilePlaceholder.setImageURI(uri)

            selectedImageUri = uri

            Toast.makeText(this, "Profile photo selected!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
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
            this,
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

    private fun saveProfile() {
        val username = etUsername.text.toString().trim()
        val heightStr = etHeight.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()
        val name = etName.text.toString().trim()

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rb_male -> "M"
            R.id.rb_female -> "F"
            else -> null
        }

        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }

        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        val height = if (heightStr.isNotEmpty()) {
            heightStr.toFloatOrNull()?.also {
                if (it <= 0) {
                    etHeight.error = "Invalid height"
                    etHeight.requestFocus()
                    return
                }
            } ?: run {
                etHeight.error = "Invalid height"
                etHeight.requestFocus()
                return
            }
        } else null

        val weight = if (weightStr.isNotEmpty()) {
            weightStr.toFloatOrNull()?.also {
                if (it <= 0) {
                    etWeight.error = "Invalid weight"
                    etWeight.requestFocus()
                    return
                }
            } ?: run {
                etWeight.error = "Invalid weight"
                etWeight.requestFocus()
                return
            }
        } else null

        if (selectedImageUri != null) {
            android.util.Log.e("COMPLETE_PROFILE", "Image selected but upload not yet implemented")
        }

        performUpdate(username, name, gender, height, weight, selectedBirthDate)
    }

    private fun performUpdate(
        username: String,
        name: String,
        gender: String?,
        height: Float?,
        weight: Float?,
        birthDate: Date?
    ) {
        showLoading(true)

        val userId = UserSession.getUserId(this)
        val currentUser = UserSession.getCurrentUser(this)

        lifecycleScope.launch {
            val result = userRepository.updateUser(
                userId = userId,
                username = username,
                email = currentUser?.email,
                nameSurname = name,
                height = height,
                weight = weight,
                birthDate = birthDate,
                gender = gender
            )

            showLoading(false)

            result.onSuccess { updatedUser ->
                UserSession.saveUser(this@CompleteProfileActivity, updatedUser)

                Toast.makeText(
                    this@CompleteProfileActivity,
                    "Profile completed!",
                    Toast.LENGTH_SHORT
                ).show()

                navigateToHome()
            }.onFailure { exception ->
                Toast.makeText(
                    this@CompleteProfileActivity,
                    exception.message ?: "Update failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSave.isEnabled = !show
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}