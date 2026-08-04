package com.example.budgetmgt.Data.Entity.Repository


import com.example.budgetmgt.Data.Dao.ItemDao
import com.example.budgetmgt.Data.Entity.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

     fun getItemsByCategory(categoryId: Int): Flow<List<Item>> =
        itemDao.getItemsByCategory(categoryId)

    suspend fun insertItem(item: Item): Long =
        itemDao.insert(item)

    suspend fun getItemByName(name: String, categoryId: Int): Item? {
        return itemDao.getItemByName(name, categoryId)
    }
}
