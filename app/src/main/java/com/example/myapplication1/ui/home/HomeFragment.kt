package com.example.myapplication1.ui.home

import Product
import ProductRepository
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.ProductAdapter
import com.example.myapplication1.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var repository: ProductRepository
    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var balanceText: TextView

    private var startDate: Long = 0L
    private var endDate: Long = System.currentTimeMillis()
    private var dateSelectionDialog: android.app.AlertDialog? = null
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Добавляем заголовок
        val title = TextView(requireContext()).apply {
            text = "Главная"
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        // Контейнер для общей статистики с иконкой календаря
        val statsHeaderContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(30, 10, 30, 10)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Заголовок статистики
        val statsTitle = TextView(requireContext()).apply {
            text = "Общие доходы/расходы"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }
        statsHeaderContainer.addView(statsTitle)

        // Иконка календаря
        val calendarIcon = TextView(requireContext()).apply {
            text = "📅"
            textSize = 20f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showDateSelectionDialog() }
            gravity = Gravity.CENTER
        }
        statsHeaderContainer.addView(calendarIcon)

        rootView.addView(statsHeaderContainer)

        // Контейнер для статистики
        val statsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(30, 0, 30, 20)
            }
            orientation = LinearLayout.VERTICAL
        }

        // Баланс
        balanceText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 15
            }
            text = "Баланс: 0 ₽"
            textSize = 18f
        }
        statsContainer.addView(balanceText)

        // Контейнер для дохода и расхода
        val incomeExpenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // Доход
        val incomeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val incomeLabel = TextView(requireContext()).apply {
            text = "Доходы"
            textSize = 14f
            setTextColor(Color.GRAY)
        }
        incomeContainer.addView(incomeLabel)

        totalIncomeText = TextView(requireContext()).apply {
            text = "0 ₽"
            textSize = 16f
            setTextColor(Color.parseColor("#4CAF50"))
        }
        incomeContainer.addView(totalIncomeText)

        // Расход
        val expenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val expenseLabel = TextView(requireContext()).apply {
            text = "Расходы"
            textSize = 14f
            setTextColor(Color.GRAY)
        }
        expenseContainer.addView(expenseLabel)

        totalExpenseText = TextView(requireContext()).apply {
            text = "0 ₽"
            textSize = 16f
            setTextColor(Color.parseColor("#F44336"))
        }
        expenseContainer.addView(totalExpenseText)

        incomeExpenseContainer.addView(incomeContainer)
        incomeExpenseContainer.addView(expenseContainer)
        statsContainer.addView(incomeExpenseContainer)

        rootView.addView(statsContainer)

        // Разделитель
        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(20, 10, 20, 10)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider)

        // Заголовок списка операций
        val operationsTitle = TextView(requireContext()).apply {
            text = "Все операции"
            textSize = 16f
            setPadding(30, 20, 30, 10)
            setTextColor(Color.DKGRAY)
        }
        rootView.addView(operationsTitle)

        // Добавляем RecyclerView
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(recyclerView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as BudgetApp).repository

        setupRecyclerView()
        observeProducts()
        observeTotals()
    }

    private fun showDateSelectionDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        // Заголовок
        val title = TextView(requireContext()).apply {
            text = "Выберите период"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(title)

        // Контейнер для дат (рядом)
        val datesContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Дата ОТ
        val fromContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginEnd = 10
            }
            orientation = LinearLayout.VERTICAL
        }


        dateFromText = TextView(requireContext()).apply {
            text = if (startDate == 0L) "Не выбрано" else formatDate(startDate)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            setOnClickListener { showDatePicker(true) }

            val border = android.graphics.drawable.GradientDrawable()
            border.setStroke(1, Color.GRAY)
            border.cornerRadius = 4f
            background = border
        }
        fromContainer.addView(dateFromText)

        // Дата ДО
        val toContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginStart = 10
            }
            orientation = LinearLayout.VERTICAL
        }

        dateToText = TextView(requireContext()).apply {
            text = formatDate(endDate)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            setOnClickListener { showDatePicker(false) }

            val border = android.graphics.drawable.GradientDrawable()
            border.setStroke(1, Color.GRAY)
            border.cornerRadius = 4f
            background = border
        }
        toContainer.addView(dateToText)

        datesContainer.addView(fromContainer)
        datesContainer.addView(toContainer)
        dialogView.addView(datesContainer)

        // Контейнер для кнопок
        val buttonsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // Кнопка Сброс
        val resetButton = TextView(requireContext()).apply {
            text = "Сброс"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setOnClickListener {
                resetDateFilter()
                dateSelectionDialog?.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginEnd = 5
            }
            setBackgroundColor(Color.GRAY)
        }

        // Кнопка Отмена
        val cancelButton = TextView(requireContext()).apply {
            text = "Отмена"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setOnClickListener { dateSelectionDialog?.dismiss() }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginEnd = 5
                marginStart = 5
            }
            setBackgroundColor(Color.GRAY)
        }

        // Кнопка Применить
        val applyButton = TextView(requireContext()).apply {
            text = "Применить"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setOnClickListener {
                applyDateFilter()
                dateSelectionDialog?.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginStart = 5
            }
            setBackgroundColor(Color.GRAY)
        }

        buttonsContainer.addView(resetButton)
        buttonsContainer.addView(cancelButton)
        buttonsContainer.addView(applyButton)
        dialogView.addView(buttonsContainer)

        dateSelectionDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dateSelectionDialog?.show()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            showProductOptionsDialog(product)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun showProductOptionsDialog(product: Product) {
        val options = arrayOf("Редактировать", "Удалить", "Отмена")

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите действие")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditProductDialog(product)
                    1 -> showDeleteConfirmationDialog(product)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null)

        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)
        val editTextDate = dialogView.findViewById<EditText>(R.id.edit_text_date)

        editTextAmount.setText(product.amount.toString())
        editTextComment.setText(product.comment)

        if (product.type == "income") {
            radioGroupType.check(R.id.radio_income)
        } else {
            radioGroupType.check(R.id.radio_expense)
        }

        updateCategoriesSpinner(spinnerCategory, product.type == "income", product.category)

        selectedDate = product.date
        updateDateText(editTextDate)

        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            updateCategoriesSpinner(spinnerCategory, checkedId == R.id.radio_income, null)
        }

        editTextDate.setOnClickListener {
            showDatePicker(editTextDate)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Редактировать запись")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                updateProductFromDialog(product, dialogView)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun updateCategoriesSpinner(spinner: Spinner, isIncome: Boolean, selectedCategory: String?) {
        val categories = if (isIncome) {
            Categories.incomeCategories
        } else {
            Categories.expenseCategories
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (selectedCategory != null) {
            val position = categories.indexOf(selectedCategory)
            if (position >= 0) {
                spinner.setSelection(position)
            }
        }
    }

    private var selectedDate = System.currentTimeMillis()

    private fun showDatePicker(editTextDate: EditText) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                selectedDate = newCalendar.timeInMillis
                updateDateText(editTextDate)
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (isStartDate && startDate != 0L) startDate else endDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                if (isStartDate) {
                    startDate = newCalendar.timeInMillis
                    dateFromText.text = formatDate(startDate)
                } else {
                    endDate = newCalendar.timeInMillis
                    dateToText.text = formatDate(endDate)
                }
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun updateDateText(editTextDate: EditText) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(selectedDate))
        editTextDate.setText(dateString)
    }

    private fun updateProductFromDialog(oldProduct: Product, dialogView: View) {
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        val amountText = editTextAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (radioGroupType.checkedRadioButtonId == R.id.radio_income) "income" else "expense"
        val category = spinnerCategory.selectedItem as String
        val comment = editTextComment.text.toString()

        val updatedProduct = oldProduct.copy(
            type = type,
            category = category,
            amount = amount,
            date = selectedDate,
            comment = comment
        )

        lifecycleScope.launch {
            repository.update(updatedProduct)
            Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Вы уверены, что хотите удалить эту запись?")
            .setPositiveButton("Удалить") { dialog, _ ->
                deleteProduct(product)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            repository.delete(product)
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filteredProducts = filterProductsByDate(products)
                adapter.submitList(filteredProducts)

                if (filteredProducts.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет записей за выбранный период", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeTotals() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filteredProducts = filterProductsByDate(products)
                updateTotals(filteredProducts)
            }
        }
    }

    private fun filterProductsByDate(products: List<Product>): List<Product> {
        return if (startDate == 0L) {
            products.filter { it.date <= endDate }
        } else {
            products.filter { it.date in startDate..endDate }
        }
    }

    private fun applyDateFilter() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filteredProducts = filterProductsByDate(products)
                adapter.submitList(filteredProducts)
                updateTotals(filteredProducts)
                return@collect
            }
        }
    }

    private fun resetDateFilter() {
        startDate = 0L
        endDate = System.currentTimeMillis()

        dateFromText.text = "Не выбрано"
        dateToText.text = formatDate(endDate)

        applyDateFilter()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun updateTotals(products: List<Product>) {
        val totalIncome = products.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpenses = products.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        totalIncomeText.text = "+${String.format("%.2f", totalIncome)} ₽"
        totalExpenseText.text = "-${String.format("%.2f", totalExpenses)} ₽"
        balanceText.text = "Баланс: ${String.format("%.2f", balance)} ₽"

        if (balance >= 0) {
            balanceText.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            balanceText.setTextColor(Color.parseColor("#F44336"))
        }
    }
}