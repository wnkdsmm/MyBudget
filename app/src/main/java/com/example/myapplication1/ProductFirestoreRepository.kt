package com.example.myapplication1

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductFirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val productsCollection = db.collection("products")
    private val TAG = "ProductFirestoreRepo"

    // Вставка или обновление продукта
    suspend fun insertProduct(product: Product): Product {
        return try {
            val documentId = if (product.id.isEmpty()) productsCollection.document().id else product.id
            val productData = hashMapOf(
                "type" to product.type,
                "category" to product.category,
                "amount" to product.amount,
                "date" to product.date,
                "comment" to product.comment
            )
            productsCollection.document(documentId).set(productData).await()
            Log.d(TAG, "Продукт сохранён, documentId: $documentId")
            product.copy(id = documentId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения продукта", e)
            throw e
        }
    }

    // Поток всех продуктов из Firestore
    fun getAllProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val date = when (val dateValue = data["date"]) {
                    is Timestamp -> dateValue.toDate().time
                    is Long -> dateValue
                    is Double -> dateValue.toLong()
                    is Int -> dateValue.toLong()
                    else -> System.currentTimeMillis()
                }

                Product(
                    id = doc.id,
                    type = data["type"] as? String ?: "",
                    category = data["category"] as? String ?: "",
                    amount = data["amount"] as? Double ?: 0.0,
                    date = date,
                    comment = data["comment"] as? String ?: ""
                )
            } ?: emptyList()

            trySend(products)
        }

        awaitClose { listener.remove() }
    }


}
