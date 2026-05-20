package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.Workout
import java.time.format.DateTimeFormatter

class WorkoutAdapter(
    private val onWorkoutClick: (Workout) -> Unit = {},
    private val isWorkoutCompleted: (String) -> Boolean = { false }
) : ListAdapter<Workout, WorkoutAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.workoutName)
        private val dateTextView: TextView=itemView.findViewById(R.id.workoutDate)
        private val timeTextView: TextView = itemView.findViewById(R.id.workoutTime)
        private val numberOfExercisesTextView: TextView = itemView.findViewById(R.id.workoutNumberOfExercises)
        private val muscleGroupTextView: TextView = itemView.findViewById(R.id.workoutMuscleGroup)
        private val cardLayout: android.widget.LinearLayout = itemView.findViewById(R.id.eventItemLayout)
        private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

        fun bind(event: Workout) {
            titleTextView.text = event.title
            timeTextView.text = event.time.toString()
            dateTextView.text = event.date.format(dateFormatter);
            numberOfExercisesTextView.text=event.numberOfExercises.toString();
            muscleGroupTextView.text=event.muscleGroup

            val completed = isWorkoutCompleted(event.id)
            if (completed) {
                cardLayout.setBackgroundColor(itemView.context.getColor(R.color.zelena))
            } else {
                cardLayout.setBackgroundColor(itemView.context.getColor(R.color.crvena))
            }

            itemView.setOnClickListener {
                onWorkoutClick(event)
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }
}