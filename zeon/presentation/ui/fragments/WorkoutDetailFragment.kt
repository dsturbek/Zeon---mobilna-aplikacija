package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.Workout
import com.example.zeon.data.repository.WorkoutRepository
import com.example.zeon.data.repository.WorkoutSessionRepository
import com.example.zeon.presentation.ui.adapters.WorkoutExerciseAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.time.format.DateTimeFormatter

class WorkoutDetailFragment : Fragment() {

    private lateinit var workout: Workout
    private var workoutId: String? = null

    private val workoutRepository = WorkoutRepository.getInstance()
    private val sessionRepository = WorkoutSessionRepository.getInstance()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var workoutTitle: TextView
    private lateinit var workoutTime: TextView
    private lateinit var workoutDuration: TextView
    private lateinit var workoutExerciseCount: TextView
    private lateinit var exercisesHeader: TextView
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var startWorkoutButton: MaterialButton

    private lateinit var exerciseAdapter: WorkoutExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workoutId = arguments?.getString(ARG_WORKOUT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadWorkout()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        workoutTitle = view.findViewById(R.id.workoutTitle)
        workoutTime = view.findViewById(R.id.workoutTime)
        workoutDuration = view.findViewById(R.id.workoutDuration)
        workoutExerciseCount = view.findViewById(R.id.workoutExerciseCount)
        exercisesHeader = view.findViewById(R.id.exercisesHeader)
        exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView)
        startWorkoutButton = view.findViewById(R.id.startWorkoutButton)
    }

    private fun loadWorkout() {
        workoutId?.let { id ->
            workout = workoutRepository.getWorkoutById(id) ?: return
            displayWorkout()
        }
    }

    private fun displayWorkout() {
        workoutTitle.text = workout.title
        workoutTime.text = "| ${workout.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        workoutDuration.text = "| 45 min"
        workoutExerciseCount.text = "| ${workout.numberOfExercises} vježbi"

        exercisesHeader.text = "VJEŽBE (0/${workout.numberOfExercises} ZAVRŠENO)"
    }

    private fun setupRecyclerView() {
        exerciseAdapter = WorkoutExerciseAdapter { workoutExercise ->
            val fragment = ExerciseDetailFragment.newInstance(workoutExercise.exerciseId)
            (requireActivity() as? HomeActivity)?.openFragment(fragment)
        }

        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exercisesRecyclerView.adapter = exerciseAdapter

        if (::workout.isInitialized) {
            exerciseAdapter.submitList(workout.exercises)
        }
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            (requireActivity() as? HomeActivity)?.returnToViewPager()
        }

        if (::workout.isInitialized && workout.date != java.time.LocalDate.now()) {
            startWorkoutButton.isEnabled = false
            startWorkoutButton.alpha = 0.5f
        } else {
            startWorkoutButton.setOnClickListener {
                startWorkout()
            }
        }
    }

    private fun startWorkout() {
        val session = sessionRepository.startWorkout(workout)

        val fragment = WorkoutInProgressFragment.newInstance()
        (requireActivity() as? HomeActivity)?.openFragment(fragment)
    }

    companion object {
        private const val ARG_WORKOUT_ID = "workout_id"

        fun newInstance(workoutId: String): WorkoutDetailFragment {
            val fragment = WorkoutDetailFragment()
            val args = Bundle()
            args.putString(ARG_WORKOUT_ID, workoutId)
            fragment.arguments = args
            return fragment
        }
    }
}
