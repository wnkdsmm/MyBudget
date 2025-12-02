package com.example.myapplication1.ui.home

import Product
import ProductRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow(0L)
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val endDate: StateFlow<Long> = _endDate

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredProducts: StateFlow<List<Product>> =
        combine(products, _startDate, _endDate, _searchQuery) { list, start, end, query ->
            applyFilters(list, start, end, query)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateStartDate(value: Long) {
        _startDate.value = value
    }

    fun updateEndDate(value: Long) {
        _endDate.value = value
    }

    fun resetDateFilter() {
        _startDate.value = 0L
        _endDate.value = System.currentTimeMillis()
    }

    fun setSelectedDate(value: Long) {
        _selectedDate.value = value
    }

    private fun applyFilters(
        list: List<Product>,
        start: Long,
        end: Long,
        query: String
    ): List<Product> {

        val dateFiltered = if (start == 0L) {
            list.filter { it.date <= end }
        } else list.filter { it.date in start..end }

        return if (query.isNotBlank()) {
            dateFiltered.filter {
                it.category.contains(query, ignoreCase = true) ||
                        it.comment.contains(query, ignoreCase = true)
            }
        } else dateFiltered
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
