package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.Workout
import com.example.zeon.helpers.UserSession
import com.example.zeon.presentation.ui.adapters.WorkoutAdapter
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.dayText)
    val container: View = view.findViewById(R.id.dayContainer)
    val eventIndicator: View = view.findViewById(R.id.eventIndicator)

    lateinit var day: CalendarDay

    init {
        view.setOnClickListener {
            if (day.position == DayPosition.MonthDate) {
                onDayClick?.invoke(day.date)
            }
        }
    }
    companion object {
        var onDayClick: ((LocalDate) -> Unit)? = null
    }
}

class PlanFragment : Fragment() {

    private lateinit var calendar: CalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: WorkoutAdapter
    private lateinit var monthYearText: TextView
    private lateinit var previousMonthButton: View
    private lateinit var nextMonthButton: View
    private lateinit var thisWeekText: TextView
    private lateinit var thisMonthText: TextView
    private lateinit var streakText: TextView

    private val workoutRepository = com.example.zeon.data.repository.WorkoutRepository.getInstance()

    private var selectedDate: LocalDate? = null
    private val events = mutableMapOf<LocalDate, MutableList<Workout>>()
    private var currentMonth = YearMonth.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar = view.findViewById(R.id.calendar)
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)
        monthYearText = view.findViewById(R.id.monthYearText)
        previousMonthButton = view.findViewById(R.id.previousMonthButton)
        nextMonthButton = view.findViewById(R.id.nextMonthButton)
        thisWeekText = view.findViewById(R.id.textView3)
        thisMonthText = view.findViewById(R.id.textView5)
        streakText = view.findViewById(R.id.textView7)

        SetUpRecyclerView()
        fetchWorkoutsFromApi()
        SetUpCalendar()
        SetUpCalendarDesign()
        SetUpMonthNavigation()

        selectedDate = LocalDate.now()
        UpdateEventsForSelectedDate()
        UpdateMonthYearText()
    }

    override fun onResume() {
        super.onResume()
        fetchWorkoutsFromApi()
    }

    private fun SetUpRecyclerView() {
        eventsAdapter = WorkoutAdapter(
            onWorkoutClick = { workout ->
                val isCompleted = workoutRepository.isWorkoutCompletedOnDate(workout.id, workout.date)
                val fragment = if (isCompleted) {
                    val workoutLog = workoutRepository.getWorkoutLogForDate(workout.id, workout.date)
                    workoutLog?.let {
                        WorkoutCompleteFragment.newInstance(it.id)
                    } ?: WorkoutDetailFragment.newInstance(workout.id)
                } else {
                    WorkoutDetailFragment.newInstance(workout.id)
                }
                (requireActivity() as? com.example.zeon.HomeActivity)?.openFragment(fragment)
            },
            isWorkoutCompleted = { workoutId ->
                selectedDate?.let { date ->
                    workoutRepository.isWorkoutCompletedOnDate(workoutId, date)
                } ?: false
            }
        )
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsRecyclerView.adapter = eventsAdapter
    }

    private fun fetchWorkoutsFromApi() {
        val clientId = UserSession.getUserId(requireContext())
        if (clientId == -1) return

        workoutRepository.fetchActiveWorkouts(clientId) { workouts ->
            if (!isAdded) return@fetchActiveWorkouts

            if (workouts != null) {
                populateEvents(workouts)
                calendar.notifyCalendarChanged()
                UpdateEventsForSelectedDate()
                UpdateStats()
            } else {
                Toast.makeText(requireContext(), "Greška pri učitavanju treninga", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateEvents(workouts: List<Workout>) {
        events.clear()
        workouts.forEach { workout ->
            events.getOrPut(workout.date) { mutableListOf() }.add(workout)
        }
    }

    private fun SetUpCalendar() {
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfTheWeek = DayOfWeek.MONDAY

        calendar.setup(startMonth, endMonth, firstDayOfTheWeek)
        calendar.scrollToMonth(currentMonth)
    }

    private fun SetUpMonthNavigation() {
        previousMonthButton.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            calendar.smoothScrollToMonth(currentMonth)
            UpdateMonthYearText()
        }

        nextMonthButton.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            calendar.smoothScrollToMonth(currentMonth)
            UpdateMonthYearText()
        }
    }

    private fun UpdateMonthYearText() {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
        monthYearText.text = currentMonth.format(formatter)
    }

    private fun SetUpCalendarDesign() {
        DayViewContainer.onDayClick = { date ->
            val oldDate = selectedDate
            selectedDate = date

            if (oldDate != null) {
                calendar.notifyDateChanged(oldDate)
            }

            calendar.notifyDateChanged(date)
            UpdateEventsForSelectedDate()
        }

        calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                val hasEvent = events.containsKey(data.date)
                val isCompleted = hasEvent && events[data.date]?.any { workout ->
                    workoutRepository.isWorkoutCompletedOnDate(workout.id, data.date)
                } == true

                when {
                    data.position != DayPosition.MonthDate -> {
                        container.textView.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.siva)
                        )
                        container.container.background = null
                        container.eventIndicator.visibility = View.GONE
                    }
                    data.date == selectedDate -> {
                        container.textView.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.bijela)
                        )
                        container.container.setBackgroundResource(R.drawable.selected_day)
                        if (hasEvent) {
                            container.eventIndicator.visibility = View.VISIBLE
                            container.eventIndicator.setBackgroundResource(
                                if (isCompleted) R.drawable.event_dot_green else R.drawable.event_dot
                            )
                        } else {
                            container.eventIndicator.visibility = View.GONE
                        }
                    }
                    data.date == LocalDate.now() -> {
                        container.textView.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.bijela)
                        )
                        container.container.setBackgroundResource(R.drawable.current_day)
                        if (hasEvent) {
                            container.eventIndicator.visibility = View.VISIBLE
                            container.eventIndicator.setBackgroundResource(
                                if (isCompleted) R.drawable.event_dot_green else R.drawable.event_dot
                            )
                        } else {
                            container.eventIndicator.visibility = View.GONE
                        }
                    }
                    else -> {
                        container.textView.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.bijela)
                        )
                        container.container.background = null
                        if (hasEvent) {
                            container.eventIndicator.visibility = View.VISIBLE
                            container.eventIndicator.setBackgroundResource(
                                if (isCompleted) R.drawable.event_dot_green else R.drawable.event_dot
                            )
                        } else {
                            container.eventIndicator.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun UpdateEventsForSelectedDate() {
        if (selectedDate != null) {
            val date = selectedDate
            val eventsForDate = events[date] ?: emptyList()
            eventsAdapter.submitList(eventsForDate)
        }
    }

    private fun UpdateStats() {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = today.with(DayOfWeek.SUNDAY)
        var weekTotal = 0
        var weekCompleted = 0
        events.forEach { (date, workouts) ->
            if (!date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)) {
                workouts.forEach { workout ->
                    weekTotal++
                    if (workoutRepository.isWorkoutCompletedOnDate(workout.id, date)) {
                        weekCompleted++
                    }
                }
            }
        }
        thisWeekText.text = "$weekCompleted/$weekTotal"

        val currentYearMonth = YearMonth.from(today)
        var monthCompleted = 0
        events.forEach { (date, workouts) ->
            if (YearMonth.from(date) == currentYearMonth) {
                workouts.forEach { workout ->
                    if (workoutRepository.isWorkoutCompletedOnDate(workout.id, date)) {
                        monthCompleted++
                    }
                }
            }
        }
        thisMonthText.text = "$monthCompleted"

        var streak = 0
        var checkDate = today
        while (true) {
            val dayWorkouts = events[checkDate]
            if (dayWorkouts != null && dayWorkouts.any { workoutRepository.isWorkoutCompletedOnDate(it.id, checkDate) }) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        streakText.text = "$streak"
    }
}