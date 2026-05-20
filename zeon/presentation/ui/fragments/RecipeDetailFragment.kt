package com.example.zeon.presentation.ui.fragments

import android.R.attr.text
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.MockFoodList
import com.example.zeon.data.model.FoodFoodDiary
import com.example.zeon.data.model.FoodItem
import com.example.zeon.data.model.MealType
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.helpers.NutritionCalculator
import com.example.zeon.presentation.viewmodel.RecipeViewModel
import com.example.zeon.presentation.viewmodel.RecipeViewModelFactory
import com.example.zeon.ws.Recipe
import com.squareup.picasso.Picasso
import java.time.LocalDate

class RecipeDetailFragment : Fragment() {

    private lateinit var ivRecipeImage: ImageView
    private lateinit var tvRecipeName: TextView
    private lateinit var tvCategoryArea: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnAddToDiary: Button
    private lateinit var pbLoading: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var viewModel: RecipeViewModel
    private var currentRecipeName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        reachingXMLComponents(view)
        loadRecipesData()
        setupButtonListeners()
        setupObservers()
    }

    private fun initializeViewModel() {
        val repository = FoodRepository(requireContext())
        val factory = RecipeViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(RecipeViewModel::class.java)
    }

    private fun reachingXMLComponents(view: View){
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage)
        tvRecipeName = view.findViewById(R.id.tvRecipeName)
        tvCategoryArea = view.findViewById(R.id.tvCategoryArea)
        tvIngredients = view.findViewById(R.id.tvIngredients)
        tvInstructions = view.findViewById(R.id.tvInstructions)
        btnAddToDiary = view.findViewById(R.id.btnAddToDiary)
        btnBack=view.findViewById(R.id.btnBack)
        pbLoading = view.findViewById(R.id.pbLoading)
    }

    private fun setupButtonListeners(){
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAddToDiary.setOnClickListener {
            showAddAmountDialog()
        }
    }

    private fun loadRecipesData(){
        val recipeName = arguments?.getString("recipeName") ?: ""
        val recipeCategory = arguments?.getString("recipeCategory") ?: ""
        val recipeArea = arguments?.getString("recipeArea") ?: ""
        val recipeInstructions = arguments?.getString("recipeInstructions") ?: ""
        val recipeThumb = arguments?.getString("recipeThumb") ?: ""
        val recipeIngredients = arguments?.getString("recipeIngredients") ?: ""

        currentRecipeName = recipeName
        tvRecipeName.text = recipeName
        tvCategoryArea.text = "$recipeCategory • $recipeArea"
        tvIngredients.text = recipeIngredients
        tvInstructions.text = recipeInstructions

        Picasso.get()
            .load(recipeThumb)
            .placeholder(R.drawable.rounded_pill_gray)
            .error(R.drawable.rounded_input_bg)
            .into(ivRecipeImage)


    }
    private fun setupObservers() {
        viewModel.recipeAddedSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "$currentRecipeName uspješno dodan/a!",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.setFragmentResult("recipe_added", Bundle().apply {
                    putBoolean("refresh", true)
                })
                parentFragmentManager.popBackStack()
                viewModel.resetRecipeAdded()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showAddAmountDialog(){
        val dialogView= LayoutInflater.from(context).inflate(R.layout.dialog_add_amount, null)
        val tvFoodName = dialogView.findViewById<TextView>(R.id.tvFoodName)
        val tvFoodInfo = dialogView.findViewById<TextView>(R.id.tvFoodInfo)
        val tvAmountLabel = dialogView.findViewById<TextView>(R.id.tvAmountLabel)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvCalculated = dialogView.findViewById<TextView>(R.id.tvCalculated)
        val spinnerMealType = dialogView.findViewById<Spinner>(R.id.spinnerMealType)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnDodaj)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnOdustani)

        tvFoodName.text = currentRecipeName
        tvAmountLabel.text = "Količina (Gram)"
        etAmount.hint = "npr. 150"
        tvFoodInfo.text = "200 kcal | 15g Protein | 5g Masti | 25g Ugljikohidrata (na 100g)"

        spinnerMealType.visibility = View.GONE

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MealType.entries.toTypedArray()
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMealType.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        etAmount.addTextChangedListener { text ->
            val amount = text.toString().toFloatOrNull() ?: 100f
            val calculator = NutritionCalculator()
            val calculatedCal = calculator.calculateCaloriesForAmount(200f, amount)
            tvCalculated.text = "= ${calculatedCal.toInt()} kcal"
        }

        btnAdd.setOnClickListener {
            val amount = etAmount.text.toString().toFloatOrNull() ?: 100f
            val selectedMealType = spinnerMealType.selectedItem as MealType

            viewModel.addRecipeToDiary(currentRecipeName, amount, selectedMealType)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}