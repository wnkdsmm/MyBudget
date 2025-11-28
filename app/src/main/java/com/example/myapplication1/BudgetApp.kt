package com.example.myapplication1

import ProductRepository
import ProductRoomDatabase
import android.app.Application

class BudgetApp : Application() {

    val database: ProductRoomDatabase by lazy {
        ProductRoomDatabase.getDatabase(this)
    }

    val repository: ProductRepository by lazy {
        ProductRepository(database.productDao())
    }
}