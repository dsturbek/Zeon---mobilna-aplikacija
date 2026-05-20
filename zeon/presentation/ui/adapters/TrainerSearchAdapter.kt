package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.model.Trainer

class TrainerSearchAdapter (private val trainersList: MutableList<Trainer>, private val onRequestClick: (Trainer) -> Unit) : RecyclerView.Adapter<TrainerSearchAdapter.TrainerSearchViewHolder>() {
    class TrainerSearchViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private var name: TextView
        private var specialization: TextView
        private var experience: TextView
        private var rating: TextView
        private var imageView: ImageView
        private var btnRequest: Button

        init {
            name = view.findViewById(R.id.tv_trainer_list_item_name)
            specialization = view.findViewById(R.id.tv_trainer_list_item_specialization)
            experience = view.findViewById(R.id.tv_trainer_list_item_experience)
            rating = view.findViewById(R.id.tv_trainer_list_item_rating)
            imageView = view.findViewById(R.id.iv_trainer_list_item_image)
            btnRequest = view.findViewById(R.id.btn_trainer_list_item_request)
        }

        fun bind(trainer: Trainer, onRequestClick: (Trainer) -> Unit){
                name.text = trainer.name
                specialization.text = trainer.specialization
                experience.text = "Iskustvo: ${trainer.experience} godina"
                rating.text = "Rating: ${trainer.rating} ⭐"
                if (trainer.imageRes != null){
                    imageView.setImageResource(trainer.imageRes)
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            btnRequest.setOnClickListener {
                onRequestClick(trainer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainerSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trainer_list_item, parent, false)
        return TrainerSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainerSearchViewHolder, position: Int) {
        holder.bind(trainersList[position], onRequestClick)
    }

    override fun getItemCount(): Int = trainersList.size

    fun updateTrainers(newTrainers: List<Trainer>) {
        trainersList.clear()
        trainersList.addAll(newTrainers)
        notifyDataSetChanged()
    }
}