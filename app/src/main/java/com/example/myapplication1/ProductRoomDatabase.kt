import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class ProductRoomDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: ProductRoomDatabase? = null

        fun getDatabase(context: Context): ProductRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductRoomDatabase::class.java,
                    "budget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}