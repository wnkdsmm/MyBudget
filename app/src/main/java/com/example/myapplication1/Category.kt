package com.example.myapplication1

data class Category(
    val id: String = "",           // ID документа в Firestore
    val name: String = "",         // Название категории
    val type: String = "expense"   // "income" или "expense"
)