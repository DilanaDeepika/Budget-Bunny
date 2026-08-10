package com.example.budgetmgt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetmgt.BudgetAllocation
import com.example.budgetmgt.Data.Entity.Category
import com.example.budgetmgt.Data.Entity.Helper.BudgetVsSpentDto
import com.example.budgetmgt.Data.Entity.Helper.CategoryWithAmount
import com.example.budgetmgt.Data.Entity.Helper.DailyExpenseDto
import com.example.budgetmgt.Data.Entity.Helper.DateUtils
import com.example.budgetmgt.Data.Entity.Item
import com.example.budgetmgt.Data.Entity.Plan
import com.example.budgetmgt.Data.Entity.PlanCategoryBudget
import com.example.budgetmgt.Data.Entity.Purchase
import com.example.budgetmgt.Data.Entity.PurchaseItem
import com.example.budgetmgt.Data.Entity.Repository.CategoryRepository
import com.example.budgetmgt.Data.Entity.Repository.ItemRepository
import com.example.budgetmgt.Data.Entity.Repository.PlanCategoryBudgetRepository
import com.example.budgetmgt.Data.Entity.Repository.PlanRepository
import com.example.budgetmgt.Data.Entity.Repository.PurchaseItemRepository
import com.example.budgetmgt.Data.Entity.Repository.PurchaseRepository
import com.example.budgetmgt.PurchaseEntryUI
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.forEach

class BudgetViewModel(
    private val planRepository: PlanRepository,
    private val purchaseRepository: PurchaseRepository,
    private val purchaseItemRepository: PurchaseItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: ItemRepository,
    private val planCategoryBudgetRepository: PlanCategoryBudgetRepository
) : ViewModel() {

    // -------------------------------
    // 🟦 CATEGORY FUNCTIONS
    // -------------------------------

     fun getAllCategories(): Flow<List<Category>> =

        categoryRepository.getAllCategories()



    // -------------------------------
    // 🟩 ITEM FUNCTIONS
    // -------------------------------

     fun getItemsByCategory(categoryId: Int): Flow<List<Item>> =
        itemRepository.getItemsByCategory(categoryId)





    // -------------------------------
    // 🟧 PLAN FUNCTIONS
    // -------------------------------

     fun getAllPlans(): Flow<List<Plan>> =
        planRepository.getAllPlans()

    suspend fun getPlanById(id: Int): Plan? =
        planRepository.getPlanById(id)





    // -------------------------------
    // 🟨 PLAN CATEGORY BUDGET
    // -------------------------------

    fun getCategoryBudgetsForPlan(planId: Int): Flow<List<CategoryWithAmount>> {
        // Correct: Just return the repository call directly.
        // No viewModelScope.launch needed here!
        return planCategoryBudgetRepository.getCategoriesWithBudgetForPlan(planId)
    }



// In BudgetViewModel.kt



    // Inside BudgetViewModel class
    fun savePlanWithAllocations(
        planName: String,
        totalBudgetStr: String,
        allocations: List<BudgetAllocation>
    ) = viewModelScope.launch {

        // 🛑 MOVE TO BACKGROUND THREAD (IO)
        withContext(Dispatchers.IO) {

            // 1. Prepare Plan
            val budgetLong = totalBudgetStr.toDoubleOrNull()?.toLong() ?: 0L
            val newPlan = Plan(
                name = planName,
                totalBudget = budgetLong,
                createdAt = System.currentTimeMillis()
            )

            // 2. Insert Plan & Get ID
            val newPlanId = planRepository.insertPlan(newPlan).toInt()

            // 3. Insert Categories
            allocations.forEach { allocation ->
                val budgetEntity = PlanCategoryBudget(
                    planId = newPlanId,
                    categoryId = allocation.categoryId,
                    estimatedBudget = allocation.amount.toDoubleOrNull() ?: 0.0,
                    // If you have a 'categoryName' field in your entity, add it here:
                    // category = allocation.categoryName
                )
                planCategoryBudgetRepository.insertBudget(budgetEntity)
            }
        }
    }


    // -------------------------------
    // 🟥 PURCHASE FUNCTIONS
    // -------------------------------

     fun getPurchasesByPlan(planId: Int): Flow<List<Purchase>> =
        purchaseRepository.getPurchasesByPlan(planId)






    // -------------------------------
    // 🟪 PURCHASE ITEM FUNCTIONS
    // -------------------------------

    fun getPlanTotalSpent(planId: Int): Flow<Double> =
        purchaseItemRepository.getTotalSpentByPlan(planId)


    // 1. Function to LOAD data for a specific date

    fun getDailyList(planId: Int, dateString: String): Flow<List<PurchaseEntryUI>> {
        // 1. Convert String "2025-11-26" -> Long (Day Number)
        val dateLong = DateUtils.toMillis(dateString)

        // 2. Get Data from DB and Map to UI Object
        return purchaseItemRepository.getDailyPurchaseItems(planId, dateLong)
            .map { dtoList ->
                dtoList.map { dto ->
                    PurchaseEntryUI(
                        id = dto.purchaseItemId.toString(),
                        category = dto.categoryName,
                        itemName = dto.itemName,
                        price = dto.price,
                        quantity = dto.quantity,
                        unit = "Item"
                    )
                }
            }
        }
    // In BudgetViewModel.kt


    fun addSingleExpense(
        planId: Int,
        dateString: String,
        categoryId: Int,
        itemName: String,
        price: Double,
        quantity: Double
    ) = viewModelScope.launch {

        // 1. Wrap in Try-Catch to catch the crash and log it instead
        try {
            withContext(Dispatchers.IO) {

                // --- DATE HANDLING (Use try-catch for parsing safety) ---
                val dateLong = DateUtils.toMillis(dateString)

                // 🛑 SAFETY CHECK: If date is 0, the format is wrong. STOP.
                if (dateLong == 0L) {
                    Log.e("BudgetViewModel", "STOPPING: Date format is invalid. UI sent: $dateString")
                    return@withContext
                }

                val cleanName = itemName.trim()

                // --- STEP 1: GET OR CREATE PURCHASE HEADER ---
                var purchaseId: Int = -1
                val existingPurchase = purchaseRepository.getPurchaseByDate(planId, dateLong)

                if (existingPurchase != null) {
                    purchaseId = existingPurchase.purchaseId
                } else {
                    val newPurchase = Purchase(planId = planId, purchasedAt = dateLong)
                    val rowId = purchaseRepository.insertPurchase(newPurchase) // Returns -1 if exists

                    if (rowId == -1L) {
                        // Race condition: It existed but we didn't catch it above. Fetch it now.
                        purchaseId = purchaseRepository.getPurchaseByDate(planId, dateLong)?.purchaseId ?: -1
                    } else {
                        purchaseId = rowId.toInt()
                    }
                }

                // --- STEP 2: GET OR CREATE ITEM (The Crash Fix) ---
                var itemId: Int = -1

                // Check if it exists FIRST
                val existingItem = itemRepository.getItemByName(cleanName, categoryId)

                if (existingItem != null) {
                    itemId = existingItem.itemId
                } else {
                    // Try to insert
                    val newItem = Item(categoryId = categoryId, name = cleanName, price = 0.0)
                    val rowId = itemRepository.insertItem(newItem) // Returns -1 if exists

                    if (rowId == -1L) {
                        // Insert failed because it exists. Fetch the ID again.
                        itemId = itemRepository.getItemByName(cleanName, categoryId)?.itemId ?: -1
                    } else {
                        itemId = rowId.toInt()
                    }
                }

                // --- STEP 3: FINAL SAFETY CHECK ---
                if (purchaseId != -1 && itemId != -1) {
                    val purchaseItemEntity = PurchaseItem(
                        purchaseId = purchaseId,
                        itemId = itemId, // We are now sure this is a VALID ID
                        quantity = quantity,
                        price = price
                    )
                    purchaseItemRepository.insertPurchaseItem(purchaseItemEntity)

                    // Update Budget
                    val totalCost = price
                    planCategoryBudgetRepository.addExpenseAmount(planId, categoryId, totalCost)

                    Log.d("BudgetViewModel", "Success: Added $cleanName to Purchase $purchaseId")
                } else {
                    // Log the error so you can see it in Logcat instead of crashing
                    Log.e("BudgetViewModel", "FAILED: Invalid IDs - PurchaseID: $purchaseId, ItemID: $itemId")
                }
            }
        } catch (e: Exception) {
            // This stops the app from closing
            Log.e("BudgetViewModel", "CRASH PREVENTED: ${e.message}")
            e.printStackTrace()
        }
    }


    // 1. DELETE FUNCTION
    fun deletePurchaseItem(
        planId: Int,
        categoryId: Int,
        oldPrice: Double,
        itemIdString: String,
        dateString: String


    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            planCategoryBudgetRepository.updateSpendAmount(planId, categoryId, oldPrice)
            // Convert String ID back to Int
            val id = itemIdString.toIntOrNull()
            if (id != null) {
                purchaseItemRepository.deletePurchaseItemById(id)
            }
            val dateLong = DateUtils.toMillis(dateString)

            purchaseRepository.checkAndDeleteEmptyPurchase(planId, dateLong)
        }
    }

    // 2. UPDATE FUNCTION

    fun updatePurchaseItem(
        planId: Int,
        categoryId: Int,
        oldPrice: Double, // Renamed for clarity
        itemIdString: String,
        newPrice: Double,
        newQty: Double
    ) = viewModelScope.launch(Dispatchers.IO) { // Launch ONCE here

        // 1. Calculate the difference
        // Logic: If Old=100, New=120. Diff = -20.
        // Query: Spent - (-20) = Spent + 20 (Correct, we spent more)
        val difference = oldPrice - newPrice

        // 2. Update the Budget Table first
        planCategoryBudgetRepository.updateSpendAmount(planId, categoryId, difference)

        // 3. Update the Item Table
        val id = itemIdString.toIntOrNull()
        if (id != null) {
            purchaseItemRepository.updatePurchaseItemFields(id, newPrice, newQty)
        }
    }
    fun getBudgetVsSpentStats(planId: Int): Flow<List<BudgetVsSpentDto>> {
        return planCategoryBudgetRepository.getBudgetVsSpent(planId)
    }

    fun getDailyStats(planId: Int): Flow<List<DailyExpenseDto>> {
        return purchaseRepository.getDailyStats(planId)
    }



    fun deletePlan(plan: Plan) = viewModelScope.launch(Dispatchers.IO) {
        planRepository.deletePlan(plan)
    }

    fun updatePlan(plan: Plan) {
        viewModelScope.launch(Dispatchers.IO) {
            planRepository.updatePlan(plan)
        }
    }

    fun updatePlanWithAllocations(
        plan: Plan,
        planName: String,
        totalBudgetStr: String,
        allocations: List<BudgetAllocation>
    ) = viewModelScope.launch(Dispatchers.IO) {
        val existingCategoryIds = planCategoryBudgetRepository
            .getCategoriesWithBudgetForPlan(plan.planId)
            .first()
            .map { it.categoryId }
            .toSet()
        val updatedCategoryIds = allocations.map { it.categoryId }.toSet()

        (existingCategoryIds - updatedCategoryIds).forEach { categoryId ->
            planCategoryBudgetRepository.deleteBudgetForCategory(plan.planId, categoryId)
        }

        allocations.forEach { allocation ->
            val amount = allocation.amount.toDoubleOrNull() ?: 0.0
            if (allocation.categoryId in existingCategoryIds) {
                planCategoryBudgetRepository.updateEstimatedBudget(plan.planId, allocation.categoryId, amount)
            } else {
                planCategoryBudgetRepository.insertBudget(
                    PlanCategoryBudget(
                        planId = plan.planId,
                        categoryId = allocation.categoryId,
                        estimatedBudget = amount
                    )
                )
            }
        }

        planRepository.updatePlan(
            plan.copy(
                name = planName.trim(),
                totalBudget = totalBudgetStr.toDoubleOrNull()?.toLong() ?: plan.totalBudget
            )
        )
    }


}
