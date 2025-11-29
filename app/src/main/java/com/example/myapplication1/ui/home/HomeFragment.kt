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
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
    private var dateSelectionDialog: AlertDialog? = null
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView

    private var selectedDate = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            // Добавляем отступ снизу для предотвращения перекрытия bottom navigation
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height))
        }

        val title = TextView(requireContext()).apply {
            text = "Главная"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        val statsHeaderContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 10, 30, 10) }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val statsTitle = TextView(requireContext()).apply {
            text = "Общие доходы/расходы"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
        }

        val calendarIcon = TextView(requireContext()).apply {
            text = "📅"
            textSize = 20f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showDateSelectionDialog() }
            gravity = Gravity.CENTER
        }

        statsHeaderContainer.addView(statsTitle)
        statsHeaderContainer.addView(calendarIcon)
        rootView.addView(statsHeaderContainer)

        val statsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
        }

        balanceText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
            text = "Баланс: 0 ₽"
            textSize = 18f
        }
        statsContainer.addView(balanceText)

        val incomeExpenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val incomeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
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

        val expenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
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

        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(20, 10, 20, 10)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider)

        val operationsTitle = TextView(requireContext()).apply {
            text = "Все операции"
            textSize = 16f
            setPadding(30, 20, 30, 10)
            setTextColor(Color.DKGRAY)
        }
        rootView.addView(operationsTitle)

        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            // Добавляем отступ снизу для RecyclerView
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.recycler_view_bottom_padding))
        }
        rootView.addView(recyclerView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as BudgetApp).repository

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product -> showProductOptionsDialog(product) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Альтернативный способ: добавляем отступ для RecyclerView
        recyclerView.updatePadding(bottom = getBottomNavigationHeight())
    }

    private fun getBottomNavigationHeight(): Int {
        return try {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else {
                // Стандартная высота bottom navigation (примерно 56dp)
                (56 * resources.displayMetrics.density).toInt()
            }
        } catch (e: Exception) {
            // Fallback значение
            (56 * resources.displayMetrics.density).toInt()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allProducts.collect { products ->
                    // Создаём новую копию списка, чтобы DiffUtil корректно обработал изменения
                    val filtered = filterProductsByDate(products)
                    adapter.submitList(filtered.map { it.copy() })  // copy() гарантирует новый объект
                    updateTotals(filtered)
                }
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
        // Обновляем данные с учетом выбранного периода
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filtered = filterProductsByDate(products)
                adapter.submitList(filtered.map { it.copy() })
                updateTotals(filtered)
            }
        }
    }

    private fun resetDateFilter() {
        startDate = 0L
        endDate = System.currentTimeMillis()

        dateFromText?.let { it.text = "Не выбрано" }
        dateToText?.let { it.text = formatDate(endDate) }

        // Применяем сброс фильтра
        applyDateFilter()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
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

        if (product.type == "income") radioGroupType.check(R.id.radio_income)
        else radioGroupType.check(R.id.radio_expense)

        updateCategoriesSpinner(spinnerCategory, product.type == "income", product.category)

        selectedDate = product.date
        updateDateText(editTextDate)

        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            updateCategoriesSpinner(spinnerCategory, checkedId == R.id.radio_income, null)
        }

        editTextDate.setOnClickListener { showDatePicker(editTextDate) }

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать запись")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                updateProductFromDialog(product, dialogView)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateCategoriesSpinner(spinner: Spinner, isIncome: Boolean, selectedCategory: String?) {
        val categories = if (isIncome) Categories.incomeCategories else Categories.expenseCategories

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (selectedCategory != null) {
            val index = categories.indexOf(selectedCategory)
            if (index >= 0) spinner.setSelection(index)
        }
    }

    private fun showDatePicker(editTextDate: EditText) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }

        DatePickerDialog(requireContext(),
            { _, y, m, d ->
                selectedDate = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
                updateDateText(editTextDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText(editTextDate: EditText) {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        editTextDate.setText(format.format(Date(selectedDate)))
    }

    private fun updateProductFromDialog(oldProduct: Product, dialogView: View) {
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val typeGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val amountField = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val commentField = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        val amount = amountField.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (typeGroup.checkedRadioButtonId == R.id.radio_income) "income" else "expense"
        val category = spinner.selectedItem as String
        val comment = commentField.text.toString()

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
            .setMessage("Удалить запись?")
            .setPositiveButton("Удалить") { d, _ ->
                deleteProduct(product); d.dismiss()
            }
            .setNegativeButton("Отмена") { d, _ -> d.dismiss() }
            .show()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            repository.delete(product)
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateSelectionDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val title = TextView(requireContext()).apply {
            text = "Выберите период"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(title)

        val datesContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val fromContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
                marginEnd = 10
            }
        }

        dateFromText = TextView(requireContext()).apply {
            text = formatDate(startDate)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            background = android.graphics.drawable.GradientDrawable().apply {
                setStroke(1, Color.GRAY); cornerRadius = 4f
            }
            setOnClickListener { showDatePicker(true) }
        }

        fromContainer.addView(dateFromText)

        val toContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
                marginStart = 10
            }
            orientation = LinearLayout.VERTICAL
        }

        dateToText = TextView(requireContext()).apply {
            text = formatDate(endDate)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            background = android.graphics.drawable.GradientDrawable().apply {
                setStroke(1, Color.GRAY); cornerRadius = 4f
            }
            setOnClickListener { showDatePicker(false) }
        }

        toContainer.addView(dateToText)

        datesContainer.addView(fromContainer)
        datesContainer.addView(toContainer)
        dialogView.addView(datesContainer)

        val buttonsContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }

        val resetButton = TextView(requireContext()).apply {
            text = "Сброс"
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setBackgroundColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f; marginEnd = 5
            }
            setOnClickListener {
                resetDateFilter()
                dateSelectionDialog?.dismiss()
            }
        }

        val cancelButton = TextView(requireContext()).apply {
            text = "Отмена"
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setBackgroundColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f; marginStart = 5; marginEnd = 5
            }
            setOnClickListener { dateSelectionDialog?.dismiss() }
        }

        val applyButton = TextView(requireContext()).apply {
            text = "Применить"
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setBackgroundColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f; marginStart = 5
            }
            setOnClickListener {
                applyDateFilter()
                dateSelectionDialog?.dismiss()
            }
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

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (isStart && startDate != 0L) startDate else endDate
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val newDate = Calendar.getInstance().apply { set(year, month, day) }.timeInMillis

                if (isStart) {
                    startDate = newDate
                    dateFromText.text = formatDate(startDate)
                } else {
                    endDate = newDate
                    dateToText.text = formatDate(endDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTotals(products: List<Product>) {
        val totalIncome = products.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = products.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        totalIncomeText.text = "+${String.format("%.2f", totalIncome)} ₽"
        totalExpenseText.text = "-${String.format("%.2f", totalExpense)} ₽"
        balanceText.text = "Баланс: ${String.format("%.2f", balance)} ₽"

        balanceText.setTextColor(
            if (balance >= 0) Color.parseColor("#4CAF50")
            else Color.parseColor("#F44336")
        )
    }
}