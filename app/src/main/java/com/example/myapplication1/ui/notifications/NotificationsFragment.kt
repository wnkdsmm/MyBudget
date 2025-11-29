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

        // Кнопка экспорта
        val exportButton = TextView(requireContext()).apply {
            text = "📤 Экспорт"
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setPadding(0, 15, 0, 15)
            setOnClickListener { exportData() }

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginEnd = 10
            }

            val border = android.graphics.drawable.GradientDrawable()
            border.cornerRadius = 8f
            background = border
        }
        importExportContainer.addView(exportButton)

        // Кнопка импорта
        val importButton = TextView(requireContext()).apply {
            text = "📥 Импорт"
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2196F3"))
            setPadding(0, 15, 0, 15)
            setOnClickListener { importData() }

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginStart = 10
            }

            val border = android.graphics.drawable.GradientDrawable()
            border.cornerRadius = 8f
            background = border
        }
        importExportContainer.addView(importButton)

        rootView.addView(importExportContainer)

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

    private fun exportData() {
        lifecycleScope.launch {
            try {
                val allProducts = repository.allProducts.first()

                if (allProducts.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Создаем CSV формат
                val csvContent = StringBuilder()

                // Заголовок CSV
                csvContent.append("type,category,amount,date,comment\n")

                // Данные
                allProducts.forEach { product ->
                    csvContent.append("${product.type},")
                    csvContent.append("${product.category},")
                    csvContent.append("${product.amount},")
                    csvContent.append("${product.date},")
                    csvContent.append("\"${product.comment}\"\n")
                }

                // Создаем имя файла с датой
                val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault())
                val fileName = "budget_data_${dateFormat.format(Date())}.csv"

                // Сохраняем файл
                val file = File(requireContext().filesDir, fileName)
                FileOutputStream(file).use { stream ->
                    stream.write(csvContent.toString().toByteArray())
                }

                Toast.makeText(
                    requireContext(),
                    "Данные экспортированы в файл: $fileName",
                    Toast.LENGTH_LONG
                ).show()

                // Показываем диалог с информацией
                showExportSuccessDialog(file.absolutePath, allProducts.size)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка при экспорте: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExportSuccessDialog(filePath: String, recordCount: Int) {
        val message = """
            Данные успешно экспортированы!
            
            Файл: ${File(filePath).name}
            Записей: $recordCount
            
            Файл сохранен во внутренней памяти приложения.
            Чтобы перенести данные на другое устройство:
            
            1. Скопируйте файл из папки приложения
            2. Перенесите на другое устройство
            3. Используйте функцию импорта
            
            Путь к файлу:
            $filePath
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Экспорт завершен")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun importData() {
        // Показываем предупреждение о перезаписи данных
        AlertDialog.Builder(requireContext())
            .setTitle("Импорт данных")
            .setMessage("Внимание! При импорте все текущие данные будут заменены. Продолжить?")
            .setPositiveButton("Продолжить") { dialog, _ ->
                showFileSelectionDialog()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showFileSelectionDialog() {
        lifecycleScope.launch {
            try {
                val filesDir = requireContext().filesDir
                val csvFiles = filesDir.listFiles { file ->
                    file.name.endsWith(".csv") && file.isFile
                } ?: emptyArray()

                if (csvFiles.isEmpty()) {
                    Toast.makeText(requireContext(), "Файлы для импорта не найдены", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fileNames = csvFiles.map { it.name }.toTypedArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Выберите файл для импорта")
                    .setItems(fileNames) { dialog, which ->
                        val selectedFile = csvFiles[which]
                        importFromFile(selectedFile)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка при поиске файлов: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importFromFile(file: File) {
        lifecycleScope.launch {
            try {
                val content = FileInputStream(file).use { stream ->
                    stream.bufferedReader().use { reader ->
                        reader.readText()
                    }
                }

                val lines = content.split("\n")
                if (lines.size < 2) {
                    Toast.makeText(requireContext(), "Файл пустой или некорректный", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Удаляем все текущие данные
                val currentProducts = repository.allProducts.first()
                currentProducts.forEach { product ->
                    repository.delete(product)
                }

                var importedCount = 0
                var errorCount = 0

                // Пропускаем заголовок и обрабатываем данные
                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue

                    try {
                        val parts = parseCSVLine(line)
                        if (parts.size >= 5) {
                            val product = Product(
                                type = parts[0],
                                category = parts[1],
                                amount = parts[2].toDouble(),
                                date = parts[3].toLong(),
                                comment = parts[4]
                            )
                            repository.insert(product)
                            importedCount++
                        }
                    } catch (e: Exception) {
                        errorCount++
                        // Пропускаем некорректные строки
                    }
                }

                // Показываем результат импорта
                showImportResultDialog(importedCount, errorCount, file.name)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка при импорте: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        var currentField = StringBuilder()

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(currentField.toString())
                    currentField = StringBuilder()
                }
                else -> currentField.append(char)
            }
        }
        result.add(currentField.toString())
        return result
    }

    private fun showImportResultDialog(successCount: Int, errorCount: Int, fileName: String) {
        val message = """
            Импорт завершен!
            
            Файл: $fileName
            Успешно импортировано: $successCount записей
            Ошибок: $errorCount записей
            
            ${if (successCount > 0) "✅ Данные успешно загружены" else "❌ Не удалось импортировать данные"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Результат импорта")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Обновляем UI после импорта
                requireActivity().runOnUiThread {
                    requireActivity().recreate()
                }
            }
            .show()
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