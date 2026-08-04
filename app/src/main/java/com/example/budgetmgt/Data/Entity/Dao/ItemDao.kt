package com.example.budgetmgt.Data.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.budgetmgt.Data.Entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items ORDER BY name ASC")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items WHERE categoryId = :categoryId")
     fun getItemsByCategory(categoryId: Int): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    suspend fun getItemById(itemId: Int): Item?

    @Query("SELECT * FROM items WHERE name = :name AND categoryId = :categoryId LIMIT 1")
    suspend fun getItemByName(name: String, categoryId: Int): Item?
}
