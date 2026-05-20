package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.ExerciseLog
import com.example.zeon.data.model.WorkoutExercise
import com.google.android.material.checkbox.MaterialCheckBox

data class ExerciseProgress(
    val workoutExercise: WorkoutExercise,
    val isCompleted: Boolean,
    val completedLogs: List<ExerciseLog>
)

class WorkoutExerciseProgressAdapter(
    private val onExerciseClick: (WorkoutExercise) -> Unit,
    private val onCompletionUpdate: () -> Unit
) : ListAdapter<ExerciseProgress, WorkoutExerciseProgressAdapter.ExerciseViewHolder>(ExerciseProgressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_exercise_progress, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseCard: CardView = itemView.findViewById(R.id.exerciseCard)
        private val exerciseNumber: TextView = itemView.findViewById(R.id.exerciseNumber)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseMuscleGroup: TextView = itemView.findViewById(R.id.exerciseMuscleGroup)
        private val exerciseCompletedData: TextView = itemView.findViewById(R.id.exerciseCompletedData)
        private val exerciseCheckbox: MaterialCheckBox = itemView.findViewById(R.id.exerciseCheckbox)

        fun bind(progress: ExerciseProgress, number: Int) {
            val exercise = progress.workoutExercise.exercise

            exerciseNumber.text = number.toString()
            exerciseName.text = exercise.name
            exerciseMuscleGroup.text = exercise.muscleGroup
            exerciseCheckbox.isChecked = progress.isCompleted

            if (progress.isCompleted && progress.completedLogs.isNotEmpty()) {
                exerciseCompletedData.visibility = View.VISIBLE

                val setsText = progress.completedLogs.joinToString(", ") { log ->
                    "${log.weight.toInt()}kg×${log.completedReps}"
                }
                exerciseCompletedData.text = setsText

                exerciseCard.setCardBackgroundColor(
                    itemView.context.getColor(R.color.zelena)
                )
                exerciseNumber.setBackgroundColor(
                    itemView.context.getColor(R.color.zelena)
                )
            } else {
                exerciseCompletedData.visibility = View.GONE
                exerciseCard.setCardBackgroundColor(
                    itemView.context.getColor(R.color.pozadina_card)
                )
                exerciseNumber.setBackgroundColor(
                    itemView.context.getColor(R.color.crvena)
                )
            }

            itemView.setOnClickListener {
                onExerciseClick(progress.workoutExercise)
            }
        }
    }

    class ExerciseProgressDiffCallback : DiffUtil.ItemCallback<ExerciseProgress>() {
        override fun areItemsTheSame(oldItem: ExerciseProgress, newItem: ExerciseProgress): Boolean {
            return oldItem.workoutExercise.id == newItem.workoutExercise.id
        }

        override fun areContentsTheSame(oldItem: ExerciseProgress, newItem: ExerciseProgress): Boolean {
            return oldItem == newItem
        }
    }
}
