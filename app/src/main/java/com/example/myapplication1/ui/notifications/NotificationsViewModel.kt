package com.example.myapplication1.ui.notifications

import Product
import ProductRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.Category
import com.example.myapplication1.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Используем suspend-функцию вместо Flow
    suspend fun getAllCategories(): List<Category> {
        return categoryRepository.getAllCategories()
    }

    fun addCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            categoryRepository.addCategory(category)
            onComplete()
        }
    }

    fun updateCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)

            val products = getProductsByCategory(category)
            products.forEach { productRepository.update(it.copy(category = category.name)) }

            onComplete()
        }
    }

    fun deleteCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category.id)

            val products = getProductsByCategory(category)
            products.forEach { productRepository.delete(it) }

            onComplete()
        }
    }

    private suspend fun getProductsByCategory(category: Category): List<Product> {
        val allProducts = productRepository.allProducts.first()
        return allProducts.filter { it.category == category.name && it.type == category.type }
    }
}
