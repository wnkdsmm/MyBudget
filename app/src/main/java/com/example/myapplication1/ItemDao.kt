package com.example.myapplication1

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemDao {
    @Insert
    fun insertItem(item: Item)

    @Query("SELECT * FROM items WHERE category = :category")
    fun findItemsByCategory(category: String): List<Item>

    @Query("DELETE FROM items WHERE itemId = :id")
    fun deleteItem(id: Int)

    @Query("SELECT * FROM items")
    fun getAllItems(): LiveData<List<Item>>
}
