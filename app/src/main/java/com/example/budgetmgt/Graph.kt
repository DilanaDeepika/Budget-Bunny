package com.example.budgetmgt

import android.content.ContentValues
import com.example.budgetmgt.Data.Entity.Repository.CategoryRepository
import com.example.budgetmgt.Data.Entity.Repository.ItemRepository
import com.example.budgetmgt.Data.Entity.Repository.PlanCategoryBudgetRepository
import com.example.budgetmgt.Data.Entity.Repository.PlanRepository
import com.example.budgetmgt.Data.Entity.Repository.PurchaseItemRepository
import com.example.budgetmgt.Data.Entity.Repository.PurchaseRepository


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetmgt.Data.Entity.*

object Graph {
    lateinit var database: BudgetMgtDatabase
        private set

    // Initialize database and repositories
    fun init(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            BudgetMgtDatabase::class.java,
            "Budget.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // 1. Define Data: Category Name -> List of Item Names
                    val initialData = mapOf(
                        "Main Carb" to listOf(
                            "Rice", "Flour", "Noodles", "Bread", "Kotthu", "Pasta"
                        ),

                        "Additional food" to listOf(
                            "Brackfast", "Snack", "Dinner"
                        ),

                        "Proteins" to listOf(
                            "Fish", "Chiken", "Egg", "Dry Fish", "Sausage", "Canned Fish",
                            "Mushroom", "Soya", "Pork", "Beef", "Crabs", "Catel Fish", "Shrimp"
                        ),

                        "Drinks" to listOf(
                            "Milk powder", "Fresh milk", "Tea", "Coffe", "Juice", "Malted milk"
                        ),

                        "Dessert" to listOf(
                            "Yougurt", "Ice Cream", "Curd"
                        ),

                        "Sweeteners" to listOf(
                            "Sugar", "Hony", "Bee Hony", "Hakaru", "Dat"
                        ),

                        "Vegetables" to listOf(
                            "Cabbage", "Tomato", "Pumpkin", "Cucumber", "Potato", "Beet",
                            "Letturce", "Brinjal", "Cauliflower", "Leeks", "Ambaralla",
                            "Carrot", "bean", "Lady's finger", "Bitter Gourd", "Radish",
                            "Winged Bean", "Drumstick", "Jackfruit", "Bread Fruit",
                            "Green Leaves", "Long Beans", "Mango", "Egg Plante",
                            "PeaeagPlants", "Marrow", "Kohila", "ASHPlantain", "Capsicum",
                            "Snakegourd", "Marrow", "Ridgegourd", "Maniyok", "Sweetpotato",
                            "Yam"
                        ),

                        "Spices" to listOf(
                            "Green chilli", "Red Chilli", "Chilli powder", "Chilli picese",
                            "Curry powder", "pepper corns", "pepper powder", "Salt",
                            "Turmeric", "Mustered", "Fenugreek", "Cinnamon", "Goraka",
                            "Onion big", "Onion small", "Garlick", "Ginger", "Viniger", "lime"
                        ),

                        "Fruits" to listOf(
                            "Banana", "Mango", "Pinapple", "Papaw", "Avocado",
                            "Water Melon", "Wood Apple", "Guava"
                        ),

                        "Cleaner" to listOf(
                            "Soap", "WashingPowder", "Toothpast", "Frashner",
                            "Dishwasher", "Till Cleaner"
                        ),

                        "Bill" to listOf(
                            "Electric Bill", "Water Bill", "Teliphlin Bill",
                            "Tax Assessment", "Lone"
                        ),

                        "Fats/mix" to listOf(
                            "Jam", "Butter", "Sauce", "Cheese", "Oil", "Margarim",
                            "Cream", "Ghee", "Chatny", "Coconut", "Coconut powder"
                        ),

                        "Seed" to listOf(
                            "Dhal", "Green Beans", "Chick pea"
                        ),

                        "Additional Expenses" to listOf(
                            "Medical", "Tution", "Transport", "Mobil Bill", "Celibrations",
                            "Entertaining", "Clothes"
                        )
                    )

                    // 2. Start the Transaction (Makes it faster and safer)
                    db.beginTransaction()
                    try {
                        // 3. Loop through every Category
                        for ((categoryName, itemsList) in initialData) {

                            // A. Prepare Category Data
                            val catValues = ContentValues().apply {
                                put("name", categoryName)
                            }

                            // B. Insert Category and GET THE NEW ID
                            // This returns the row ID (e.g., 1 for Food, 2 for Transport)
                            val newCategoryId = db.insert("categories", SQLiteDatabase.CONFLICT_REPLACE, catValues)

                            // C. Loop through items for THIS category
                            for (itemName in itemsList) {
                                val itemValues = ContentValues().apply {
                                    put("name", itemName)
                                    put("categoryId", newCategoryId) // <--- THIS LINKS THEM!
                                    put("price", 0.0) // Required because your Entity has 'val price: Double'
                                }

                                // D. Insert the Item
                                db.insert("items", SQLiteDatabase.CONFLICT_REPLACE, itemValues)
                            }
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            })

            .fallbackToDestructiveMigration()
            .build()
    }

    // Repositories
    val planRepository: PlanRepository by lazy { PlanRepository(database.planDao()) }
    val purchaseRepository: PurchaseRepository by lazy { PurchaseRepository(database.purchaseDao()) }
    val purchaseItemRepository: PurchaseItemRepository by lazy { PurchaseItemRepository(database.purchaseItemDao()) }
    val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val itemRepository: ItemRepository by lazy { ItemRepository(database.itemDao()) }
    val planCategoryBudgetRepository: PlanCategoryBudgetRepository by lazy { PlanCategoryBudgetRepository(database.planCategoryBudgetDao()) }
}
