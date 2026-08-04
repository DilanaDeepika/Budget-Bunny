package com.example.budgetmgt.Data.Entity.Repository


import com.example.budgetmgt.Data.Dao.CategoryDao
import com.example.budgetmgt.Data.Entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

     fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

}
