package com.example.myapplication1.ui.notifications

import ProductRepository
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.Category
import com.example.myapplication1.CategoryRepository
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private lateinit var repository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(repository, categoryRepository)
    }

    private lateinit var incomeContainer: LinearLayout
    private lateinit var expenseContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }

        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(requireContext()).apply {
            text = "Категории"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        val addCategoryButton = TextView(requireContext()).apply {
            text = "+ Добавить новую категорию"
            textSize = 16f
            gravity = Gravity.CENTER

            setPadding(0, 20, 0, 20)
            setOnClickListener { showAddCategoryDialog() }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(30, 0, 30, 20) }

            val border = android.graphics.drawable.GradientDrawable()
            border.cornerRadius = 8f
            background = border
        }
        rootView.addView(addCategoryButton)

        // Контейнер для доходов
        incomeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
        }
        val incomeTitle = TextView(requireContext()).apply {
            text = "Категории доходов"
            textSize = 18f
            setTextColor(Color.parseColor("#4CAF50"))
            setPadding(0, 0, 0, 15)
        }
        incomeContainer.addView(incomeTitle)

        // Контейнер для расходов
        expenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setPadding(30, 0, 30, 20) }
            orientation = LinearLayout.VERTICAL
        }
        val expenseTitle = TextView(requireContext()).apply {
            text = "Категории расходов"
            textSize = 18f
            setTextColor(Color.parseColor("#F44336"))
            setPadding(0, 0, 0, 15)
        }
        expenseContainer.addView(expenseTitle)

        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply { setMargins(20, 10, 20, 10) }
            setBackgroundColor(Color.LTGRAY)
        }

        rootView.addView(incomeContainer)
        rootView.addView(divider)
        rootView.addView(expenseContainer)

        // Заглушка / поле под категориями
        val placeholder = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                450 // высота заглушки в пикселях
            )
        }
        rootView.addView(placeholder)

        scrollView.addView(rootView)
        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = (requireActivity().application as BudgetApp).repository
        categoryRepository = CategoryRepository()

        lifecycleScope.launch {
            val categories = viewModel.getAllCategories()
            displayCategories(categories)
        }
    }

    private fun displayCategories(categories: List<Category>) {
        if (incomeContainer.childCount > 1) {
            incomeContainer.removeViews(1, incomeContainer.childCount - 1)
        }
        if (expenseContainer.childCount > 1) {
            expenseContainer.removeViews(1, expenseContainer.childCount - 1)
        }

        categories.filter { it.type == "income" }.forEach {
            incomeContainer.addView(createCategoryView(it))
        }
        categories.filter { it.type == "expense" }.forEach {
            expenseContainer.addView(createCategoryView(it))
        }
    }

    private fun createCategoryView(category: Category): View {
        val layout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 10 }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 15, 20, 15)
        }

        val nameView = TextView(requireContext()).apply {
            text = category.name
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
        }
        layout.addView(nameView)

        val editButton = TextView(requireContext()).apply {
            text = "✏️"
            textSize = 16f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showEditCategoryDialog(category) }
        }
        layout.addView(editButton)

        val deleteButton = TextView(requireContext()).apply {
            text = "🗑️"
            textSize = 16f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showDeleteCategoryDialog(category) }
        }
        layout.addView(deleteButton)

        return layout
    }

    private fun showAddCategoryDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val title = TextView(requireContext()).apply {
            text = "Добавить новую категорию"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(title)

        val radioGroup = RadioGroup(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        val incomeRadioId = View.generateViewId()
        val expenseRadioId = View.generateViewId()
        val incomeRadio = RadioButton(requireContext()).apply { id = incomeRadioId; text = "Доход"; isChecked = true }
        val expenseRadio = RadioButton(requireContext()).apply { id = expenseRadioId; text = "Расход" }
        radioGroup.addView(incomeRadio)
        radioGroup.addView(expenseRadio)
        dialogView.addView(radioGroup)

        val editText = EditText(requireContext()).apply {
            hint = "Введите название категории"
            textSize = 16f
            setPadding(20, 15, 20, 15)
        }
        dialogView.addView(editText)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialog, _ ->
                val name = editText.text.toString().trim()
                val type = when (radioGroup.checkedRadioButtonId) {
                    incomeRadioId -> "income"
                    expenseRadioId -> "expense"
                    else -> "income"
                }

                if (name.isNotEmpty()) {
                    viewModel.addCategory(Category(name = name, type = type)) {
                        Toast.makeText(requireContext(), "Категория добавлена: $name", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val editText = EditText(requireContext()).apply {
            setText(category.name)
            textSize = 16f
            setPadding(20, 15, 20, 15)
        }
        dialogView.addView(editText)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateCategory(category.copy(name = newName)) {
                        Toast.makeText(requireContext(), "Категория обновлена: $newName", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteCategoryDialog(category: Category) {
        lifecycleScope.launch {
            val message = "Удалить категорию \"${category.name}\"?"
            AlertDialog.Builder(requireContext())
                .setTitle("Удаление категории")
                .setMessage(message)
                .setPositiveButton("Удалить") { dialog, _ ->
                    viewModel.deleteCategory(category) {
                        Toast.makeText(requireContext(), "Категория удалена: ${category.name}", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}
