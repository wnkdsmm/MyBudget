package com.example.myapplication1.ui.home

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
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.CategoryRepository
import com.example.myapplication1.Product
import com.example.myapplication1.ProductAdapter
import com.example.myapplication1.R
import com.example.myapplication1.ui.notifications.NotificationsViewModel
import com.example.myapplication1.ui.notifications.NotificationsViewModelFactory
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.*

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        val repo = (requireActivity().application as BudgetApp).repository
        HomeViewModelFactory(repo)
    }
    private lateinit var categoryRepository: CategoryRepository
    private val categoryViewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(
            (requireActivity().application as BudgetApp).repository,
            CategoryRepository()
        )
    }
    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var balanceText: TextView
    private lateinit var searchTextInputLayout: TextInputLayout
    private lateinit var searchEditText: EditText
    private lateinit var refreshButton: Button

    private var dateSelectionDialog: AlertDialog? = null
    private lateinit var dateFromText: TextView
    private lateinit var dateToText: TextView
    private var tempStartDate: Long = 0L
    private var tempEndDate: Long = System.currentTimeMillis()
    private var activeRefreshNotification: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return buildUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupRefreshButton()
        observeData()

        // Назначаем обработчик кликов для корневого View
        view.setOnClickListener {
            hideKeyboardAndClearFocus()
        }
    }

    private fun buildUI(): View {
        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height))
            // Делаем корневой View кликабельным, но не перехватывающим дочерние клики
            setOnClickListener {
                // Обработка клика по фону (вне полей ввода)
            }
            isClickable = true
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

        // Поиск в стиле TextInputLayout
        searchTextInputLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(30, 0, 30, 20)
            }
            hint = "Поиск по категории или комментарию..."
            isHintEnabled = true
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            endIconDrawable = context.getDrawable(com.google.android.material.R.drawable.mtrl_ic_cancel)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setEndIconTintList(null) // Оставляем стандартный цвет иконки

            // Предотвращаем закрытие клавиатуры при клике на TextInputLayout
            setOnClickListener {
                searchEditText.requestFocus()
                showKeyboard(searchEditText)
            }
        }

        searchEditText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Настраиваем обработку фокуса
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    hideKeyboard(this)
                }
            }
        }

        searchTextInputLayout.addView(searchEditText)
        rootView.addView(searchTextInputLayout)

        val statsContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
            // Не перехватываем клики - пусть идут к родителю
            isClickable = false
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

        refreshButton = Button(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
            text = "🔄 Обновить список записей"
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            elevation = 4f
        }
        statsContainer.addView(refreshButton)

        rootView.addView(statsContainer)

        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply { setMargins(20, 10, 20, 10) }
            setBackgroundColor(Color.LTGRAY)
        }
        rootView.addView(divider)

        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.recycler_view_bottom_padding))

            // Обработчик кликов для RecyclerView
            addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                    // При касании RecyclerView скрываем клавиатуру
                    hideKeyboardAndClearFocus()
                    return false // Позволяем обрабатывать клики дальше
                }
            })
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

    private fun setupSearch() {
        // Обработка ввода текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearch(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка клика по иконке очистки
        searchTextInputLayout.setEndIconOnClickListener {
            searchEditText.text?.clear()
            viewModel.updateSearch("")
            searchEditText.requestFocus()
            showKeyboard(searchEditText)
        }
    }

    private fun setupRefreshButton() {
        refreshButton.setOnClickListener {
            hideKeyboardAndClearFocus()
            refreshData()
        }
    }

    private fun refreshData() {
        // Сброс поиска
        searchEditText.text.clear()
        viewModel.updateSearch("")

        // Сброс фильтра по датам
        viewModel.resetDateFilter()

        // Обновление данных в адаптере
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredProducts.collect { products ->
                adapter.submitList(products.map { it.copy() })
                updateTotals(products)

                // Показать Toast уведомление
                Toast.makeText(requireContext(), "Список обновлён", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboardAndClearFocus() {
        // Снимаем фокус с поля поиска
        searchEditText.clearFocus()

        // Скрываем клавиатуру
        val inputMethodManager = ContextCompat.getSystemService(
            requireContext(),
            InputMethodManager::class.java
        )
        inputMethodManager?.hideSoftInputFromWindow(
            searchEditText.windowToken,
            0
        )
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = ContextCompat.getSystemService(
            requireContext(),
            InputMethodManager::class.java
        )
        inputMethodManager?.hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }

    private fun showKeyboard(view: View) {
        val inputMethodManager = ContextCompat.getSystemService(
            requireContext(),
            InputMethodManager::class.java
        )
        inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showRefreshNotification() {
        val root = requireView() as LinearLayout

        // Удаляем предыдущее уведомление, если оно существует
        activeRefreshNotification?.let { root.removeView(it) }

        val notification = TextView(requireContext()).apply {
            text = "Список обновлён"
            gravity = Gravity.CENTER
            setPadding(10, 5, 10, 5)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(30, 10, 30, 10)
        }

        // Добавляем уведомление после заголовка и статистики
        val insertIndex = 3 // 0 title, 1 header, 2 stats, 3 divider — после stats
        root.addView(notification, insertIndex, layoutParams)

        // Запоминаем текущее уведомление
        activeRefreshNotification = notification

        notification.postDelayed({
            root.removeView(notification)
            if (activeRefreshNotification === notification) {
                activeRefreshNotification = null
            }
        }, 500)
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
        hideKeyboardAndClearFocus()
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
        hideKeyboardAndClearFocus()
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val amountEdit = EditText(requireContext()).apply {
            hint = "Сумма"
            setText(product.amount.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val categorySpinner = Spinner(requireContext())

        val commentEdit = EditText(requireContext()).apply {
            hint = "Комментарий"
            setText(product.comment)
        }

        dialogView.addView(amountEdit)
        dialogView.addView(categorySpinner)
        dialogView.addView(commentEdit)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Редактировать запись")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null) // обработаем вручную
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {

            // Подписка на категории
            viewLifecycleOwner.lifecycleScope.launch {
                categoryViewModel.categories.collect { categories ->

                    // Фильтруем категории по типу записи (income/expense)
                    val filtered = categories.filter { it.type == product.type }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        filtered.map { it.name }.sorted()
                    )


                    categorySpinner.adapter = adapter

                    // Устанавливаем текущую категорию
                    val index = filtered.indexOfFirst { it.name == product.category }
                    categorySpinner.setSelection(if (index >= 0) index else 0)
                }
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val filteredCategories =
                    categoryViewModel.categories.value.filter { it.type == product.type }

                if (filteredCategories.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет категорий для данного типа", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newCategory = filteredCategories[categorySpinner.selectedItemPosition].name

                val updated = product.copy(
                    amount = amountEdit.text.toString().toDoubleOrNull() ?: product.amount,
                    category = newCategory,
                    comment = commentEdit.text.toString()
                )

                viewModel.updateProduct(updated)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // -------------------- Фильтр по датам --------------------
    private fun showDateSelectionDialog() {
        hideKeyboardAndClearFocus()
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

        tempStartDate = viewModel.filteredProducts.value.minOfOrNull { it.date } ?: 0L
        tempEndDate = viewModel.filteredProducts.value.maxOfOrNull { it.date } ?: System.currentTimeMillis()

        dateFromText = createDateTextView(tempStartDate)
        dateToText = createDateTextView(tempEndDate)

        dateFromText.setOnClickListener { showDatePicker(true) }
        dateToText.setOnClickListener { showDatePicker(false) }

        val datesContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        datesContainer.addView(dateFromText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 10 })
        datesContainer.addView(dateToText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = 10 })
        dialogView.addView(datesContainer)

        val buttonsContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }

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
            background = android.graphics.drawable.GradientDrawable().apply {
                setStroke(1, Color.GRAY)
                cornerRadius = 4f
            }
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
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(5, 0, 5, 0)
            }
            setBackgroundColor(Color.GRAY)
        }
    }
}