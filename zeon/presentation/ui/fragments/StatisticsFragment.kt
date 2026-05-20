package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.helpers.UserSession
import com.example.zeon.presentation.ui.adapters.MuscleProgressAdapter
import com.example.zeon.presentation.ui.adapters.PersonalRecordAdapter
import com.example.zeon.presentation.viewmodel.StatisticsUiState
import com.example.zeon.presentation.viewmodel.StatisticsViewModel

class StatisticsFragment : Fragment() {

    private lateinit var viewModel: StatisticsViewModel

    private lateinit var btnBack: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var loadingOverlay: FrameLayout

    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvLongestStreak: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var tvWeeklyFrequency: TextView

    private lateinit var rvMuscleProgress: RecyclerView
    private lateinit var tvMuscleProgressEmpty: TextView
    private lateinit var muscleProgressAdapter: MuscleProgressAdapter

    private lateinit var rvPersonalRecords: RecyclerView
    private lateinit var tvPersonalRecordsEmpty: TextView
    private lateinit var personalRecordAdapter: PersonalRecordAdapter

    private var clientId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        initViews(view)
        setupAdapters()
        setupClickListeners()
        observeViewModel()

        loadClientData()
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        loadingOverlay = view.findViewById(R.id.loading_overlay)

        tvCurrentStreak = view.findViewById(R.id.tv_current_streak)
        tvLongestStreak = view.findViewById(R.id.tv_longest_streak)
        tvTotalWorkouts = view.findViewById(R.id.tv_total_workouts)
        tvWeeklyFrequency = view.findViewById(R.id.tv_weekly_frequency)


        rvMuscleProgress = view.findViewById(R.id.rv_muscle_progress)
        tvMuscleProgressEmpty = view.findViewById(R.id.tv_muscle_progress_empty)

        rvPersonalRecords = view.findViewById(R.id.rv_personal_records)
        tvPersonalRecordsEmpty = view.findViewById(R.id.tv_personal_records_empty)

        swipeRefresh.setColorSchemeResources(R.color.crvena)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.pozadina_card)
    }

    private fun setupAdapters() {
        muscleProgressAdapter = MuscleProgressAdapter { muscleProgress ->
            Toast.makeText(
                requireContext(),
                "${muscleProgress.muscleGroup}: ${String.format("%.1f", muscleProgress.progressPercent)}% progress",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvMuscleProgress.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = muscleProgressAdapter
        }

        personalRecordAdapter = PersonalRecordAdapter { record ->
            Toast.makeText(
                requireContext(),
                "${record.exerciseName}: ${record.maxWeight} kg x ${record.maxReps}",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvPersonalRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = personalRecordAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            (requireActivity() as HomeActivity).returnToViewPager()
        }

        swipeRefresh.setOnRefreshListener {
            if (clientId != -1) {
                viewModel.refreshStatistics(clientId)
            } else {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.statisticsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is StatisticsUiState.Loading -> {
                    loadingOverlay.visibility = View.VISIBLE
                }
                is StatisticsUiState.Success -> {
                    loadingOverlay.visibility = View.GONE
                    updateUI(state.summary)
                }
                is StatisticsUiState.Error -> {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            swipeRefresh.isRefreshing = isRefreshing
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadClientData() {
        val user = UserSession.getCurrentUser(requireContext())
        if (user != null) {
            clientId = user.id
            viewModel.loadStatistics(clientId)
        } else {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            (requireActivity() as HomeActivity).returnToViewPager()
        }
    }

    private fun updateUI(summary: com.example.zeon.data.model.StatisticsSummary) {
        tvCurrentStreak.text = summary.currentStreak.toString()
        tvLongestStreak.text = summary.longestStreak.toString()
        tvTotalWorkouts.text = summary.totalWorkouts.toString()
        tvWeeklyFrequency.text = String.format("%.1f", summary.weeklyFrequency)

        if (summary.muscleProgress.isEmpty()) {
            rvMuscleProgress.visibility = View.GONE
            tvMuscleProgressEmpty.visibility = View.VISIBLE
        } else {
            rvMuscleProgress.visibility = View.VISIBLE
            tvMuscleProgressEmpty.visibility = View.GONE
            muscleProgressAdapter.submitList(summary.muscleProgress)
        }

        if (summary.personalRecords.isEmpty()) {
            rvPersonalRecords.visibility = View.GONE
            tvPersonalRecordsEmpty.visibility = View.VISIBLE
        } else {
            rvPersonalRecords.visibility = View.VISIBLE
            tvPersonalRecordsEmpty.visibility = View.GONE
            val topRecords = summary.personalRecords
                .groupBy { it.exerciseId }
                .values
                .map { records -> records.maxByOrNull { it.maxWeight }!! }
                .sortedByDescending { it.maxWeight }
                .take(10)
            personalRecordAdapter.submitList(topRecords)
        }
    }

    companion object {
        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }
}
