package com.example.budgetmgt.Data.Dao

import androidx.room.*
import com.example.budgetmgt.Data.Entity.Helper.BudgetVsSpentDto
import com.example.budgetmgt.Data.Entity.Helper.CategoryWithAmount
import com.example.budgetmgt.Data.Entity.PlanCategoryBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanCategoryBudgetDao {

    // Insert a single record
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(planCategoryBudget: PlanCategoryBudget): Long

    // Update
    @Update
    suspend fun update(planCategoryBudget: PlanCategoryBudget)

    @Query("""
        UPDATE plan_category_budget
        SET estimatedBudget = :estimatedBudget
        WHERE planId = :planId AND categoryId = :categoryId
    """)
    suspend fun updateEstimatedBudget(planId: Int, categoryId: Int, estimatedBudget: Double)

    @Query("DELETE FROM plan_category_budget WHERE planId = :planId AND categoryId = :categoryId")
    suspend fun deleteBudgetForCategory(planId: Int, categoryId: Int)

    // Delete
    @Delete
    suspend fun delete(planCategoryBudget: PlanCategoryBudget)

    // Get all budgets for a specific plan
    @Query("SELECT * FROM plan_category_budget WHERE planId = :planId")
     fun getBudgetsForPlan(planId: Int): Flow<List<PlanCategoryBudget>>

    @Query("UPDATE plan_category_budget SET spentAmount = spentAmount  - :amount WHERE planId = :planId AND categoryId = :categoryId")
    suspend fun updateSpendAmount(planId: Int, categoryId: Int, amount: Double)


    // OPTIONAL: delete all budgets when deleting plan
    @Query("DELETE FROM plan_category_budget WHERE planId = :planId")
    suspend fun deleteBudgetsForPlan(planId: Int)

    @Query("""
        SELECT 
            c.id as categoryId, 
            c.name as categoryName, 
            pcb.estimatedBudget as allocatedAmount
        FROM categories c
        INNER JOIN plan_category_budget pcb ON c.id = pcb.categoryId
        WHERE pcb.planId = :planId
    """)
    fun getCategoriesWithBudgetForPlan(planId: Int): Flow<List<CategoryWithAmount>>

    @Query("""
        UPDATE plan_category_budget 
        SET spentAmount = spentAmount + :amount 
        WHERE planId = :planId AND categoryId = :categoryId
    """)
    suspend fun addExpenseAmount(planId: Int, categoryId: Int, amount: Double)

    @Query("""
        SELECT 
            c.id as categoryId, 
            c.name as categoryName, 
            pcb.estimatedBudget as budgetLimit, 
            pcb.spentAmount as spentAmount
        FROM plan_category_budget pcb
        INNER JOIN categories c ON c.id = pcb.categoryId
        WHERE pcb.planId = :planId
        """)
    fun getBudgetVsSpentStats(planId: Int): Flow<List<BudgetVsSpentDto>>


}
