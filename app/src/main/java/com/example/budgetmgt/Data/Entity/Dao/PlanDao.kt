package com.example.budgetmgt.Data.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.budgetmgt.Data.Entity.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(plan: Plan): Long

    @Update
    suspend fun update(plan: Plan)

    @Delete
    suspend fun delete(plan: Plan)

    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
     fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE planId = :planId")
    suspend fun getPlanById(planId: Int): Plan?




}
