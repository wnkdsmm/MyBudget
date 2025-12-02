package com.example.myapplication1


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication1.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedDate = System.currentTimeMillis()

    private val productRepository by lazy { (application as BudgetApp).repository }
    private val categoryRepository by lazy { (application as BudgetApp).categoryRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        lifecycleScope.launch {
            MigrationUtils.migrateRoomToFirestore(this@MainActivity)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupFAB()
    }

    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            showAddProductDialog()
        }
    }

    private suspend fun resetMigrationForDebug() {
        MigrationUtils.resetMigration(this)
        Log.d("MainActivity", "Флаг миграции сброшен")
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)

        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)
        val editTextDate = dialogView.findViewById<EditText>(R.id.edit_text_date)

        selectedDate = System.currentTimeMillis()
        updateDateText(editTextDate)

        // Первичная загрузка категорий (расход по умолчанию)
        loadCategoriesIntoSpinner(spinnerCategory, isIncome = false)

        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            val isIncome = checkedId == R.id.radio_income
            loadCategoriesIntoSpinner(spinnerCategory, isIncome)
        }

        editTextDate.setOnClickListener { showDatePicker(editTextDate) }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить запись")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialog, _ ->
                saveProductFromDialog(dialogView)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }

    private fun showDatePicker(editTextDate: EditText) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val newCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                selectedDate = newCalendar.timeInMillis
                updateDateText(editTextDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateDateText(editTextDate: EditText) {
        val dateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(selectedDate))
        editTextDate.setText(dateString)
    }

    /** Загружает категории из базы и обновляет Spinner */
    private fun loadCategoriesIntoSpinner(spinner: Spinner, isIncome: Boolean) {
        val type = if (isIncome) "income" else "expense"

        lifecycleScope.launch {
            val categories = categoryRepository.getCategoriesByType(type)

            if (categories.isEmpty()) {
                Log.w("MainActivity", "Категории ($type) не найдены в БД")
            }

            val adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                categories
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun saveProductFromDialog(dialogView: View) {
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        val amount = editTextAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (radioGroupType.checkedRadioButtonId == R.id.radio_income) "income" else "expense"
        val category = spinnerCategory.selectedItem as? String ?: ""
        val comment = editTextComment.text.toString()

        val product = Product(
            type = type,
            category = category,
            amount = amount,
            date = selectedDate,
            comment = comment
        )

        lifecycleScope.launch {
            productRepository.insert(product)

            // уведомляем HomeFragment для обновления списка
            supportFragmentManager.setFragmentResult("update_list", Bundle())

            Toast.makeText(this@MainActivity, "Запись добавлена", Toast.LENGTH_SHORT).show()
        }

    }
}
