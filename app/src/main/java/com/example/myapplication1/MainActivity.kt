package com.example.myapplication1

import Product
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
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
    private var selectedDate = System.currentTimeMillis() // по умолчанию сегодня

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        lifecycleScope.launch {
            resetMigrationForDebug()
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

        // Инициализация даты (сегодня)
        selectedDate = System.currentTimeMillis()
        updateDateText(editTextDate)

        // Инициализация спиннера
        updateCategoriesSpinner(spinnerCategory, false)

        // Обработка смены типа (доход/расход)
        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            updateCategoriesSpinner(spinnerCategory, checkedId == R.id.radio_income)
        }

        // Обработка клика на поле даты
        editTextDate.setOnClickListener {
            showDatePicker(editTextDate)
        }


        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить запись")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialog, _ ->
                saveProductFromDialog(dialogView)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showDatePicker(editTextDate: EditText) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                selectedDate = newCalendar.timeInMillis
                updateDateText(editTextDate)
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun updateDateText(editTextDate: EditText) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(selectedDate))
        editTextDate.setText(dateString)
    }

    private fun updateCategoriesSpinner(spinner: Spinner, isIncome: Boolean) {
        val categories = if (isIncome) {
            Categories.incomeCategories
        } else {
            Categories.expenseCategories
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun saveProductFromDialog(dialogView: View) {
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.edit_text_amount)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        val amountText = editTextAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (radioGroupType.checkedRadioButtonId == R.id.radio_income) "income" else "expense"
        val category = spinnerCategory.selectedItem as String
        val comment = editTextComment.text.toString()

        val product = Product(
            type = type,
            category = category,
            amount = amount,
            date = selectedDate, // используем выбранную дату
            comment = comment
        )

        // Сохраняем в базу данных
        lifecycleScope.launch {
            val repository = (application as BudgetApp).repository
            repository.insert(product)
            Toast.makeText(this@MainActivity, "Запись добавлена", Toast.LENGTH_SHORT).show()
        }

    }

}