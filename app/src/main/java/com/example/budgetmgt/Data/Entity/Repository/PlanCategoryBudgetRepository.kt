package com.example.budgetmgt.Data.Entity.Repository

import com.example.budgetmgt.Data.Dao.PlanCategoryBudgetDao
import com.example.budgetmgt.Data.Entity.Helper.BudgetVsSpentDto
import com.example.budgetmgt.Data.Entity.Helper.CategoryWithAmount
import com.example.budgetmgt.Data.Entity.PlanCategoryBudget
import kotlinx.coroutines.flow.Flow

class PlanCategoryBudgetRepository(private val planCategoryBudgetDao: PlanCategoryBudgetDao) {

    suspend fun insertBudget(planCategoryBudget: PlanCategoryBudget) =
        planCategoryBudgetDao.insert(planCategoryBudget)

    suspend fun updateSpendAmount(planId: Int, categoryId: Int, amount: Double) =
        planCategoryBudgetDao.updateSpendAmount(planId,categoryId,amount)

    suspend fun updateEstimatedBudget(planId: Int, categoryId: Int, estimatedBudget: Double) =
        planCategoryBudgetDao.updateEstimatedBudget(planId, categoryId, estimatedBudget)

    suspend fun deleteBudgetForCategory(planId: Int, categoryId: Int) =
        planCategoryBudgetDao.deleteBudgetForCategory(planId, categoryId)

    fun getCategoriesWithBudgetForPlan(planId: Int): Flow<List<CategoryWithAmount>> {
        return planCategoryBudgetDao.getCategoriesWithBudgetForPlan(planId)
    }

    suspend fun addExpenseAmount(planId: Int, categoryId: Int, amount: Double) {
        planCategoryBudgetDao.addExpenseAmount(planId, categoryId, amount)
    }
    fun getBudgetVsSpent(planId: Int): Flow<List<BudgetVsSpentDto>> {
        return planCategoryBudgetDao.getBudgetVsSpentStats(planId)
    }

}
