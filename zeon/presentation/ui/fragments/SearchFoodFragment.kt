package com.example.zeon.presentation.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.data.MockFoodList
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.presentation.ui.adapters.FoodAdapter
import com.example.zeon.presentation.viewmodel.SearchFoodViewModel
import com.example.zeon.presentation.viewmodel.SearchFoodViewModelFactory

class SearchFoodFragment: DialogFragment(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnClose: Button
    private lateinit var btnAddNewFood: Button
    private lateinit var adapter: FoodAdapter
    private lateinit var viewModel: SearchFoodViewModel

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_food,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        reachingXMLComponents(view)
        setupRecyclerView()
        setupButtonListeners()
        setupObservers()
    }

    private fun initializeViewModel() {
        val repository = FoodRepository(requireContext())
        val factory = SearchFoodViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SearchFoodViewModel::class.java)
    }

    private fun reachingXMLComponents(view:View){
        btnClose=view.findViewById<Button>(R.id.btnCloseSearch)
        btnAddNewFood=view.findViewById<Button>(R.id.btnAddNewFood)
        recyclerView=view.findViewById(R.id.rvSearchFood)
        etSearch= view.findViewById(R.id.etSearchFood)
    }

    private fun setupRecyclerView(){
        adapter= FoodAdapter(mutableListOf(),R.layout.food_list_item_search)
        recyclerView.layoutManager= LinearLayoutManager(requireContext())
        recyclerView.adapter= adapter

        adapter.onItemClick={selectedFood->
            parentFragmentManager.setFragmentResult(
                "selected_food",
                Bundle().apply { putInt("id",selectedFood.id) }
            )
            dismiss()
        }
    }

    private fun setupButtonListeners(){
        btnClose.setOnClickListener {
            dismiss()
        }

        btnAddNewFood.setOnClickListener {
            showAddFoodDialog()
        }

        etSearch.addTextChangedListener{text->
            val textToString=text.toString()
            viewModel.searchFoods(textToString)
        }
    }

    private fun setupObservers() {
        viewModel.filteredFoods.observe(viewLifecycleOwner) { foods ->
            adapter.updateFood(foods) }

        viewModel.foodAddedSuccess.observe(viewLifecycleOwner) { newFood ->
            if (newFood != null) {
                Toast.makeText(
                    requireContext(),
                    "Hrana '${newFood.name}' dodan/a!",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.setFragmentResult(
                    "foods_refreshed",
                    Bundle().apply {
                        putInt("id", newFood.id)
                        putBoolean("refresh", true)
                    }
                )
                dismiss()
            }
        }
    }

    private fun showAddFoodDialog(){
        val newFoodDialogView= LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_add_food,null)

        val etName=newFoodDialogView.findViewById<EditText>(R.id.etFoodName)
        val etCalories=newFoodDialogView.findViewById<EditText>(R.id.etCalories)
        val etProtein=newFoodDialogView.findViewById<EditText>(R.id.etProtein)
        val etFat=newFoodDialogView.findViewById<EditText>(R.id.etFat)
        val etCarbs=newFoodDialogView.findViewById<EditText>(R.id.etCarbs)
        val btnDodaj = newFoodDialogView.findViewById<Button>(R.id.btnDodaj)
        val btnOdustani = newFoodDialogView.findViewById<Button>(R.id.btnOdustani)
        val spinnerFoodType = newFoodDialogView.findViewById<Spinner>(R.id.spinnerFoodType)

        val foodTypes = arrayOf("Hrana (grama)", "Tekućina (mililitara)")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            foodTypes
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFoodType.adapter = spinnerAdapter


        val dialog=AlertDialog.Builder(context)
            .setView(newFoodDialogView)
            .create()

        dialog.show()

        btnDodaj.setOnClickListener {
            val name = etName.text.toString().trim()
            val calories = etCalories.text.toString().toIntOrNull() ?: 0
            val protein = etProtein.text.toString().toFloatOrNull() ?: 0f
            val fat = etFat.text.toString().toFloatOrNull() ?: 0f
            val carbs = etCarbs.text.toString().toFloatOrNull() ?: 0f
            val isLiquid = spinnerFoodType.selectedItemPosition == 1

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite naziv hrane", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (calories <= 0) {
                Toast.makeText(requireContext(), "Unesite kalorije > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addNewFood(
                name = name,
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs,
                isLiquid = isLiquid
            )

            dialog.dismiss()
        }

        btnOdustani.setOnClickListener {
            dialog.dismiss()
        }
    }
}