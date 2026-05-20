package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.WorkoutExercise
import com.google.android.material.checkbox.MaterialCheckBox

class WorkoutExerciseAdapter(
    private val onExerciseClick: (WorkoutExercise) -> Unit
) : ListAdapter<WorkoutExercise, WorkoutExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNumber: TextView = itemView.findViewById(R.id.exerciseNumber)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseMuscleGroup: TextView = itemView.findViewById(R.id.exerciseMuscleGroup)
        private val exerciseSetsReps: TextView = itemView.findViewById(R.id.exerciseSetsReps)
        private val exerciseWeight: TextView = itemView.findViewById(R.id.exerciseWeight)
        private val exerciseCheckbox: MaterialCheckBox = itemView.findViewById(R.id.exerciseCheckbox)

        fun bind(workoutExercise: WorkoutExercise, number: Int) {
            exerciseNumber.text = number.toString()
            exerciseName.text = workoutExercise.exercise.name
            exerciseMuscleGroup.text = workoutExercise.exercise.muscleGroup
            exerciseSetsReps.text = "${workoutExercise.plannedSets} sets × ${workoutExercise.plannedReps} reps"

            exerciseWeight.visibility = View.GONE

            exerciseCheckbox.visibility = View.GONE

            itemView.setOnClickListener {
                onExerciseClick(workoutExercise)
            }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<WorkoutExercise>() {
        override fun areItemsTheSame(oldItem: WorkoutExercise, newItem: WorkoutExercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkoutExercise, newItem: WorkoutExercise): Boolean {
            return oldItem == newItem
        }
    }
}
