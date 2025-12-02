package com.example.myapplication1.ui.notifications

import Product
import ProductRepository
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.Category
import com.example.myapplication1.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private lateinit var repository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository

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
            isFillViewport = false
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
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        val addCategoryButton = TextView(requireContext()).apply {
            text = "+ Добавить новую категорию"
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF9800"))
            setPadding(0, 20, 0, 20)
            setOnClickListener { showAddCategoryDialog() }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(30, 0, 30, 20)
            }

            val border = android.graphics.drawable.GradientDrawable()
            border.cornerRadius = 8f
            background = border
        }
        rootView.addView(addCategoryButton)

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

        rootView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

// Позволяем ScrollView корректно измерять содержимое
        scrollView.isFillViewport = true

// Добавляем rootView как единственного ребёнка ScrollView
        scrollView.addView(rootView)

        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = (requireActivity().application as BudgetApp).repository
        categoryRepository = CategoryRepository()
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = categoryRepository.getAllCategories()

            if (incomeContainer.childCount > 1) {
                val count = incomeContainer.childCount - 1
                incomeContainer.removeViews(1, count)
            }
            if (expenseContainer.childCount > 1) {
                val count = expenseContainer.childCount - 1
                expenseContainer.removeViews(1, count)
            }

            categories.filter { it.type == "income" }.forEach {
                incomeContainer.addView(createCategoryView(it))
            }
            categories.filter { it.type == "expense" }.forEach {
                expenseContainer.addView(createCategoryView(it))
            }
        }
    }

    private fun createCategoryView(category: Category): View {
        val layout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 10 }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
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
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(title)

        val radioGroup = RadioGroup(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val incomeRadioId = View.generateViewId()
        val expenseRadioId = View.generateViewId()

        val incomeRadio = RadioButton(requireContext()).apply {
            id = incomeRadioId
            text = "Доход"
            isChecked = true
        }
        val expenseRadio = RadioButton(requireContext()).apply {
            id = expenseRadioId
            text = "Расход"
        }

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
                    addCategory(Category(name = name, type = type))
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
                    editCategory(category.copy(name = newName))
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
            val products = getProductsByCategory(category)
            val message = if (products.isNotEmpty()) {
                "Удалить категорию \"${category.name}\"?\nУдалится ${products.size} записей."
            } else {
                "Удалить категорию \"${category.name}\"?"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Удаление категории")
                .setMessage(message)
                .setPositiveButton("Удалить") { dialog, _ ->
                    deleteCategory(category)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun addCategory(category: Category) {
        lifecycleScope.launch {
            categoryRepository.addCategory(category)
            Toast.makeText(requireContext(), "Категория добавлена: ${category.name}", Toast.LENGTH_SHORT).show()
            loadCategories()
        }
    }

    private fun editCategory(category: Category) {
        lifecycleScope.launch {
            categoryRepository.updateCategory(category)

            val products = getProductsByCategory(category)
            products.forEach {
                repository.update(it.copy(category = category.name))
            }

            Toast.makeText(requireContext(), "Категория обновлена: ${category.name}", Toast.LENGTH_SHORT).show()
            loadCategories()
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            categoryRepository.deleteCategory(category.id)

            val products = getProductsByCategory(category)
            products.forEach { repository.delete(it) }

            Toast.makeText(requireContext(), "Категория удалена: ${category.name}", Toast.LENGTH_SHORT).show()
            loadCategories()
        }
    }

    private suspend fun getProductsByCategory(category: Category): List<Product> {
        val allProducts = repository.allProducts.first()
        return allProducts.filter {
            it.category == category.name && it.type == category.type
        }
    }
}
