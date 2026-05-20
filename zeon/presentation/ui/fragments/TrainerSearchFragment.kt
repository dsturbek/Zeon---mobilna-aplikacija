package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.Trainer
import com.example.zeon.data.repository.TrainerRepository
import com.example.zeon.presentation.ui.adapters.TrainerSearchAdapter
import com.example.zeon.ws.TrainerDto
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrainerSearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: FloatingActionButton

    private val repository = TrainerRepository()
    private lateinit var adapter: TrainerSearchAdapter

    private val useMockData = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trainer_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.fab_trainer_search_back)
        recyclerView = view.findViewById(R.id.rv_trainer_search)

        setupRecyclerView()
        loadTrainers()

        btnBack.setOnClickListener {
            (requireActivity() as HomeActivity).returnToViewPager()
        }
    }

    private fun setupRecyclerView() {
        adapter = TrainerSearchAdapter(mutableListOf()) { trainer ->
            handleTrainerRequest(trainer)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TrainerSearchFragment.adapter
        }
    }

    private fun loadTrainers() {
        if (useMockData) {
            loadMockTrainers()
        } else {
            loadRealTrainers()
        }
    }

    private fun loadMockTrainers() {
        val trainers = repository.getMockTrainers()
        adapter.updateTrainers(trainers)
    }

    private fun loadRealTrainers() {
        repository.getAllTrainers().enqueue(
            object : Callback<List<TrainerDto>> {
                override fun onResponse(
                    call: Call<List<TrainerDto>>,
                    response: Response<List<TrainerDto>>
                ) {
                    if (response.isSuccessful) {
                        val trainersDto = response.body()
                        if (trainersDto != null && trainersDto.isNotEmpty()) {
                            val trainers = repository.mapTrainersFromDto(trainersDto, requireContext())
                            adapter.updateTrainers(trainers)
                        } else {
                            showToast("Nema dostupnih trenera")
                        }
                    } else {
                        showToast("Greška pri učitavanju trenera")
                    }
                }

                override fun onFailure(call: Call<List<TrainerDto>>, t: Throwable) {
                    showToast("Greška: ${t.message}")
                }
            }
        )
    }

    private fun handleTrainerRequest(trainer: Trainer) {
        showToast("Zahtjev za povezivanje s ${trainer.name} - uskoro dostupno!")

        // TODO: implementirati s REST API
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}