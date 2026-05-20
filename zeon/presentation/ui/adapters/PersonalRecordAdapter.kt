package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.PersonalRecord
import java.time.format.DateTimeFormatter

class PersonalRecordAdapter(
    private val onRecordClick: ((PersonalRecord) -> Unit)? = null
) : ListAdapter<PersonalRecord, PersonalRecordAdapter.PersonalRecordViewHolder>(PersonalRecordDiffCallback()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personal_record, parent, false)
        return PersonalRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonalRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonalRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvMuscleGroup: TextView = itemView.findViewById(R.id.tv_muscle_group)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvMaxWeight: TextView = itemView.findViewById(R.id.tv_max_weight)
        private val tvMaxReps: TextView = itemView.findViewById(R.id.tv_max_reps)

        fun bind(record: PersonalRecord) {
            tvExerciseName.text = record.exerciseName
            tvMuscleGroup.text = record.muscle
            tvDate.text = record.dateAchievement.format(dateFormatter)
            tvMaxWeight.text = "${formatWeight(record.maxWeight)} kg"
            tvMaxReps.text = "${record.maxReps} reps"

            itemView.setOnClickListener {
                onRecordClick?.invoke(record)
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

    class PersonalRecordDiffCallback : DiffUtil.ItemCallback<PersonalRecord>() {
        override fun areItemsTheSame(oldItem: PersonalRecord, newItem: PersonalRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PersonalRecord, newItem: PersonalRecord): Boolean {
            return oldItem == newItem
        }
    }
}
