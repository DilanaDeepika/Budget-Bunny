package com.example.budgetmgt.Data.Entity.Repository


import com.example.budgetmgt.Data.Dao.PlanDao
import com.example.budgetmgt.Data.Entity.Plan
import kotlinx.coroutines.flow.Flow

class PlanRepository(private val planDao: PlanDao) {

     fun getAllPlans(): Flow<List<Plan>> {
        return planDao.getAllPlans()
    }

    suspend fun getPlanById(id: Int): Plan? {
        return planDao.getPlanById(id)
    }

    suspend fun insertPlan(plan: Plan): Long{
        return planDao.insert(plan)
    }

    suspend fun updatePlan(plan: Plan) {
        planDao.update(plan)
    }

    suspend fun deletePlan(plan: Plan) {
        planDao.delete(plan)
    }


}
