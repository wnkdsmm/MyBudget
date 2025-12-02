package com.example.myapplication1.ui.dashboard

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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.BudgetApp
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var balanceText: TextView
    private lateinit var incomePieChart: PieChart
    private lateinit var expensePieChart: PieChart
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView
    private var dateSelectionDialog: AlertDialog? = null

    private lateinit var viewModel: DashboardViewModel

    // Временные даты для выбора пользователем
    private var tempStartDate: Long = 0L
    private var tempEndDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val scrollView = android.widget.ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(requireContext()).apply {
            text = "Статистика"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        setupStatsHeader(rootView)
        setupStatsContainer(rootView)
        setupPieChartsContainer(rootView)

        scrollView.addView(rootView)
        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository: ProductRepository = (requireActivity().application as BudgetApp).repository
        viewModel = ViewModelProvider(this, DashboardViewModelFactory(repository))
            .get(DashboardViewModel::class.java)
        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.products.collectLatest { products ->
                updateTotals(products)
                updateCharts(products)
            }
        }
    }

    private fun setupStatsHeader(rootView: LinearLayout) {
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
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { weight = 1f }
        }
        statsHeaderContainer.addView(statsTitle)

        val calendarIcon = TextView(requireContext()).apply {
            text = "📅"
            textSize = 20f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showDateSelectionDialog() }
            gravity = Gravity.CENTER
        }
        statsHeaderContainer.addView(calendarIcon)
        rootView.addView(statsHeaderContainer)
    }

    private fun setupStatsContainer(rootView: LinearLayout) {
        val statsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
        }

        balanceText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
            text = "Баланс: 0 ₽"
            textSize = 18f
        }
        statsContainer.addView(balanceText)

        val incomeExpenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }
        setupIncomeExpenseContainers(incomeExpenseContainer)
        statsContainer.addView(incomeExpenseContainer)
        rootView.addView(statsContainer)
    }

    private fun setupIncomeExpenseContainers(container: LinearLayout) {
        val incomeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { weight = 1f }
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
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { weight = 1f }
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

        container.addView(incomeContainer)
        container.addView(expenseContainer)
    }

    private fun setupPieChartsContainer(rootView: LinearLayout) {
        val chartHeight = (250 * resources.displayMetrics.density).toInt()

        incomePieChart = PieChart(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, chartHeight
            ).apply { setMargins(20, 20, 20, 20) }
        }
        setupPieChart(incomePieChart)
        rootView.addView(incomePieChart)

        expensePieChart = PieChart(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, chartHeight
            ).apply { setMargins(20, 20, 20, 20) }
        }
        setupPieChart(expensePieChart)
        rootView.addView(expensePieChart)
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.transparentCircleRadius = 25f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setCenterTextColor(getColorFromTheme(android.R.attr.textColorPrimary))
        pieChart.setDrawCenterText(true)
        pieChart.animateY(1000)

        // Легенда: справа сверху, вертикально (как изначально)
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = getColorFromTheme(android.R.attr.textColorPrimary)
        pieChart.legend.textSize = 14f
        pieChart.legend.formSize = 14f
        pieChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        pieChart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
        pieChart.legend.setDrawInside(false)

        pieChart.setDrawEntryLabels(false)
    }
    private fun getColorFromTheme(attr: Int): Int {
        val typedArray = requireContext().theme.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.WHITE)
        typedArray.recycle()
        return color
    }

    private fun updateTotals(products: List<Product>) {
        val (totalIncome, totalExpenses, balance) = viewModel.calculateTotals(products)
        totalIncomeText.text = "+${String.format("%.2f", totalIncome)} ₽"
        totalExpenseText.text = "-${String.format("%.2f", totalExpenses)} ₽"
        balanceText.text = "Баланс: ${String.format("%.2f", balance)} ₽"
        balanceText.setTextColor(if (balance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
    }

    private fun updateCharts(products: List<Product>) {
        updatePieChart(incomePieChart, products.filter { it.type == "income" }, "Доходы")
        updatePieChart(expensePieChart, products.filter { it.type == "expense" }, "Расходы")
    }

    private fun updatePieChart(chart: PieChart, products: List<Product>, title: String) {
        if (products.isEmpty()) {
            chart.data = null
            chart.centerText = "Нет данных\nпо ${title.lowercase()}"
            chart.invalidate()
            return
        }

        val categorySums = viewModel.groupByCategory(products)
        val entries = categorySums.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#FF6384"),
                Color.parseColor("#36A2EB"),
                Color.parseColor("#FFCE56"),
                Color.parseColor("#4BC0C0"),
                Color.parseColor("#9966FF"),
                Color.parseColor("#FF9F40"),
                Color.parseColor("#8AC926"),
                Color.parseColor("#1982C4")
            ).take(entries.size)
            valueTextSize = 12f
            setDrawValues(true)
            valueTextColor = Color.BLACK
        }

        chart.data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    "${String.format("%.2f", value)}%"
            })
        }

        chart.centerText = "$title\n${String.format("%.2f", categorySums.values.sum())} ₽"
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun showDateSelectionDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
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

        // инициализация временных дат
        tempStartDate = viewModel.products.value.minOfOrNull { it.date } ?: 0L
        tempEndDate = viewModel.products.value.maxOfOrNull { it.date } ?: System.currentTimeMillis()

        dateFromText = createDateTextView(tempStartDate)
        dateToText = createDateTextView(tempEndDate)

        dateFromText.setOnClickListener { showDatePicker(true) }
        dateToText.setOnClickListener { showDatePicker(false) }

        val datesContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        datesContainer.addView(dateFromText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 10 })
        datesContainer.addView(dateToText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = 10 })
        dialogView.addView(datesContainer)

        val buttonsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
            orientation = LinearLayout.HORIZONTAL
        }

        val resetButton = createDialogButton("Сброс") {
            viewModel.resetDates()
            dateFromText.text = "Не выбрано"
            dateToText.text = formatDate(System.currentTimeMillis())
            dateSelectionDialog?.dismiss()
        }
        val cancelButton = createDialogButton("Отмена") { dateSelectionDialog?.dismiss() }
        val applyButton = createDialogButton("Применить") {
            viewModel.setStartDate(tempStartDate)
            viewModel.setEndDate(tempEndDate)
            dateSelectionDialog?.dismiss()
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

    private fun createDateTextView(initial: Long): TextView {
        return TextView(requireContext()).apply {
            text = if (initial == 0L) "Не выбрано" else formatDate(initial)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            background = android.graphics.drawable.GradientDrawable().apply {
                setStroke(1, Color.GRAY)
                cornerRadius = 4f
            }
        }
    }

    private fun createDialogButton(text: String, action: () -> Unit): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(5,0,5,0) }
            setBackgroundColor(Color.GRAY)
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val current = if (isStart) tempStartDate else tempEndDate
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val newTime = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
            if (isStart) {
                tempStartDate = newTime
                dateFromText.text = formatDate(newTime)
            } else {
                tempEndDate = newTime
                dateToText.text = formatDate(newTime)
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
