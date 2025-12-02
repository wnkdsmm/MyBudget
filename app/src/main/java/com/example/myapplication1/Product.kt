package com.example.myapplication1

data class Product(
    val id: String = "",      // ID документа Firestore
    val type: String = "expense",
    val category: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val comment: String = ""
)
