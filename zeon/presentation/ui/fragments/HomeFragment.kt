package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.Workout
import com.example.zeon.data.repository.WorkoutRepository
import com.example.zeon.helpers.UserSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private val workoutRepository = WorkoutRepository.getInstance()
    private var todaysWorkout: Workout? = null
    private var isWorkoutCompleted: Boolean = false

    private lateinit var todayWorkoutCard: CardView
    private lateinit var todayWorkoutTitle: TextView
    private lateinit var todayWorkoutTime: TextView
    private lateinit var todayWorkoutDuration: TextView
    private lateinit var todayWorkoutExercises: TextView
    private lateinit var todayWorkoutMuscleGroup: TextView
    private lateinit var noWorkoutMessage: TextView
    private lateinit var todayBadge: TextView
    private lateinit var workoutIcon: android.widget.ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadTodaysWorkout()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadTodaysWorkout()
    }

    private fun initViews(view: View) {
        todayWorkoutCard = view.findViewById(R.id.todayWorkoutCard)
        todayWorkoutTitle = view.findViewById(R.id.todayWorkoutTitle)
        todayWorkoutTime = view.findViewById(R.id.todayWorkoutTime)
        todayWorkoutDuration = view.findViewById(R.id.todayWorkoutDuration)
        todayWorkoutExercises = view.findViewById(R.id.todayWorkoutExercises)
        todayWorkoutMuscleGroup = view.findViewById(R.id.todayWorkoutMuscleGroup)
        noWorkoutMessage = view.findViewById(R.id.noWorkoutMessage)
        todayBadge = view.findViewById(R.id.todayBadge)
        workoutIcon = view.findViewById(R.id.todayWorkoutIcon)
    }

    private fun loadTodaysWorkout() {
        val today = LocalDate.now()
        val todaysWorkouts = workoutRepository.getWorkoutsByDate(today)

        if (todaysWorkouts.isNotEmpty()) {
            showTodaysWorkout(todaysWorkouts, today)
        } else {
            val clientId = UserSession.getUserId(requireContext())
            if (clientId == -1) {
                showNoWorkout()
                return
            }
            workoutRepository.fetchActiveWorkouts(clientId) { workouts ->
                if (!isAdded) return@fetchActiveWorkouts
                val fetched = workoutRepository.getWorkoutsByDate(today)
                if (fetched.isNotEmpty()) {
                    showTodaysWorkout(fetched, today)
                } else {
                    showNoWorkout()
                }
            }
        }
    }

    private fun showTodaysWorkout(todaysWorkouts: List<Workout>, today: LocalDate) {
        todaysWorkout = todaysWorkouts[0]
        isWorkoutCompleted = workoutRepository.isWorkoutCompletedOnDate(todaysWorkout!!.id, today)
        displayWorkout(todaysWorkout!!)
        updateCompletionUI()
        todayWorkoutCard.visibility = View.VISIBLE
        noWorkoutMessage.visibility = View.GONE
    }

    private fun showNoWorkout() {
        todayWorkoutCard.visibility = View.GONE
        noWorkoutMessage.visibility = View.VISIBLE
    }

    private fun updateCompletionUI() {
        if (isWorkoutCompleted) {
            todayBadge.text = "ZAVRŠENO"
            todayBadge.setBackgroundResource(R.drawable.rounded_badge_green)
            workoutIcon.setBackgroundResource(R.drawable.rounded_icon_bg_green)
        } else {
            todayBadge.text = "DANAS"
            todayBadge.setBackgroundResource(R.drawable.rounded_badge_red)
            workoutIcon.setBackgroundResource(R.drawable.rounded_icon_bg)
        }
    }

    private fun displayWorkout(workout: Workout) {
        todayWorkoutTitle.text = workout.title
        todayWorkoutTime.text = "| ${workout.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        todayWorkoutDuration.text = "| 45 min"
        todayWorkoutExercises.text = "${workout.numberOfExercises} vježbi"
        todayWorkoutMuscleGroup.text = "${workout.muscleGroup}"
    }

    private fun setupClickListeners() {
        todayWorkoutCard.setOnClickListener {
            todaysWorkout?.let { workout ->
                val fragment = if (isWorkoutCompleted) {
                    val workoutLog = workoutRepository.getWorkoutLogForDate(workout.id, LocalDate.now())
                    workoutLog?.let {
                        com.example.zeon.presentation.ui.fragments.WorkoutCompleteFragment.newInstance(it.id)
                    } ?: com.example.zeon.presentation.ui.fragments.WorkoutDetailFragment.newInstance(workout.id)
                } else {
                    com.example.zeon.presentation.ui.fragments.WorkoutDetailFragment.newInstance(workout.id)
                }
                (requireActivity() as? HomeActivity)?.openFragment(fragment)
            }
        }
    }
}