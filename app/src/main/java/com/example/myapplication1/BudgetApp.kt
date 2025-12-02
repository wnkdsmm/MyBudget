package com.example.myapplication1

import android.app.Application
import ProductRepository


class BudgetApp : Application() {

    // Репозиторий для работы с продуктами
    val repository: ProductRepository by lazy {
        ProductRepository()
    }

    // Репозиторий для работы с категориями
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository()
    }
}
