package com.example.myapplication1.ui.dashboard

import Product
import ProductRepository
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.BudgetApp
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var repository: ProductRepository
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var balanceText: TextView
    private lateinit var incomePieChart: PieChart
    private lateinit var expensePieChart: PieChart
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView

    private var startDate: Long = 0L
    private var endDate: Long = System.currentTimeMillis()

    private var dateSelectionDialog: android.app.AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scrollView = android.widget.ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Добавляем заголовок
        val title = TextView(requireContext()).apply {
            text = "Статистика"
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
        val divider1 = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(20, 10, 20, 10)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider1)

        // Диаграмма доходов
        val incomeChartTitle = TextView(requireContext()).apply {
            text = "Доходы по категориям"
            textSize = 16f
            setPadding(30, 20, 30, 10)
            gravity = android.view.Gravity.CENTER
        }
        rootView.addView(incomeChartTitle)

        incomePieChart = PieChart(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                550
            ).apply {
                setMargins(20, 10, 20, 20)
            }
        }
        rootView.addView(incomePieChart)

        // Разделитель
        val divider2 = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(20, 10, 20, 10)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider2)

        // Диаграмма расходов
        val expenseChartTitle = TextView(requireContext()).apply {
            text = "Расходы по категориям"
            textSize = 16f
            setPadding(30, 20, 30, 20)
            gravity = android.view.Gravity.CENTER
        }
        rootView.addView(expenseChartTitle)

        expensePieChart = PieChart(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                550
            ).apply {
                setMargins(20, 10, 20, 20)
            }
        }
        rootView.addView(expensePieChart)

        setupPieCharts()

        scrollView.addView(rootView)
        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as BudgetApp).repository
        observeData()
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

            // Простая рамка
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

            // Простая рамка
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

            // Простой стиль кнопки
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

            // Простой стиль кнопки
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

            // Простой стиль кнопки
            setBackgroundColor(Color.GRAY)
        }

        buttonsContainer.addView(resetButton)
        buttonsContainer.addView(cancelButton)
        buttonsContainer.addView(applyButton)
        dialogView.addView(buttonsContainer)

        dateSelectionDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dateSelectionDialog?.show()
    }

    private fun setupPieCharts() {
        setupPieChart(incomePieChart)
        setupPieChart(expensePieChart)
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.transparentCircleRadius = 25f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setCenterTextColor(getColorFromTheme(android.R.attr.textColorPrimary))
        pieChart.setDrawCenterText(true)
        pieChart.animateY(1000)
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = getColorFromTheme(android.R.attr.textColorPrimary)
        pieChart.legend.textSize = 14f
        pieChart.legend.formSize = 14f
        pieChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        pieChart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
        pieChart.setDrawEntryLabels(false)
    }

    private fun getColorFromTheme(attr: Int): Int {
        val typedArray = requireContext().theme.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.WHITE)
        typedArray.recycle()
        return color
    }

    private fun observeData() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filteredProducts = filterProductsByDate(products)
                updateTotals(filteredProducts)
                updateCharts(filteredProducts)
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

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (isStartDate && startDate != 0L) startDate else endDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(
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

    private fun applyDateFilter() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                val filteredProducts = filterProductsByDate(products)
                updateTotals(filteredProducts)
                updateCharts(filteredProducts)
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

    private fun updateCharts(products: List<Product>) {
        updateIncomeChart(products)
        updateExpenseChart(products)
    }

    private fun updateIncomeChart(products: List<Product>) {
        val incomeProducts = products.filter { it.type == "income" }

        if (incomeProducts.isEmpty()) {
            incomePieChart.centerText = "Нет данных\nпо доходам"
            incomePieChart.data = null
            incomePieChart.invalidate()
            return
        }

        val categorySums = incomeProducts.groupBy { it.category }
            .mapValues { (_, products) -> products.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val entries = categorySums.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = getColors(categorySums.size)
            valueTextSize = 12f
            setDrawValues(true)
            valueTextColor = getColorFromTheme(android.R.attr.textColorPrimary)
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${String.format("%.2f", value)}%"
                }
            })
        }

        incomePieChart.data = pieData
        incomePieChart.centerText = "Доходы\n${String.format("%.2f", categorySums.sumOf { it.second })} ₽"
        incomePieChart.invalidate()
    }

    private fun updateExpenseChart(products: List<Product>) {
        val expenseProducts = products.filter { it.type == "expense" }

        if (expenseProducts.isEmpty()) {
            expensePieChart.centerText = "Нет данных\nпо расходам"
            expensePieChart.data = null
            expensePieChart.invalidate()
            return
        }

        val categorySums = expenseProducts.groupBy { it.category }
            .mapValues { (_, products) -> products.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val entries = categorySums.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = getColors(categorySums.size)
            valueTextSize = 12f
            setDrawValues(true)
            valueTextColor = getColorFromTheme(android.R.attr.textColorPrimary)
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${String.format("%.2f", value)}%"
                }
            })
        }

        expensePieChart.data = pieData
        expensePieChart.centerText = "Расходы\n${String.format("%.2f", categorySums.sumOf { it.second })} ₽"
        expensePieChart.invalidate()
    }

    private fun getColors(size: Int): List<Int> {
        return listOf(
            Color.parseColor("#FF6384"),
            Color.parseColor("#36A2EB"),
            Color.parseColor("#FFCE56"),
            Color.parseColor("#4BC0C0"),
            Color.parseColor("#9966FF"),
            Color.parseColor("#FF9F40"),
            Color.parseColor("#8AC926"),
            Color.parseColor("#1982C4"),
            Color.parseColor("#6A4C93"),
            Color.parseColor("#F15BB5")
        ).take(size)
    }
}