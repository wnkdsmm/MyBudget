package com.example.myapplication1.ui.notifications



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.Category
import com.example.myapplication1.CategoryRepository
import com.example.myapplication1.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // StateFlow для списка категорий (UI будет подписан)
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    init {
        refreshCategories()
    }

    // Обновление списка категорий
    private fun refreshCategories() {
        viewModelScope.launch {
            _categories.value = categoryRepository.getAllCategories()
        }
    }

    // Добавление новой категории
    fun addCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            categoryRepository.addCategory(category)
            refreshCategories()
            onComplete()
        }
    }

    // Редактирование категории
    fun updateCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Получаем старую категорию по ID
            val oldCategory = categoryRepository.getCategoryById(category.id)
            if (oldCategory != null) {
                // Обновляем категорию в Firestore
                categoryRepository.updateCategory(category)

                // Получаем все продукты с этой старой категорией
                val products = productRepository.allProducts.first()
                    .filter { it.category == oldCategory.name && it.type == oldCategory.type }

                // Обновляем категорию у всех продуктов
                products.forEach { product ->
                    productRepository.update(product.copy(category = category.name))
                }

                // Обновляем список категорий для UI
                refreshCategories()
            }
            onComplete()
        }
    }

    // Удаление категории
    fun deleteCategory(category: Category, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Получаем все продукты с этой категорией
            val products = productRepository.allProducts.first()
                .filter { it.category == category.name && it.type == category.type }

            // Удаляем продукты из Firestore
            products.forEach { productRepository.delete(it) }

            // Удаляем категорию
            categoryRepository.deleteCategory(category.id)

            // Обновляем список категорий
            refreshCategories()
            onComplete()
        }
    }
}
