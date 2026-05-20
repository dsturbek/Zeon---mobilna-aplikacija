package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.Exercise
import com.example.zeon.data.model.ExerciseLog
import com.example.zeon.data.repository.ExerciseRepository
import com.example.zeon.data.repository.WorkoutSessionRepository
import com.example.zeon.helpers.UserSession
import com.example.zeon.presentation.ui.adapters.ExerciseSet
import com.example.zeon.presentation.ui.adapters.ExerciseSetAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.util.UUID

class ExerciseDetailFragment : Fragment() {

    private lateinit var exercise: Exercise
    private var exerciseId: String? = null

    private val exerciseRepository = ExerciseRepository.getInstance()
    private val sessionRepository = WorkoutSessionRepository.getInstance()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var exerciseName: TextView
    private lateinit var exerciseMuscleGroup: TextView
    private lateinit var videoWebView: WebView
    private lateinit var playButton: ImageView
    private lateinit var trainerNoteCard: CardView
    private lateinit var trainerNote: TextView
    private lateinit var lastWeight: TextView
    private lateinit var averageReps: TextView
    private lateinit var personalBest: TextView
    private lateinit var setsRecyclerView: RecyclerView
    private lateinit var addSetButton: MaterialButton
    private lateinit var difficultyLabel: TextView
    private lateinit var difficultyHard: LinearLayout
    private lateinit var difficultyPerfect: LinearLayout
    private lateinit var difficultyEasy: LinearLayout
    private lateinit var notesInput: TextInputEditText
    private lateinit var completeExerciseButton: MaterialButton

    private lateinit var setsAdapter: ExerciseSetAdapter
    private val sets = mutableListOf<ExerciseSet>()
    private var selectedDifficulty: String = "PERFECT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseId = arguments?.getString(ARG_EXERCISE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadExercise()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateUIBasedOnWorkoutState()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        exerciseName = view.findViewById(R.id.exerciseName)
        exerciseMuscleGroup = view.findViewById(R.id.exerciseMuscleGroup)
        videoWebView = view.findViewById(R.id.videoWebView)
        playButton = view.findViewById(R.id.playButton)
        trainerNoteCard = view.findViewById(R.id.trainerNoteCard)
        trainerNote = view.findViewById(R.id.trainerNote)
        lastWeight = view.findViewById(R.id.lastWeight)
        averageReps = view.findViewById(R.id.averageReps)
        personalBest = view.findViewById(R.id.personalBest)
        setsRecyclerView = view.findViewById(R.id.setsRecyclerView)
        addSetButton = view.findViewById(R.id.addSetButton)
        difficultyLabel = view.findViewById(R.id.difficultyLabel)
        difficultyHard = view.findViewById(R.id.difficultyHard)
        difficultyPerfect = view.findViewById(R.id.difficultyPerfect)
        difficultyEasy = view.findViewById(R.id.difficultyEasy)
        notesInput = view.findViewById(R.id.notesInput)
        completeExerciseButton = view.findViewById(R.id.completeExerciseButton)
    }

    private fun loadExercise() {
        exerciseId?.let { id ->
            val cached = exerciseRepository.getExerciseById(id)
            if (cached != null) {
                exercise = cached
                onExerciseLoaded()
            } else {
                exerciseRepository.getExerciseById(id) { fetchedExercise ->
                    if (!isAdded) return@getExerciseById
                    if (fetchedExercise != null) {
                        exercise = fetchedExercise
                        onExerciseLoaded()
                    }
                }
            }
        }
    }

    private fun onExerciseLoaded() {
        displayExercise()
        setupRecyclerView()
        setupVideo()
        loadExerciseStats()
        updateUIBasedOnWorkoutState()
    }

    private fun displayExercise() {
        exerciseName.text = exercise.name
        exerciseMuscleGroup.text = exercise.muscleGroup

        if (exercise.description.isNotEmpty()) {
            trainerNote.text = exercise.description
            trainerNoteCard.visibility = View.VISIBLE
        } else {
            trainerNoteCard.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val currentSession = sessionRepository.getCurrentSession()
        val existingLogs = currentSession?.getExerciseLogsForExercise(exercise.id) ?: emptyList()
        val workoutExercise = currentSession?.workout?.exercises
            ?.find { it.exerciseId == exercise.id }

        if (existingLogs.isNotEmpty()) {
            existingLogs.forEachIndexed { index, log ->
                sets.add(ExerciseSet(
                    setNumber = index + 1,
                    weight = log.weight,
                    reps = log.completedReps,
                    isCompleted = true
                ))
            }
        } else {
            val plannedSets = workoutExercise?.plannedSets ?: 3
            val plannedReps = workoutExercise?.plannedReps
            val plannedWeight = workoutExercise?.plannedWeight?.takeIf { it > 0 }

            for (i in 1..plannedSets) {
                sets.add(ExerciseSet(
                    setNumber = i,
                    hintWeight = plannedWeight,
                    hintReps = plannedReps
                ))
            }
        }

        setsAdapter = ExerciseSetAdapter(sets) { position, set ->
        }

        setsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        setsRecyclerView.adapter = setsAdapter
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        addSetButton.setOnClickListener {
            setsAdapter.addSet()
        }

        difficultyHard.setOnClickListener {
            selectDifficulty("HARD")
        }

        difficultyPerfect.setOnClickListener {
            selectDifficulty("PERFECT")
        }

        difficultyEasy.setOnClickListener {
            selectDifficulty("EASY")
        }

        completeExerciseButton.setOnClickListener {
            completeExercise()
        }

        playButton.setOnClickListener {
            playButton.visibility = View.GONE
            videoWebView.loadUrl(getYouTubeEmbedUrl(exercise.videoUrl))
        }
    }

    private fun setupVideo() {
        videoWebView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(false)
        }
        videoWebView.webViewClient = WebViewClient()

        playButton.visibility = View.VISIBLE
    }

    private fun getYouTubeEmbedUrl(url: String): String {
        val videoId = if (url.contains("youtube.com/watch?v=")) {
            url.substringAfter("watch?v=").substringBefore("&")
        } else if (url.contains("youtu.be/")) {
            url.substringAfter("youtu.be/").substringBefore("?")
        } else {
            return url
        }

        return "https://www.youtube.com/embed/$videoId?autoplay=1&rel=0"
    }

    private fun loadExerciseStats() {
        if (!::exercise.isInitialized) return
        val clientId = UserSession.getUserId(requireContext())
        val numericExerciseId = exercise.id.toIntOrNull() ?: return

        sessionRepository.fetchExerciseStats(clientId, numericExerciseId) { lw, ar, pb ->
            if (!isAdded) return@fetchExerciseStats
            lastWeight.text = if (lw != null) "${lw.toInt()}kg" else "-"
            averageReps.text = if (ar != null) String.format("%.1f", ar) else "-"
            personalBest.text = if (pb != null) "${pb.toInt()}kg" else "-"
        }
    }

    private fun selectDifficulty(difficulty: String) {
        selectedDifficulty = difficulty

        difficultyHard.setBackgroundResource(R.color.pozadina_card)
        difficultyPerfect.setBackgroundResource(R.color.pozadina_card)
        difficultyEasy.setBackgroundResource(R.color.pozadina_card)

        when (difficulty) {
            "HARD" -> difficultyHard.setBackgroundResource(R.color.tamna_crvena)
            "PERFECT" -> difficultyPerfect.setBackgroundResource(R.color.tamna_crvena)
            "EASY" -> difficultyEasy.setBackgroundResource(R.color.tamna_crvena)
        }
    }

    private fun updateUIBasedOnWorkoutState() {
        val hasActiveWorkout = sessionRepository.getCurrentSession() != null

        if (!hasActiveWorkout) {
            completeExerciseButton.visibility = View.GONE
            difficultyLabel.visibility = View.GONE
            difficultyHard.visibility = View.GONE
            difficultyPerfect.visibility = View.GONE
            difficultyEasy.visibility = View.GONE
            notesInput.visibility = View.GONE

            setsRecyclerView.alpha = 0.5f
            addSetButton.isEnabled = false
        } else {
            completeExerciseButton.visibility = View.VISIBLE
            difficultyLabel.visibility = View.VISIBLE
            difficultyHard.visibility = View.VISIBLE
            difficultyPerfect.visibility = View.VISIBLE
            difficultyEasy.visibility = View.VISIBLE
            notesInput.visibility = View.VISIBLE

            setsRecyclerView.alpha = 1.0f
            addSetButton.isEnabled = true
        }
    }

    private fun completeExercise() {
        val currentSession = sessionRepository.getCurrentSession()
        if (currentSession == null) {
            Toast.makeText(requireContext(), "Nema aktivnog treninga! Prvo započni trening.", Toast.LENGTH_LONG).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        val completedSets = setsAdapter.getSets().filter {
            it.weight != null && it.reps != null
        }

        if (completedSets.isEmpty()) {
            Toast.makeText(requireContext(), "Unesi težinu i broj ponavljanja za barem jedan set!", Toast.LENGTH_SHORT).show()
            return
        }

        sessionRepository.removeExerciseLogsForExercise(exercise.id)

        val totalSets = completedSets.size
        val avgReps = completedSets.map { it.reps ?: 0 }.average().toInt()
        val avgWeight = completedSets.map { it.weight ?: 0.0 }.average()

        val log = ExerciseLog(
            id = UUID.randomUUID().toString(),
            workoutId = currentSession.workoutId,
            exerciseId = exercise.id,
            completedSets = totalSets,
            completedReps = avgReps,
            weight = avgWeight,
            comment = notesInput.text.toString(),
            timestamp = LocalDateTime.now()
        )
        sessionRepository.addExerciseLog(log)

        Toast.makeText(requireContext(), "Vježba završena!", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
        private const val ARG_EXERCISE_ID = "exercise_id"

        fun newInstance(exerciseId: String): ExerciseDetailFragment {
            val fragment = ExerciseDetailFragment()
            val args = Bundle()
            args.putString(ARG_EXERCISE_ID, exerciseId)
            fragment.arguments = args
            return fragment
        }
    }
}
