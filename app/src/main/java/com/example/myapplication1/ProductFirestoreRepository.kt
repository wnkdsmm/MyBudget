package com.example.myapplication1

import Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.Timestamp

class ProductFirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val productsCollection = db.collection("products")
    private val TAG = "ProductFirestoreRepo"

    // Метод для вставки/обновления документа
    suspend fun insertProduct(product: Product) {
        try {
            Log.d(TAG, "Вставляем продукт в Firestore: ID=${product.id}")

            // Если ID = 0, генерируем новый документ
            val documentId = if (product.id == 0L) {
                productsCollection.document().id
            } else {
                product.id.toString()
            }

            // Преобразуем данные для Firestore
            val productData = hashMapOf(
                "id" to product.id,
                "type" to product.type,
                "category" to product.category,
                "amount" to product.amount,
                "date" to product.date, // Сохраняем как Long
                "comment" to product.comment
            )

            productsCollection.document(documentId).set(productData).await()
            Log.d(TAG, "Продукт успешно сохранен в Firestore, documentId: $documentId")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения в Firestore", e)
            throw e
        }
    }

    // Метод для получения всех продуктов
    suspend fun getAllProducts(): List<Product> {
        return try {
            Log.d(TAG, "Получение всех продуктов из Firestore")
            val snapshot = productsCollection.get().await()

            val products = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    val date = when (val dateValue = data["date"]) {
                        is Timestamp -> dateValue.toDate().time
                        is Long -> dateValue
                        is Double -> dateValue.toLong()
                        is Int -> dateValue.toLong()
                        else -> {
                            Log.w(TAG, "Некорректный формат даты: $dateValue")
                            System.currentTimeMillis()
                        }
                    }

                    Product(
                        id = (data["id"] as? Long) ?: 0L,
                        type = data["type"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        amount = (data["amount"] as? Double) ?: 0.0,
                        date = date,
                        comment = data["comment"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка парсинга документа ${document.id}", e)
                    null
                }
            }

            Log.d(TAG, "Получено ${products.size} продуктов из Firestore")
            products

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения продуктов из Firestore", e)
            emptyList()
        }
    }

    // Метод для проверки подключения к Firestore
    suspend fun testConnection(): Boolean {
        return try {
            // Простая проверка - пытаемся получить счетчик документов
            productsCollection.limit(1).get().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка подключения к Firestore", e)
            false
        }
    }
}