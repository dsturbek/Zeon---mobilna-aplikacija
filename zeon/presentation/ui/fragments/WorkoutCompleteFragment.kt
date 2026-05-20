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
import com.example.zeon.data.model.WorkoutLog
import com.example.zeon.data.repository.ExerciseRepository
import com.example.zeon.data.repository.WorkoutRepository
import com.example.zeon.data.repository.WorkoutSessionRepository
import com.example.zeon.presentation.ui.adapters.CompletedExerciseAdapter
import com.example.zeon.presentation.ui.adapters.CompletedExerciseItem
import com.google.android.material.button.MaterialButton

class WorkoutCompleteFragment : Fragment() {

    private lateinit var workoutLog: WorkoutLog
    private var workoutLogId: String? = null

    private val sessionRepository = WorkoutSessionRepository.getInstance()
    private val workoutRepository = WorkoutRepository.getInstance()
    private val exerciseRepository = ExerciseRepository.getInstance()

    private lateinit var durationValue: TextView
    private lateinit var caloriesValue: TextView
    private lateinit var exercisesValue: TextView
    private lateinit var completedExercisesRecyclerView: RecyclerView
    private lateinit var doneButton: MaterialButton

    private lateinit var completedExerciseAdapter: CompletedExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workoutLogId = arguments?.getString(ARG_WORKOUT_LOG_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadWorkoutLog()
        setupRecyclerView()
        setupClickListeners()
        displayStats()
    }

    private fun initViews(view: View) {
        durationValue = view.findViewById(R.id.durationValue)
        caloriesValue = view.findViewById(R.id.caloriesValue)
        exercisesValue = view.findViewById(R.id.exercisesValue)
        completedExercisesRecyclerView = view.findViewById(R.id.completedExercisesRecyclerView)
        doneButton = view.findViewById(R.id.doneButton)
    }

    private fun loadWorkoutLog() {
        workoutLogId?.let { id ->
            val logs = workoutRepository.getWorkoutLogs()
            workoutLog = logs.find { it.id == id } ?: return
        }
    }

    private fun displayStats() {
        val durationSeconds = workoutLog.durationSeconds
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        durationValue.text = String.format("%d:%02d", minutes, seconds)

        val calories = calculateCalories(durationSeconds)
        caloriesValue.text = calories.toString()

        val uniqueExercises = workoutLog.exerciseLogs
            .map { it.exerciseId }
            .distinct()
            .size
        exercisesValue.text = uniqueExercises.toString()
    }

    private fun calculateCalories(durationSeconds: Long): Int {
        val minutes = durationSeconds / 60
        return (minutes * 8).toInt()
    }

    private fun setupRecyclerView() {
        completedExerciseAdapter = CompletedExerciseAdapter()
        completedExercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        completedExercisesRecyclerView.adapter = completedExerciseAdapter

        val exerciseLogGroups = workoutLog.exerciseLogs.groupBy { it.exerciseId }

        val completedItems = mutableListOf<CompletedExerciseItem>()

        exerciseLogGroups.forEach { (exerciseId, logs) ->
            val exercise = exerciseRepository.getExerciseById(exerciseId) ?: return@forEach

            val maxWeight = logs.maxOfOrNull { it.weight } ?: 0.0
            val previousBest = sessionRepository.getPersonalBest(exerciseId) ?: 0.0
            val isPR = maxWeight > previousBest

            val workoutExercise = com.example.zeon.data.model.WorkoutExercise(
                id = "temp_${exerciseId}",
                exerciseId = exerciseId,
                exercise = exercise,
                plannedSets = logs.size,
                plannedReps = logs.firstOrNull()?.completedReps ?: 0
            )

            completedItems.add(
                CompletedExerciseItem(
                    workoutExercise = workoutExercise,
                    logs = logs,
                    isPR = isPR
                )
            )
        }

        completedExerciseAdapter.submitList(completedItems)
    }

    private fun setupClickListeners() {
        doneButton.setOnClickListener {
            (requireActivity() as? HomeActivity)?.returnToViewPager()
        }
    }

    companion object {
        private const val ARG_WORKOUT_LOG_ID = "workout_log_id"

        fun newInstance(workoutLogId: String): WorkoutCompleteFragment {
            val fragment = WorkoutCompleteFragment()
            val args = Bundle()
            args.putString(ARG_WORKOUT_LOG_ID, workoutLogId)
            fragment.arguments = args
            return fragment
        }
    }
}
