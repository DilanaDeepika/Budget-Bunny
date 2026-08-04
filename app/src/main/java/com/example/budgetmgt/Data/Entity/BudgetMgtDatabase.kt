package com.example.budgetmgt.Data.Entity

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.budgetmgt.Data.Dao.CategoryDao
import com.example.budgetmgt.Data.Dao.ItemDao
import com.example.budgetmgt.Data.Dao.PlanCategoryBudgetDao
import com.example.budgetmgt.Data.Dao.PlanDao
import com.example.budgetmgt.Data.Dao.PurchaseDao
import com.example.budgetmgt.Data.Entity.Dao.PurchaseItemDao

@Database(
    entities = [
        Plan::class,
        Purchase::class,
        PurchaseItem::class,
        Category::class,
        Item::class,
        PlanCategoryBudget::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BudgetMgtDatabase : RoomDatabase() {

    // DAOs
    abstract fun planDao(): PlanDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun planCategoryBudgetDao(): PlanCategoryBudgetDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemDao(): ItemDao
}