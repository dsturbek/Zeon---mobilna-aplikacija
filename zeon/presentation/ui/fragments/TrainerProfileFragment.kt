package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.Trainer
import com.example.zeon.data.repository.TrainerRepository
import com.example.zeon.helpers.UserSession
import com.example.zeon.ws.TrainerDto
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrainerProfileFragment : Fragment() {

    private lateinit var ivTrainerImage: ShapeableImageView
    private lateinit var tvTrainerName: TextView
    private lateinit var tvTrainerSpecialization: TextView
    private lateinit var tvTrainerRating: TextView
    private lateinit var tvTrainerPrice: TextView
    private lateinit var btnDisconnect: MaterialButton
    private lateinit var fabBack: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private val repository = TrainerRepository()
    private var trainerId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trainer_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trainerId = arguments?.getInt(ARG_TRAINER_ID, -1) ?: -1

        initViews(view)
        setupClickListeners()
        loadTrainerData()
    }

    private fun initViews(view: View) {
        ivTrainerImage = view.findViewById(R.id.iv_trainer_image)
        tvTrainerName = view.findViewById(R.id.tv_trainer_name)
        tvTrainerSpecialization = view.findViewById(R.id.tv_trainer_specialization)
        tvTrainerRating = view.findViewById(R.id.tv_trainer_rating)
        tvTrainerPrice = view.findViewById(R.id.tv_trainer_price)
        btnDisconnect = view.findViewById(R.id.btn_disconnect)
        fabBack = view.findViewById(R.id.fab_back)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        fabBack.setOnClickListener {
            (requireActivity() as HomeActivity).returnToViewPager()
        }

        btnDisconnect.setOnClickListener {
            handleDisconnect()
        }
    }

    private fun loadTrainerData() {
        if (trainerId == -1) {
            showToast("Greška: Trener nije pronađen")
            (requireActivity() as HomeActivity).returnToViewPager()
            return
        }

        progressBar.visibility = View.VISIBLE

        repository.getTrainerById(trainerId).enqueue(object : Callback<TrainerDto> {
            override fun onResponse(call: Call<TrainerDto>, response: Response<TrainerDto>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val trainerDto = response.body()
                    if (trainerDto != null) {
                        val trainer = repository.mapTrainerFromDto(trainerDto, requireContext())
                        displayTrainerData(trainer)
                    } else {
                        showToast("Trener nije pronađen")
                    }
                } else {
                    showToast("Greška pri učitavanju trenera")
                }
            }

            override fun onFailure(call: Call<TrainerDto>, t: Throwable) {
                progressBar.visibility = View.GONE
                showToast("Greška: ${t.message}")
            }
        })
    }

    private fun displayTrainerData(trainer: Trainer) {
        tvTrainerName.text = trainer.name
        tvTrainerSpecialization.text = trainer.specialization
        tvTrainerRating.text = String.format("%.1f", trainer.rating)
        tvTrainerPrice.text = "$${trainer.price.toInt()}"

        trainer.imageRes?.let {
            ivTrainerImage.setImageResource(it)
        }
    }

    private fun handleDisconnect() {
        UserSession.saveTrainerId(requireContext(), null)
        showToast("Odspojeni ste od trenera")
        (requireActivity() as HomeActivity).returnToViewPager()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_TRAINER_ID = "trainer_id"

        fun newInstance(trainerId: Int): TrainerProfileFragment {
            return TrainerProfileFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TRAINER_ID, trainerId)
                }
            }
        }
    }
}
