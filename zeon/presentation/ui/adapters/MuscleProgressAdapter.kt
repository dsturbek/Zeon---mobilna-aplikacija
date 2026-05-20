package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.MuscleProgress

class MuscleProgressAdapter(
    private val onMuscleClick: ((MuscleProgress) -> Unit)? = null
) : ListAdapter<MuscleProgress, MuscleProgressAdapter.MuscleProgressViewHolder>(MuscleProgressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_muscle_progress, parent, false)
        return MuscleProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: MuscleProgressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MuscleProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMuscleName: TextView = itemView.findViewById(R.id.tv_muscle_name)
        private val tvProgressPercent: TextView = itemView.findViewById(R.id.tv_progress_percent)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvInitialWeight: TextView = itemView.findViewById(R.id.tv_initial_weight)
        private val tvCurrentWeight: TextView = itemView.findViewById(R.id.tv_current_weight)
        private val tvExerciseCount: TextView = itemView.findViewById(R.id.tv_exercise_count)

        fun bind(progress: MuscleProgress) {
            tvMuscleName.text = progress.muscleGroup

            val percentText = if (progress.progressPercent >= 0) {
                "+${String.format("%.1f", progress.progressPercent)}%"
            } else {
                "${String.format("%.1f", progress.progressPercent)}%"
            }
            tvProgressPercent.text = percentText

            val progressColor = if (progress.progressPercent >= 0) {
                ContextCompat.getColor(itemView.context, R.color.zelena)
            } else {
                ContextCompat.getColor(itemView.context, R.color.crvena)
            }
            tvProgressPercent.setTextColor(progressColor)

            val progressValue = minOf(100, maxOf(0, progress.progressPercent.toInt() + 50))
            progressBar.progress = progressValue

            tvInitialWeight.text = "${formatWeight(progress.initialWeight)} kg"
            tvCurrentWeight.text = "${formatWeight(progress.currentWeight)} kg"

            val exerciseText = if (progress.exerciseCount == 1) {
                "1 exercise tracked"
            } else {
                "${progress.exerciseCount} exercises tracked"
            }
            tvExerciseCount.text = exerciseText

            itemView.setOnClickListener {
                onMuscleClick?.invoke(progress)
            }
        }

        private fun formatWeight(weight: Double): String {
            return if (weight == weight.toLong().toDouble()) {
                weight.toLong().toString()
            } else {
                String.format("%.1f", weight)
            }
        }
    }

    class MuscleProgressDiffCallback : DiffUtil.ItemCallback<MuscleProgress>() {
        override fun areItemsTheSame(oldItem: MuscleProgress, newItem: MuscleProgress): Boolean {
            return oldItem.muscleGroup == newItem.muscleGroup
        }

        override fun areContentsTheSame(oldItem: MuscleProgress, newItem: MuscleProgress): Boolean {
            return oldItem == newItem
        }
    }
}
