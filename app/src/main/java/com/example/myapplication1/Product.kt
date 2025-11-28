import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "income" или "expense"
    val category: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val comment: String = ""
)