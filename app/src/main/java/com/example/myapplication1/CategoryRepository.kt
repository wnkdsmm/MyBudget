package com.example.myapplication1

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    suspend fun getAllCategories(): List<Category> {
        val snapshot = categoriesCollection.get().await()
        return snapshot.documents.map { doc ->
            Category(
                id = doc.id,
                name = doc.getString("name") ?: "",
                type = doc.getString("type") ?: "expense"
            )
        }
    }

    suspend fun addCategory(category: Category) {
        categoriesCollection.add(
            mapOf(
                "name" to category.name,
                "type" to category.type
            )
        ).await()
    }

    suspend fun updateCategory(category: Category) {
        categoriesCollection.document(category.id)
            .update(
                mapOf(
                    "name" to category.name,
                    "type" to category.type
                )
            ).await()
    }

    suspend fun deleteCategory(categoryId: String) {
        categoriesCollection.document(categoryId).delete().await()
    }

    suspend fun getCategoriesByType(type: String): List<String> {
        val snapshot = categoriesCollection
            .whereEqualTo("type", type)
            .get()
            .await()

        return snapshot.documents.map { doc -> doc.getString("name") ?: "" }
    }


}
