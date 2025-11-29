import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products ORDER BY date DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE type = :type ORDER BY date DESC")
    fun getProductsByType(type: String): Flow<List<Product>>

    @Query("SELECT SUM(amount) FROM products WHERE type = :type")
    suspend fun getTotalByType(type: String): Double?

    @Query("SELECT * FROM products WHERE category = :category AND type = :type")
    suspend fun getProductsByCategoryAndType(category: String, type: String): List<Product>
}