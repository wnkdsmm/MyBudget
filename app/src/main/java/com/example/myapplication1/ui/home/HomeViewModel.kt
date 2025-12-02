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

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products

    private var startDate: Long = 0L
    private var endDate: Long = System.currentTimeMillis()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredProducts: StateFlow<List<Product>> = combine(_products, _searchQuery) { list, query ->
        applyFilters(list, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshProducts()
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            repository.allProducts.collect { list ->
                _products.value = filterProductsByDate(list)
            }
        }
    }

    private fun filterProductsByDate(products: List<Product>): List<Product> {
        return if (startDate == 0L) products.filter { it.date <= endDate }
        else products.filter { it.date in startDate..endDate }
    }

    private fun applyFilters(list: List<Product>, query: String): List<Product> {
        val dateFiltered = filterProductsByDate(list)
        return if (query.isNotBlank()) {
            dateFiltered.filter {
                it.category.contains(query, ignoreCase = true) ||
                        it.comment.contains(query, ignoreCase = true)
            }
        } else dateFiltered
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateStartDate(timestamp: Long) {
        startDate = timestamp
        refreshProducts()
    }

    fun updateEndDate(timestamp: Long) {
        endDate = timestamp
        refreshProducts()
    }

    fun resetDateFilter() {
        startDate = 0L
        endDate = System.currentTimeMillis()
        refreshProducts()
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch { repository.update(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.delete(product) }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Не выбрано"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
