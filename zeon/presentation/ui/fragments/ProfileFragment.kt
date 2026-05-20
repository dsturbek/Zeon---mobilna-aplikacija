package com.example.zeon.presentation.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.zeon.HomeActivity
import com.example.zeon.MainActivity
import com.example.zeon.R
import com.example.zeon.helpers.UserSession
import com.example.zeon.data.model.User
import com.example.zeon.data.repository.GoalRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileFragment : Fragment() {

    private lateinit var btnSettings: ImageView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var btnEditProfile: MaterialButton

    private lateinit var tvWeightValue: TextView
    private lateinit var tvHeightValue: TextView
    private lateinit var tvAgeValue: TextView
    private lateinit var tvDailyCalories: TextView
    private lateinit var tvDailyWater: TextView
    private lateinit var tvProtein: TextView

    private lateinit var cvMyStatistics: MaterialCardView
    private lateinit var cvUpdateGoal: MaterialCardView
    private lateinit var cvMyTrainer: MaterialCardView
    private lateinit var cvNotifications: MaterialCardView
    private lateinit var btnLogout: MaterialButton

    private var currentUser: User? = null
    private val goalsRepository = GoalRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadCurrentUser()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnSettings = view.findViewById(R.id.btn_settings)
        profileImage = view.findViewById(R.id.profile_image)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvMemberSince = view.findViewById(R.id.tv_member_since)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)

        tvWeightValue = view.findViewById(R.id.tv_weight_value)
        tvHeightValue = view.findViewById(R.id.tv_height_value)
        tvAgeValue = view.findViewById(R.id.tv_age_value)

        tvDailyCalories = view.findViewById(R.id.tv_daily_calories)
        tvDailyWater = view.findViewById(R.id.tv_daily_water)

        cvMyStatistics = view.findViewById(R.id.cv_my_statistics)
        cvMyTrainer = view.findViewById(R.id.cv_my_trainer)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun loadCurrentUser() {
        currentUser = UserSession.getCurrentUser(requireContext())

        if (currentUser != null) {
            loadUserData(currentUser!!)
            loadGoalsFromServer(currentUser!!.id)
        } else {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    fun refreshUserData() {
        loadCurrentUser()
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        btnEditProfile.setOnClickListener {
            openEditProfile()
        }

        cvMyStatistics.setOnClickListener {
            openStatistics()
        }

        cvMyTrainer.setOnClickListener {
            openTrainerSection()
        }

        btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun loadUserData(user: User) {
        tvUserName.text = user.username
        tvMemberSince.text = "Member since January 2024"

        tvWeightValue.text = "${user.weight.toInt()}kg"
        tvHeightValue.text = "${user.height.toInt()}cm"

        val age = calculateAge(user.birthDate)
        tvAgeValue.text = age.toString()

    }

    private fun loadGoalsFromServer(clientId: Int) {

        lifecycleScope.launch {
            val result = goalsRepository.getGoalsForClient(clientId)

            result.onSuccess { goals ->

                if (goals.isNotEmpty()) {
                    val currentGoal = goals.last()

                    updateGoalsUI(currentGoal)
                } else {
                    currentUser?.let { user ->
                        val dailyCalories = calculateDailyCalories(user)
                        val dailyWater = calculateDailyWater(user)

                        tvDailyCalories.text = formatNumber(dailyCalories)
                        tvDailyWater.text = "$dailyWater L"
                    }
                }
            }.onFailure { exception ->

                Toast.makeText(
                    requireContext(),
                    "Could not load goals",
                    Toast.LENGTH_SHORT
                ).show()

                currentUser?.let { user ->
                    val dailyCalories = calculateDailyCalories(user)
                    val dailyWater = calculateDailyWater(user)

                    tvDailyCalories.text = formatNumber(dailyCalories)
                    tvDailyWater.text = "$dailyWater L"
                }
            }
        }
    }

    private fun updateGoalsUI(goal: com.example.zeon.data.model.Goals) {
        if (goal.goalCalories != null && goal.goalCalories > 0) {
            tvDailyCalories.text = formatNumber(goal.goalCalories)
        } else {
            currentUser?.let { user ->
                val dailyCalories = calculateDailyCalories(user)
                tvDailyCalories.text = formatNumber(dailyCalories)
            }
        }

        if (goal.goalWater != null && goal.goalWater > 0) {
            val waterInLiters = goal.goalWater / 1000.0 // Convert ml to L
            tvDailyWater.text = String.format("%.1f L", waterInLiters)
        } else {
            currentUser?.let { user ->
                val dailyWater = calculateDailyWater(user)
                tvDailyWater.text = "$dailyWater L"
            }
        }
    }

    private fun calculateAge(birthDate: java.util.Date): Int {
        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = birthDate

        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

        if (today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
            age--
        }

        return age
    }

    private fun calculateDailyCalories(user: User): Int {
        return (user.weight * 40).toInt()
    }

    private fun calculateDailyWater(user: User): String {
        val liters = (user.weight * 0.033).toDouble()
        return String.format("%.1f", liters)
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    private fun openEditProfile() {
        val editProfileFragment = EditProfileFragment()
        (requireActivity() as HomeActivity).openFragment(editProfileFragment)
    }

    private fun openStatistics() {
        val statisticsFragment = StatisticsFragment.newInstance()
        (requireActivity() as HomeActivity).openFragment(statisticsFragment)
    }

    private fun openTrainerSection() {
        val trainerId = UserSession.getTrainerId(requireContext())
        if (trainerId != null) {
            val trainerProfile = TrainerProfileFragment.newInstance(trainerId)
            (requireActivity() as HomeActivity).openFragment(trainerProfile)
        } else {
            val trainerSearch = TrainerSearchFragment()
            (requireActivity() as HomeActivity).openFragment(trainerSearch)
        }
    }

    private fun handleLogout() {
        UserSession.clearSession(requireContext())
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}