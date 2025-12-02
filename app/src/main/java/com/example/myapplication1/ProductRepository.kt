import com.example.myapplication1.ProductFirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepository(private val firestoreRepo: ProductFirestoreRepository = ProductFirestoreRepository()) {

    // Поток всех продуктов из Firestore
    val allProducts: Flow<List<Product>> = flow {
        while (true) {
            emit(firestoreRepo.getAllProducts())
            kotlinx.coroutines.delay(5000) // обновление каждые 5 секунд
        }
    }

    // Вставка нового продукта
    suspend fun insert(product: Product): Product {
        firestoreRepo.insertProduct(product)
        return product
    }

    // Обновление существующего продукта
    suspend fun update(product: Product) {
        firestoreRepo.insertProduct(product) // Firestore set() с существующим ID обновляет документ
    }

    // Удаление продукта
    suspend fun delete(product: Product) {
        try {
            val docId = product.id.toString()
            firestoreRepo.productsCollection.document(docId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Получение продуктов по типу
    fun getProductsByType(type: String): Flow<List<Product>> = flow {
        while (true) {
            val filtered = firestoreRepo.getAllProducts().filter { it.type == type }
            emit(filtered)
            kotlinx.coroutines.delay(5000)
        }
    }

    // Получение общей суммы доходов
    suspend fun getTotalIncome(): Double {
        return firestoreRepo.getAllProducts().filter { it.type == "income" }.sumOf { it.amount }
    }

    // Получение общей суммы расходов
    suspend fun getTotalExpenses(): Double {
        return firestoreRepo.getAllProducts().filter { it.type == "expense" }.sumOf { it.amount }
    }

    // Получение баланса
    suspend fun getBalance(): Double {
        return getTotalIncome() - getTotalExpenses()
    }

    // Получение продуктов по категории и типу
    suspend fun getProductsByCategoryAndType(category: String, type: String): List<Product> {
        return firestoreRepo.getAllProducts().filter { it.type == type && it.category == category }
    }
}
