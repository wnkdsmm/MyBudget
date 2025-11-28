package com.example.myapplication1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1, exportSchema = false) // исправлено здесь
abstract class ItemRoomDatabase: RoomDatabase() {
    abstract fun itemDao(): ItemDao // исправлено название метода (было productDao)

    companion object {
        private var INSTANCE: ItemRoomDatabase? = null

        fun getInstance(context: Context): ItemRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ItemRoomDatabase::class.java,
                        "item_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}