package com.example.myapplication1

import Product

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.first

object MigrationUtils {

    private const val PREFS_NAME = "migration_prefs"
    private const val KEY_MIGRATED = "is_migrated"
    private const val TAG = "MigrationUtils"

    suspend fun migrateRoomToFirestore(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isMigrated = prefs.getBoolean(KEY_MIGRATED, false)

        if (isMigrated) {
            Log.d(TAG, "Миграция уже выполнена ранее")
            return
        }

        try {
            Log.d(TAG, "Начало миграции Room → Firestore")

            // Получаем репозитории
            val app = context.applicationContext as BudgetApp
            val roomRepository = app.repository
            val firestoreRepository = ProductFirestoreRepository()

            // Получаем все локальные продукты
            val localProducts = roomRepository.allProducts.first()
            Log.d(TAG, "Найдено ${localProducts.size} продуктов для миграции")

            if (localProducts.isEmpty()) {
                Log.d(TAG, "Нет данных для миграции")
                markMigrationComplete(prefs)
                return
            }

            // Отправляем в Firestore
            var successCount = 0
            var errorCount = 0

            localProducts.forEachIndexed { index, product ->
                try {
                    Log.d(TAG, "Миграция продукта ${index + 1}/${localProducts.size}: ID=${product.id}, Дата=${product.date}")

                    // Создаем копию продукта с корректным ID для Firestore
                    val productForFirestore = Product(
                        id = product.id,
                        type = product.type,
                        category = product.category,
                        amount = product.amount,
                        date = product.date,
                        comment = product.comment
                    )

                    firestoreRepository.insertProduct(productForFirestore)
                    successCount++
                } catch (e: Exception) {
                    errorCount++
                    Log.e(TAG, "Ошибка при миграции продукта ${product.id}: ${e.message}", e)
                }
            }

            Log.d(TAG, "Миграция завершена: успешно $successCount, ошибок $errorCount")

            if (successCount > 0) {
                markMigrationComplete(prefs)
            } else {
                Log.w(TAG, "Ни один продукт не был мигрирован")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка миграции: ${e.message}", e)
            throw e
        }
    }

    private fun markMigrationComplete(prefs: SharedPreferences) {
        prefs.edit().putBoolean(KEY_MIGRATED, true).apply()
        Log.d(TAG, "Миграция помечена как завершенная")
    }

    // Метод для сброса флага миграции (для отладки)
    fun resetMigration(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MIGRATED, false).apply()
        Log.d(TAG, "Флаг миграции сброшен")
    }

    // Проверка статуса миграции
    fun isMigrationComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MIGRATED, false)
    }
}