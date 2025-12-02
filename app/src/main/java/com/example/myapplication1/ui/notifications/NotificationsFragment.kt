package com.example.myapplication1.ui.notifications

import Product
import ProductRepository
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsFragment : Fragment() {

    private lateinit var repository: ProductRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Создаем ScrollView как корневой элемент
        val scrollView = ScrollView(requireContext()).apply {
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
            text = "Категории"
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 30)
        }
        rootView.addView(title)

        // Контейнер для кнопок импорта/экспорта
        val importExportContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(30, 0, 30, 20)
            }
            orientation = LinearLayout.HORIZONTAL
        }



        // Кнопка добавления новой категории
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

        // Контейнер для категорий доходов
        val incomeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(30, 0, 30, 20)
            }
            orientation = LinearLayout.VERTICAL
        }

        val incomeTitle = TextView(requireContext()).apply {
            text = "Категории доходов"
            textSize = 18f
            setTextColor(Color.parseColor("#4CAF50"))
            setPadding(0, 0, 0, 15)
        }
        incomeContainer.addView(incomeTitle)

        // Добавляем категории доходов
        Categories.incomeCategories.forEach { category ->
            val categoryView = createCategoryView(category, true)
            incomeContainer.addView(categoryView)
        }

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

        // Контейнер для категорий расходов
        val expenseContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(30, 0, 30, 20)
            }
            orientation = LinearLayout.VERTICAL
        }

        val expenseTitle = TextView(requireContext()).apply {
            text = "Категории расходов"
            textSize = 18f
            setTextColor(Color.parseColor("#F44336"))
            setPadding(0, 0, 0, 15)
        }
        expenseContainer.addView(expenseTitle)

        // Добавляем категории расходов
        Categories.expenseCategories.forEach { category ->
            val categoryView = createCategoryView(category, false)
            expenseContainer.addView(categoryView)
        }

        rootView.addView(incomeContainer)
        rootView.addView(divider)
        rootView.addView(expenseContainer)

        // Добавляем rootView в ScrollView
        scrollView.addView(rootView)

        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = (requireActivity().application as BudgetApp).repository
    }














    private fun createCategoryView(category: String, isIncome: Boolean): View {
        val categoryLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(20, 15, 20, 15)


        }

        val categoryText = TextView(requireContext()).apply {
            text = category
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }
        categoryLayout.addView(categoryText)

        val editButton = TextView(requireContext()).apply {
            text = "✏️"
            textSize = 16f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showEditCategoryDialog(category, isIncome) }

        }
        categoryLayout.addView(editButton)

        val deleteButton = TextView(requireContext()).apply {
            text = "🗑️"
            textSize = 16f
            setPadding(20, 10, 20, 10)
            setOnClickListener { showDeleteCategoryDialog(category, isIncome) }

        }
        categoryLayout.addView(deleteButton)

        return categoryLayout
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

        val typeContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            orientation = LinearLayout.VERTICAL
        }

        val typeLabel = TextView(requireContext()).apply {
            text = "Тип категории:"
            textSize = 16f
            setPadding(0, 0, 0, 10)
        }
        typeContainer.addView(typeLabel)

        val radioGroup = RadioGroup(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val incomeRadio = RadioButton(requireContext()).apply {
            text = "Доход"
            id = View.generateViewId()
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }

        val expenseRadio = RadioButton(requireContext()).apply {
            text = "Расход"
            id = View.generateViewId()
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }

        radioGroup.addView(incomeRadio)
        radioGroup.addView(expenseRadio)
        incomeRadio.isChecked = true

        typeContainer.addView(radioGroup)
        dialogView.addView(typeContainer)

        val nameContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            orientation = LinearLayout.VERTICAL
        }

        val nameLabel = TextView(requireContext()).apply {
            text = "Название категории:"
            textSize = 16f
        }
        nameContainer.addView(nameLabel)

        val editText = EditText(requireContext()).apply {
            hint = "Введите название категории"
            textSize = 16f
            setPadding(20, 15, 20, 15)

            val border = android.graphics.drawable.GradientDrawable()

            border.cornerRadius = 4f

        }
        nameContainer.addView(editText)

        dialogView.addView(nameContainer)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialog, _ ->
                val categoryName = editText.text.toString().trim()
                val isIncome = incomeRadio.isChecked

                if (categoryName.isNotEmpty()) {
                    addCategory(categoryName, isIncome)
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showEditCategoryDialog(oldCategory: String, isIncome: Boolean) {
        val dialogView = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val title = TextView(requireContext()).apply {
            text = "Редактировать категорию"
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(title)

        val editText = EditText(requireContext()).apply {
            setText(oldCategory)
            textSize = 16f
            setPadding(20, 15, 20, 15)

            val border = android.graphics.drawable.GradientDrawable()
            border.setStroke(1, Color.GRAY)
            border.cornerRadius = 4f
            background = border
        }
        dialogView.addView(editText)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newCategory = editText.text.toString().trim()
                if (newCategory.isNotEmpty()) {
                    editCategory(oldCategory, newCategory, isIncome)
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showDeleteCategoryDialog(category: String, isIncome: Boolean) {
        lifecycleScope.launch {
            val type = if (isIncome) "income" else "expense"
            val products = getProductsByCategoryAndType(category, type)
            val productCount = products.size

            val message = if (productCount > 0) {
                "Вы уверены, что хотите удалить категорию \"$category\"?\n" +
                        "Будет удалено $productCount записей, связанных с этой категорией."
            } else {
                "Вы уверены, что хотите удалить категорию \"$category\"?"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Удаление категории")
                .setMessage(message)
                .setPositiveButton("Удалить") { dialog, _ ->
                    deleteCategory(category, isIncome)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun addCategory(categoryName: String, isIncome: Boolean) {
        lifecycleScope.launch {
            val existingCategories = if (isIncome) {
                Categories.incomeCategories
            } else {
                Categories.expenseCategories
            }

            if (existingCategories.contains(categoryName)) {
                Toast.makeText(requireContext(), "Категория \"$categoryName\" уже существует", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (isIncome) {
                Categories.addIncomeCategory(categoryName)
                Toast.makeText(requireContext(), "Категория доходов добавлена: $categoryName", Toast.LENGTH_SHORT).show()
            } else {
                Categories.addExpenseCategory(categoryName)
                Toast.makeText(requireContext(), "Категория расходов добавлена: $categoryName", Toast.LENGTH_SHORT).show()
            }

            requireActivity().runOnUiThread {
                requireActivity().recreate()
            }
        }
    }

    private fun editCategory(oldCategory: String, newCategory: String, isIncome: Boolean) {
        lifecycleScope.launch {
            val existingCategories = if (isIncome) {
                Categories.incomeCategories
            } else {
                Categories.expenseCategories
            }

            if (existingCategories.contains(newCategory) && oldCategory != newCategory) {
                Toast.makeText(requireContext(), "Категория \"$newCategory\" уже существует", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val success = if (isIncome) {
                Categories.editIncomeCategory(oldCategory, newCategory)
            } else {
                Categories.editExpenseCategory(oldCategory, newCategory)
            }

            if (success) {
                val type = if (isIncome) "income" else "expense"
                val products = getProductsByCategoryAndType(oldCategory, type)

                products.forEach { product ->
                    val updatedProduct = product.copy(category = newCategory)
                    repository.update(updatedProduct)
                }

                Toast.makeText(
                    requireContext(),
                    "Категория изменена: $oldCategory → $newCategory\nОбновлено ${products.size} записей",
                    Toast.LENGTH_SHORT
                ).show()

                requireActivity().runOnUiThread {
                    requireActivity().recreate()
                }
            } else {
                Toast.makeText(requireContext(), "Ошибка при изменении категории", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCategory(category: String, isIncome: Boolean) {
        lifecycleScope.launch {
            val success = if (isIncome) {
                Categories.deleteIncomeCategory(category)
            } else {
                Categories.deleteExpenseCategory(category)
            }

            if (success) {
                val type = if (isIncome) "income" else "expense"
                val products = getProductsByCategoryAndType(category, type)

                products.forEach { product ->
                    repository.delete(product)
                }

                Toast.makeText(
                    requireContext(),
                    "Категория удалена: $category\nУдалено ${products.size} записей",
                    Toast.LENGTH_SHORT
                ).show()

                requireActivity().runOnUiThread {
                    requireActivity().recreate()
                }
            } else {
                Toast.makeText(requireContext(), "Ошибка при удалении категории", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getProductsByCategoryAndType(category: String, type: String): List<Product> {
        val allProducts = repository.allProducts.first()
        return allProducts.filter { it.category == category && it.type == type }
    }
}