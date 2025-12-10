package com.example.myapplication1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.Product
import com.example.myapplication1.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    // Поиск
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Фильтр по датам
    private val _startDate = MutableStateFlow(0L)
    private val _endDate = MutableStateFlow(System.currentTimeMillis())

    // Все продукты из репозитория
    private val allProductsFlow: StateFlow<List<Product>> =
        repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Фильтрованные продукты
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProductsFlow,
        _searchQuery,
        _startDate,
        _endDate
    ) { products, query, start, end ->
        products
            .filter { it.date in start..end }
            .filter { product ->
                query.isBlank() ||
                        product.category.contains(query, ignoreCase = true) ||
                        product.comment.contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.date }   // сортировка по убыванию (новые сверху)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Обновление поиска
    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    // Обновление даты начала
    fun updateStartDate(timestamp: Long) {
        _startDate.value = timestamp
    }

    // Обновление даты окончания
    fun updateEndDate(timestamp: Long) {
        _endDate.value = timestamp
    }

    // Сброс фильтра по датам
    fun resetDateFilter() {
        _startDate.value = 0L
        _endDate.value = System.currentTimeMillis()
    }

    // CRUD операции
    fun addProduct(product: Product) {
        viewModelScope.launch { repository.insert(product) }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch { repository.update(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.delete(product) }
    }

    // Форматирование даты
    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
