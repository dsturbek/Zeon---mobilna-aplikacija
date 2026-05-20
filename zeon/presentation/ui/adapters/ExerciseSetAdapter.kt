package com.example.zeon.presentation.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

data class ExerciseSet(
    val setNumber: Int,
    var weight: Double? = null,
    var reps: Int? = null,
    var isCompleted: Boolean = false,
    val hintWeight: Double? = null,
    val hintReps: Int? = null
)

class ExerciseSetAdapter(
    private val sets: MutableList<ExerciseSet>,
    private val onSetChanged: (Int, ExerciseSet) -> Unit
) : RecyclerView.Adapter<ExerciseSetAdapter.SetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_set_input, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(sets[position], position)
    }

    override fun getItemCount(): Int = sets.size

    fun addSet() {
        val newSetNumber = sets.size + 1
        sets.add(ExerciseSet(newSetNumber))
        notifyItemInserted(sets.size - 1)
    }

    fun getSets(): List<ExerciseSet> = sets.toList()

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val setNumber: TextView = itemView.findViewById(R.id.setNumber)
        private val weightInput: TextInputEditText = itemView.findViewById(R.id.weightInput)
        private val repsInput: TextInputEditText = itemView.findViewById(R.id.repsInput)
        private val completedCheckbox: MaterialCheckBox = itemView.findViewById(R.id.setCompleteCheckbox)

        fun bind(set: ExerciseSet, position: Int) {
            setNumber.text = set.setNumber.toString()

            weightInput.setText(set.weight?.toString() ?: "")
            repsInput.setText(set.reps?.toString() ?: "")
            weightInput.hint = set.hintWeight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
            repsInput.hint = set.hintReps?.toString() ?: ""
            completedCheckbox.isChecked = set.isCompleted

            weightInput.tag = null
            repsInput.tag = null

            weightInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (weightInput.tag != null) return
                    set.weight = s?.toString()?.toDoubleOrNull()
                    onSetChanged(position, set)
                }
            })

            repsInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (repsInput.tag != null) return
                    set.reps = s?.toString()?.toIntOrNull()
                    onSetChanged(position, set)
                }
            })

            completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
                set.isCompleted = isChecked
                onSetChanged(position, set)
            }
        }
    }
}
