package com.example.myapplication1

import ProductRepository
import android.app.Application

class BudgetApp : Application() {

    // Репозиторий, работающий только с Firestore
    val repository: ProductRepository by lazy {
        ProductRepository()
    }
}
