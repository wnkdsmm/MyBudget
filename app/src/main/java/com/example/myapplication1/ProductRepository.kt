package com.example.myapplication1

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestoreRepo: ProductFirestoreRepository = ProductFirestoreRepository()
) {

    // Поток всех продуктов
    val allProducts: Flow<List<Product>> = firestoreRepo.getAllProductsFlow()

    // Вставка нового продукта
    suspend fun insert(product: Product): Product {
        return firestoreRepo.insertProduct(product)
    }

    // Обновление продукта
    suspend fun update(product: Product) {
        firestoreRepo.insertProduct(product) // insertProduct обновляет по id
    }

    // Удаление продукта
    suspend fun delete(product: Product) {
        if (product.id.isNotEmpty()) {
            firestoreRepo.productsCollection.document(product.id).delete().await()
        }
    }

}
