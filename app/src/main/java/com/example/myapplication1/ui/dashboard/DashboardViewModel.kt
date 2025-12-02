package com.example.myapplication1.ui.dashboard



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.Product
import com.example.myapplication1.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products

    private var startDate: Long = 0L
    private var endDate: Long = System.currentTimeMillis()

    init {
        refreshProducts()
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            repository.allProducts.collectLatest { productList ->
                _products.value = filterProductsByDate(productList)
            }
        }
    }

    private fun filterProductsByDate(products: List<Product>): List<Product> {
        return if (startDate == 0L) products.filter { it.date <= endDate }
        else products.filter { it.date in startDate..endDate }
    }

    fun setStartDate(timestamp: Long) {
        startDate = timestamp
        refreshProducts()
    }

    fun setEndDate(timestamp: Long) {
        endDate = timestamp
        refreshProducts()
    }

    fun resetDates() {
        startDate = 0L
        endDate = System.currentTimeMillis()
        refreshProducts()
    }

    fun getStartDate() = startDate
    fun getEndDate() = endDate

    fun calculateTotals(products: List<Product>): Triple<Double, Double, Double> {
        val totalIncome = products.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = products.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        return Triple(totalIncome, totalExpense, balance)
    }

    fun groupByCategory(products: List<Product>): Map<String, Double> {
        return products.groupBy { it.category }
            .mapValues { it.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }
}
