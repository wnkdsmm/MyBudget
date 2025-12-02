//import kotlinx.coroutines.flow.Flow
//
//class `ProductRepository-room`(private val productDao: ProductDao) {
//
//    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
//
//
//
//    suspend fun update(product: Product) {
//        productDao.update(product)
//    }
//
//    suspend fun delete(product: Product) {
//        productDao.delete(product)
//    }
//
//    fun getProductsByType(type: String): Flow<List<Product>> {
//        return productDao.getProductsByType(type)
//    }
//
//    suspend fun getTotalIncome(): Double {
//        return productDao.getTotalByType("income") ?: 0.0
//    }
//
//    suspend fun getTotalExpenses(): Double {
//        return productDao.getTotalByType("expense") ?: 0.0
//    }
//
//    suspend fun getBalance(): Double {
//        return getTotalIncome() - getTotalExpenses()
//    }
//    suspend fun getProductsByCategoryAndType(category: String, type: String): List<Product> {
//        return productDao.getProductsByCategoryAndType(category, type)
//    }
//
//    suspend fun insert(product: Product): Product {
//        val id = productDao.insertReturnId(product)
//        return product.copy(id = id)
//    }
//}