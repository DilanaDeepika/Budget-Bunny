package com.example.budgetmgt.Data.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.budgetmgt.Data.Entity.Helper.DailyExpenseDto
import com.example.budgetmgt.Data.Entity.Purchase
import kotlinx.coroutines.flow.Flow


@Dao
interface PurchaseDao {

    @Insert
    suspend fun insertPurchase(purchase: Purchase): Long

    @Query("SELECT * FROM purchases WHERE planId = :planId")
     fun getPurchasesByPlan(planId: Int): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE planId = :planId AND purchasedAt = :dateLong LIMIT 1")
    suspend fun getPurchaseByDate(planId: Int, dateLong: Long): Purchase?

    @Query("""
        SELECT COUNT(pi.id) 
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.purchaseId
        WHERE p.planId = :planId AND p.purchasedAt = :dateLong
    """)
    suspend fun getItemCountForDate(planId: Int, dateLong: Long): Int

    @Query("SELECT * FROM purchases WHERE purchaseId = :purchaseId")
     fun getPurchaseById(purchaseId: Int): Purchase?

    @Query("""
        SELECT 
            p.purchasedAt as date, 
            COALESCE(SUM(pi.price), 0.0) as totalSpent
        FROM purchases p
        LEFT JOIN purchase_items pi ON p.purchaseId = pi.purchaseId
        WHERE p.planId = :planId
        GROUP BY p.purchasedAt
        ORDER BY p.purchasedAt ASC
    """)
    fun getDailyStats(planId: Int): Flow<List<DailyExpenseDto>>

    @Query("DELETE FROM purchases WHERE planId = :planId AND purchasedAt = :date")
    suspend fun deleteDailyPurchaseRecord(planId: Int, date: Long)

    @Update
    suspend fun updatePurchase(purchase: Purchase)
}
