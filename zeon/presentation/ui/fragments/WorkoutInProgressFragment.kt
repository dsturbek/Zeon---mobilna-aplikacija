package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.WorkoutSession
import com.example.zeon.data.repository.WorkoutSessionRepository
import com.example.zeon.presentation.ui.adapters.ExerciseProgress
import com.example.zeon.presentation.ui.adapters.WorkoutExerciseProgressAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkoutInProgressFragment : Fragment() {

    private lateinit var session: WorkoutSession
    private val sessionRepository = WorkoutSessionRepository.getInstance()

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var elapsedSeconds: Long = 0
    private var isPaused = false

    private lateinit var toolbar: MaterialToolbar
    private lateinit var workoutTitle: TextView
    private lateinit var progressText: TextView
    private lateinit var timerText: TextView
    private lateinit var pauseResumeButton: FloatingActionButton
    private lateinit var workoutProgressBar: ProgressBar
    private lateinit var exercisesHeader: TextView
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var completeWorkoutButton: MaterialButton

    private lateinit var exerciseAdapter: WorkoutExerciseProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_in_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadSession()
        setupRecyclerView()
        setupClickListeners()
        startTimer()
    }

    override fun onResume() {
        super.onResume()
        updateExerciseList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        workoutTitle = view.findViewById(R.id.workoutTitle)
        progressText = view.findViewById(R.id.progressText)
        timerText = view.findViewById(R.id.timerText)
        pauseResumeButton = view.findViewById(R.id.pauseResumeButton)
        workoutProgressBar = view.findViewById(R.id.workoutProgressBar)
        exercisesHeader = view.findViewById(R.id.exercisesHeader)
        exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView)
        completeWorkoutButton = view.findViewById(R.id.completeWorkoutButton)
    }

    private fun loadSession() {
        session = sessionRepository.getCurrentSession() ?: run {
            (requireActivity() as? HomeActivity)?.returnToViewPager()
            return
        }

        displayWorkoutInfo()
    }

    private fun displayWorkoutInfo() {
        workoutTitle.text = session.workout.title
        updateProgress()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = WorkoutExerciseProgressAdapter(
            onExerciseClick = { workoutExercise ->
                val fragment = ExerciseDetailFragment.newInstance(workoutExercise.exerciseId)
                (requireActivity() as? HomeActivity)?.openFragment(fragment)
            },
            onCompletionUpdate = {
                updateProgress()
            }
        )

        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exercisesRecyclerView.adapter = exerciseAdapter

        updateExerciseList()
    }

    private fun updateExerciseList() {
        val progressList = session.workout.exercises.map { workoutExercise ->
            val logs = session.getExerciseLogsForExercise(workoutExercise.exerciseId)
            ExerciseProgress(
                workoutExercise = workoutExercise,
                isCompleted = logs.isNotEmpty(),
                completedLogs = logs
            )
        }

        exerciseAdapter.submitList(progressList)
        updateProgress()
    }

    private fun updateProgress() {
        val completedCount = session.getCompletedExercisesCount()
        val totalCount = session.getTotalExercisesCount()

        progressText.text = "$completedCount/$totalCount završeno"
        exercisesHeader.text = "VJEŽBE ($completedCount/$totalCount ZAVRŠENO)"

        val progress = if (totalCount > 0) {
            (completedCount.toFloat() / totalCount.toFloat() * 100).toInt()
        } else {
            0
        }
        workoutProgressBar.progress = progress
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            (requireActivity() as? HomeActivity)?.returnToViewPager()
        }

        pauseResumeButton.setOnClickListener {
            togglePauseResume()
        }

        completeWorkoutButton.setOnClickListener {
            completeWorkout()
        }
    }

    private fun startTimer() {
        stopTimer()
        updateTimerFromClock()
        timerRunnable = object : Runnable {
            override fun run() {
                updateTimerFromClock()
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(timerRunnable!!, 1000)
    }

    private fun updateTimerFromClock() {
        elapsedSeconds = sessionRepository.getElapsedSeconds()
        sessionRepository.updateElapsedTime(elapsedSeconds)
        updateTimerDisplay()
    }

    private fun stopTimer() {
        timerRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun togglePauseResume() {
        isPaused = !isPaused

        if (isPaused) {
            sessionRepository.pauseWorkout()
            pauseResumeButton.setImageResource(android.R.drawable.ic_media_play)
        } else {
            sessionRepository.resumeWorkout()
            pauseResumeButton.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    private fun updateTimerDisplay() {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        timerText.text = String.format("%d:%02d", minutes, seconds)
    }

    private fun completeWorkout() {
        stopTimer()
        completeWorkoutButton.isEnabled = false

        val difficulty = com.example.zeon.data.model.DifficultyLevel.MEDIUM
        val comment = ""

        sessionRepository.completeWorkout(requireContext(), comment, difficulty) { workoutLog ->
            if (!isAdded) return@completeWorkout

            if (workoutLog != null) {
                val fragment = WorkoutCompleteFragment.newInstance(workoutLog.id)
                (requireActivity() as? HomeActivity)?.openFragment(fragment)
            } else {
                completeWorkoutButton.isEnabled = true
                Toast.makeText(requireContext(), "Greška pri završavanju treninga", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(): WorkoutInProgressFragment {
            return WorkoutInProgressFragment()
        }
    }
}
