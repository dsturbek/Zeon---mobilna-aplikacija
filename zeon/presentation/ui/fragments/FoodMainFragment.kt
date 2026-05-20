package com.example.zeon.presentation.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.model.MealType
import com.example.zeon.data.repository.FoodRepository
import com.example.zeon.helpers.NutritionCalculator
import com.example.zeon.presentation.ui.adapters.FoodAdapter
import com.example.zeon.presentation.viewmodel.FoodViewModel
import com.example.zeon.presentation.viewmodel.FoodViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FoodMainFragment: Fragment() {
    private lateinit var btnSearchFood: Button
    private lateinit var btnRecipes: Button
    private lateinit var btnPreviousDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvCaloriesGoal: TextView
    private lateinit var tvWater: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFat: TextView

    private val breakfastAdapter= FoodAdapter(mutableListOf(),R.layout.food_list_item)
    private var currentMealType: MealType? = null

    private lateinit var viewModel: FoodViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_food_main,container,false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDiaryData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupFragmentResultListeners()
        reachingXMLComponents(view)
        setupMealCards(view)
        setupButtonListeners()
        setupObservers()

        viewModel.loadDiaryData()
    }

    private fun initializeViewModel(){
        val repository = FoodRepository(requireContext())
        val calculator = NutritionCalculator()
        val factory = FoodViewModelFactory(repository, calculator)

        viewModel = ViewModelProvider(this, factory).get(FoodViewModel::class.java)

    }

    private fun setupFragmentResultListeners(){
        parentFragmentManager.setFragmentResultListener(
            "selected_food",
            viewLifecycleOwner
        ) { _, bundle ->

            val foodId = bundle.getInt("id")
            showAddAmountDialog(foodId, currentMealType)
        }

        parentFragmentManager.setFragmentResultListener(
            "recipe_added",
            viewLifecycleOwner
        ) { _, bundle ->
            val refresh = bundle.getBoolean("refresh", false)
            if (refresh) {
                viewModel.loadAllFoods()
                viewModel.loadDiaryData()
            }
        }

        parentFragmentManager.setFragmentResultListener(
            "foods_refreshed",
            viewLifecycleOwner
        ) { _, bundle ->
            val foodId = bundle.getInt("id")
            val refresh = bundle.getBoolean("refresh", false)

            if (refresh) {
                viewModel.loadAllFoods()
                showAddAmountDialog(foodId, currentMealType)
            }
        }

    }

    private fun reachingXMLComponents(view: View){
        btnPreviousDay=view.findViewById(R.id.btnPreviousDay)
        btnNextDay=view.findViewById(R.id.btnNextDay)
        btnSearchFood=view.findViewById(R.id.btnSearchFood)
        btnRecipes=view.findViewById(R.id.btnRecipes)

        tvCalories = view.findViewById(R.id.tvCalories)
        tvCaloriesGoal = view.findViewById(R.id.tvCaloriesGoal)
        tvWater = view.findViewById(R.id.tvWater)
        tvProtein = view.findViewById(R.id.tvProtein)
        tvCarbs = view.findViewById(R.id.tvCarbs)
        tvFat = view.findViewById(R.id.tvFat)
        tvSelectedDate=view.findViewById(R.id.tvSelectedDate)
    }

    private fun setupMealCards(view: View){
        setupMealCard(
            cardView = view.findViewById(R.id.mealCardBreakfast),
            mealType = MealType.DORUČAK,
            mealName = "HRANA",
            adapter = breakfastAdapter
        )

    }

    private fun setupMealCard(cardView: View, mealType: MealType, mealName: String, adapter: FoodAdapter){
        val tvMealName = cardView.findViewById<TextView>(R.id.tvMealName)
        val rvMealFood = cardView.findViewById<RecyclerView>(R.id.rvMealFood)
        val btnAddFood = cardView.findViewById<Button>(R.id.btnAddFood)

        tvMealName.text=mealName
        rvMealFood.adapter=adapter
        rvMealFood.layoutManager= LinearLayoutManager(context)

        adapter.onDeleteClick = { food ->
            viewModel.removeFoodFromDiary(food.id, mealType)
        }

        btnAddFood.setOnClickListener {
            currentMealType = mealType
            SearchFoodFragment().show(parentFragmentManager, "searchFood")
        }

    }

    private fun setupButtonListeners(){
        btnSearchFood.setOnClickListener {
            currentMealType=null
            SearchFoodFragment().show(parentFragmentManager, "searchFood")
        }

        btnRecipes.setOnClickListener {
            (activity as HomeActivity).openFragment(RecipesFragment())
        }

        btnPreviousDay.setOnClickListener {
           viewModel.previousDay()
        }

        btnNextDay.setOnClickListener {
           viewModel.nextDay()

        }

        tvSelectedDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun setupObservers() {
        viewModel.breakfastEntries.observe(viewLifecycleOwner) { entries ->
            updateMealCard(MealType.DORUČAK, entries, breakfastAdapter)
        }

        viewModel.nutritionSummary.observe(viewLifecycleOwner) { nutrition ->
            updateNutritionDisplay(nutrition)
        }

        viewModel.caloriesPercent.observe(viewLifecycleOwner) { percent ->
            val color = NutritionCalculator().getColorForProgress(percent, resources)
            tvCalories.setTextColor(color)
        }

        viewModel.proteinPercent.observe(viewLifecycleOwner) { percent ->
            val color = NutritionCalculator().getColorForProgress(percent, resources)
            tvProtein.setTextColor(color)
        }

        viewModel.carbsPercent.observe(viewLifecycleOwner) { percent ->
            val color = NutritionCalculator().getColorForProgress(percent, resources)
            tvCarbs.setTextColor(color)
        }

        viewModel.fatPercent.observe(viewLifecycleOwner) { percent ->
            val color = NutritionCalculator().getColorForProgress(percent, resources)
            tvFat.setTextColor(color)
        }

        viewModel.waterAmount.observe(viewLifecycleOwner) { water ->
            val goals = viewModel.userGoals.value
            if (goals != null) {
                tvWater.text = String.format("%.1f L", water)
            }
        }

        viewModel.waterPercent.observe(viewLifecycleOwner) { percent ->
            val color = NutritionCalculator().getColorForProgress(percent, resources)
            tvWater.setTextColor(color)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateDateDisplay(date)
        }

        viewModel.foodAddedSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Hrana dodan/a!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.foodRemovedSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Hrana obrisana!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userGoals.observe(viewLifecycleOwner) { goals ->
            tvCaloriesGoal.text = "/ ${goals.goalCalories} kcal"
        }
    }

    private fun updateMealCard(
        mealType: MealType,
        entries: List<com.example.zeon.data.model.FoodFoodDiary>,
        adapter: FoodAdapter
    ) {
        val cardView = when (mealType) {
            MealType.DORUČAK -> requireView().findViewById<View>(R.id.mealCardBreakfast)
            else -> return
        }

        val allFoodList = viewModel.allFoods.value ?: emptyList()
        val foodItems = entries.mapNotNull { entry ->
            allFoodList.find { it.id == entry.foodId }
        }

        adapter.updateFood(foodItems)

        val tvMealCalories = cardView.findViewById<TextView>(R.id.tvMealCalories)
        val tvEmptyState = cardView.findViewById<TextView>(R.id.tvEmptyState)
        val rvMealFood = cardView.findViewById<RecyclerView>(R.id.rvMealFood)

        val mealCalories = viewModel.getMealCalories(mealType)
        tvMealCalories.text = "${mealCalories.toInt()} kcal"

        if (foodItems.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvMealFood.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvMealFood.visibility = View.VISIBLE
        }
    }

    private fun updateNutritionDisplay(nutrition: com.example.zeon.helpers.NutritionSummary) {
        tvCalories.text = "${nutrition.calories.toInt()} kcal"
        tvProtein.text = "${nutrition.protein.toInt()}g "
        tvCarbs.text = "${nutrition.carbs.toInt()}g "
        tvFat.text = "${nutrition.fat.toInt()}g "
    }

    private fun updateDateDisplay(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale("hr"))
        val dateText = date.format(formatter)
        tvSelectedDate.text = dateText.replaceFirstChar { it.uppercase() }

        val canGoNext = viewModel.canGoToNextDay()
        btnNextDay.isEnabled = canGoNext
        btnNextDay.alpha = if (canGoNext) 1.0f else 0.3f
    }


    private fun showAddAmountDialog(foodId: Int, preselectedMealType: MealType?=null){
        val newAmountDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_add_amount, null)

        val food = viewModel.allFoods.value?.find { it.id == foodId } ?: return

        val tvFoodName = newAmountDialogView.findViewById<TextView>(R.id.tvFoodName)
        val tvFoodInfo = newAmountDialogView.findViewById<TextView>(R.id.tvFoodInfo)
        val tvAmountLabel = newAmountDialogView.findViewById<TextView>(R.id.tvAmountLabel)
        val etAmount = newAmountDialogView.findViewById<EditText>(R.id.etAmount)
        val tvCalculated = newAmountDialogView.findViewById<TextView>(R.id.tvCalculated)
        val spinnerMealType = newAmountDialogView.findViewById<Spinner>(R.id.spinnerMealType)
        val btnAdd = newAmountDialogView.findViewById<Button>(R.id.btnDodaj)
        val btnCancel = newAmountDialogView.findViewById<Button>(R.id.btnOdustani)

        tvFoodName.text = food.name
        if (food.isLiquid) {
            tvAmountLabel.text = "Količina (Mililitar)"
            etAmount.hint = "npr. 250"
            tvFoodInfo.text = "${food.calories} kcal (na 100ml)"
        } else {
            tvAmountLabel.text = "Količina (Gram)"
            etAmount.hint = "npr. 150"
            tvFoodInfo.text = "${food.calories} kcal | ${food.protein}g Protein | ${food.fat}g Masti | ${food.carbs}g Ugljikohidrata (na 100g)"
        }

        spinnerMealType.visibility = View.GONE

        val spinnerAdapter= ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MealType.entries.toTypedArray()
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMealType.adapter=spinnerAdapter

        val dialog=AlertDialog.Builder(context).setView(newAmountDialogView).create()

        etAmount.addTextChangedListener { text ->
            val amount = text.toString().toFloatOrNull() ?: 100f
            val calculator = NutritionCalculator()
            val calculatedCal = calculator.calculateCaloriesForAmount(food.calories.toFloat(), amount)
            tvCalculated.text = "= ${calculatedCal.toInt()} kcal"
        }

        btnAdd.setOnClickListener {
            val amount = etAmount.text.toString().toFloatOrNull()?:100f
            val selectedMealType = MealType.DORUČAK

            viewModel.addFoodToDiary(food.id, amount, selectedMealType)
            currentMealType=null
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        if(preselectedMealType!=null){
            val position=spinnerAdapter.getPosition(preselectedMealType)
            if(position>=0){
                spinnerMealType.setSelection(position)
            }
        }
        dialog.show()
    }

    private fun showDatePickerDialog(){
        val currentDate = viewModel.selectedDate.value ?: LocalDate.now()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.setSelectedDate(newDate)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

}
