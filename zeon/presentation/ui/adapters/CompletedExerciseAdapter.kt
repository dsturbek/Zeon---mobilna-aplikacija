package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.ExerciseLog
import com.example.zeon.data.model.WorkoutExercise

data class CompletedExerciseItem(
    val workoutExercise: WorkoutExercise,
    val logs: List<ExerciseLog>,
    val isPR: Boolean
)

class CompletedExerciseAdapter : ListAdapter<CompletedExerciseItem, CompletedExerciseAdapter.ViewHolder>(CompletedExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNumber: TextView = itemView.findViewById(R.id.exerciseNumber)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseMuscleGroup: TextView = itemView.findViewById(R.id.exerciseMuscleGroup)
        private val exerciseCompletedData: TextView = itemView.findViewById(R.id.exerciseCompletedData)
        private val prBadge: TextView = itemView.findViewById(R.id.prBadge)

        fun bind(item: CompletedExerciseItem, number: Int) {
            val exercise = item.workoutExercise.exercise

            exerciseNumber.text = number.toString()
            exerciseName.text = exercise.name
            exerciseMuscleGroup.text = exercise.muscleGroup

            prBadge.visibility = if (item.isPR) View.VISIBLE else View.GONE

            if (item.logs.isNotEmpty()) {
                val weights = item.logs.map { it.weight.toInt() }.distinct()
                val reps = item.logs.joinToString(", ") { it.completedReps.toString() }

                val weightText = if (weights.size == 1) {
                    "${weights[0]}kg"
                } else {
                    "${weights.min()}-${weights.max()}kg"
                }

                exerciseCompletedData.text = "$weightText × $reps"
            }
        }
    }

    class CompletedExerciseDiffCallback : DiffUtil.ItemCallback<CompletedExerciseItem>() {
        override fun areItemsTheSame(oldItem: CompletedExerciseItem, newItem: CompletedExerciseItem): Boolean {
            return oldItem.workoutExercise.id == newItem.workoutExercise.id
        }

        override fun areContentsTheSame(oldItem: CompletedExerciseItem, newItem: CompletedExerciseItem): Boolean {
            return oldItem == newItem
        }
    }
}
