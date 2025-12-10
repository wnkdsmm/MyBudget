package com.example.myapplication1.ui.notifications

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.Category
import com.example.myapplication1.CategoryRepository
import com.example.myapplication1.ProductRepository
import kotlinx.coroutines.flow.collectLatest
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
            val border = GradientDrawable().apply {
                cornerRadius = 8f
                setStroke(2, Color.LTGRAY)
            }
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

        // Заглушка снизу
        val bottomSpacer = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                550
            )
        }
        rootView.addView(bottomSpacer)

        scrollView.addView(rootView)
        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = (requireActivity().application as BudgetApp).repository
        categoryRepository = CategoryRepository()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { categories ->
                    displayCategories(categories)
                }
            }
        }
    }

    private fun displayCategories(categories: List<Category>) {
        if (incomeContainer.childCount > 1) {
            incomeContainer.removeViews(1, incomeContainer.childCount - 1)
        }
        if (expenseContainer.childCount > 1) {
            expenseContainer.removeViews(1, expenseContainer.childCount - 1)
        }

        // GridLayout для доходов
        val incomeGrid = GridLayout(requireContext()).apply {
            columnCount = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        categories.filter { it.type == "income" }.forEach { category ->
            incomeGrid.addView(createCategoryCard(category))
        }
        incomeContainer.addView(incomeGrid)

        // GridLayout для расходов
        val expenseGrid = GridLayout(requireContext()).apply {
            columnCount = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        categories.filter { it.type == "expense" }.forEach { category ->
            expenseGrid.addView(createCategoryCard(category))
        }
        expenseContainer.addView(expenseGrid)
    }

    private fun createCategoryCard(category: Category): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 40) // увеличена высота карточки
            background = GradientDrawable().apply {
                cornerRadius = 16f
                setStroke(2, Color.LTGRAY)
            }
            elevation = 8f
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = LinearLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(10, 10, 10, 10)
            }

            setOnClickListener {
                showCategoryOptionsDialog(category)
            }
        }

        val nameView = TextView(requireContext()).apply {
            text = category.name
            textSize = 16f
            gravity = Gravity.CENTER
        }
        card.addView(nameView)

        return card
    }

    private fun showCategoryOptionsDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle(category.name)
            .setItems(arrayOf("Редактировать", "Удалить")) { dialog, which ->
                when (which) {
                    0 -> showEditCategoryDialog(category)
                    1 -> showDeleteCategoryDialog(category)
                }
            }
            .show()
    }



    private fun showAddCategoryDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
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
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление категории")
            .setMessage("Удалить категорию \"${category.name}\" и все связанные записи?")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewModel.deleteCategory(category) {
                    Toast.makeText(requireContext(), "Категория и связанные записи удалены: ${category.name}", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
