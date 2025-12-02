package com.example.myapplication1.ui.home

import Product
import ProductRepository
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.ProductAdapter
import com.example.myapplication1.R
import kotlinx.coroutines.launch
import java.util.*

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        val repo = (requireActivity().application as BudgetApp).repository
        HomeViewModelFactory(repo)
    }

    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var balanceText: TextView
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: TextView

    private var dateSelectionDialog: AlertDialog? = null
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView
    private var tempStartDate: Long = 0L
    private var tempEndDate: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return buildUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeData()
    }

    private fun buildUI(): View {
        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
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
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
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

        val searchContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        searchEditText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            hint = "Поиск по категории или комментарию..."
            setPadding(20, 15, 20, 15)
        }

        clearSearchButton = TextView(requireContext()).apply {
            text = "✕"
            textSize = 18f
            setPadding(15, 15, 15, 15)
            visibility = View.GONE
            setOnClickListener {
                searchEditText.text.clear()
                viewModel.updateSearch("")
                clearSearchButton.visibility = View.GONE
            }
        }

        searchContainer.addView(searchEditText)
        searchContainer.addView(clearSearchButton)
        rootView.addView(searchContainer)

        val statsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
        }

        balanceText = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = 15 }
            text = "Баланс: 0 ₽"
            textSize = 18f
        }
        statsContainer.addView(balanceText)

        val incomeExpenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply { setMargins(20, 10, 20, 10) }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider)

        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.recycler_view_bottom_padding))
        }
        rootView.addView(recyclerView)

        return rootView
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product -> showProductOptionsDialog(product) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.updatePadding(bottom = getBottomNavigationHeight())
    }

    private fun getBottomNavigationHeight(): Int {
        return try {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
            else (56 * resources.displayMetrics.density).toInt()
        } catch (_: Exception) {
            (56 * resources.displayMetrics.density).toInt()
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearch(s.toString().trim())
                clearSearchButton.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredProducts.collect { products ->
                    adapter.submitList(products.map { it.copy() })
                    updateTotals(products)
                }
            }
        }
    }

    private fun updateTotals(products: List<Product>) {
        val totalIncome = products.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = products.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        totalIncomeText.text = "+${String.format("%.2f", totalIncome)} ₽"
        totalExpenseText.text = "-${String.format("%.2f", totalExpense)} ₽"
        balanceText.text = "Баланс: ${String.format("%.2f", balance)} ₽"
        balanceText.setTextColor(if (balance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
    }

    private fun showProductOptionsDialog(product: Product) {
        val options = arrayOf("Редактировать", "Удалить", "Отмена")
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите действие")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditProductDialog(product)
                    1 -> viewModel.deleteProduct(product)
                }
                dialog.dismiss()
            }.show()
    }

    private fun showEditProductDialog(product: Product) {
        // Реализация редактирования продукта
    }

    // -------------------- Фильтр по датам --------------------
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

        tempStartDate = viewModel.products.value.minOfOrNull { it.date } ?: 0L
        tempEndDate = viewModel.products.value.maxOfOrNull { it.date } ?: System.currentTimeMillis()

        dateFromText = createDateTextView(tempStartDate)
        dateToText = createDateTextView(tempEndDate)

        dateFromText.setOnClickListener { showDatePicker(true) }
        dateToText.setOnClickListener { showDatePicker(false) }

        val datesContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        datesContainer.addView(dateFromText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 10 })
        datesContainer.addView(dateToText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = 10 })
        dialogView.addView(datesContainer)

        val buttonsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 20, 0, 0) }

        val resetButton = createDialogButton("Сброс") {
            viewModel.resetDateFilter()
            dateFromText.text = "Не выбрано"
            dateToText.text = viewModel.formatDate(System.currentTimeMillis())
            dateSelectionDialog?.dismiss()
        }

        val cancelButton = createDialogButton("Отмена") { dateSelectionDialog?.dismiss() }

        val applyButton = createDialogButton("Применить") {
            viewModel.updateStartDate(tempStartDate)
            viewModel.updateEndDate(tempEndDate)
            dateSelectionDialog?.dismiss()
        }

        buttonsContainer.addView(resetButton)
        buttonsContainer.addView(cancelButton)
        buttonsContainer.addView(applyButton)
        dialogView.addView(buttonsContainer)

        dateSelectionDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dateSelectionDialog?.show()
    }

    private fun createDateTextView(initial: Long): TextView {
        return TextView(requireContext()).apply {
            text = if (initial == 0L) "Не выбрано" else viewModel.formatDate(initial)
            textSize = 16f
            setPadding(10, 10, 10, 10)
            background = android.graphics.drawable.GradientDrawable().apply { setStroke(1, Color.GRAY); cornerRadius = 4f }
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val current = if (isStart) tempStartDate else tempEndDate
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val newTime = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
            if (isStart) {
                tempStartDate = newTime
                dateFromText.text = viewModel.formatDate(newTime)
            } else {
                tempEndDate = newTime
                dateToText.text = viewModel.formatDate(newTime)
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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
}
