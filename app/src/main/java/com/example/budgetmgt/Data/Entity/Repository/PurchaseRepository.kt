package com.example.budgetmgt.Data.Entity.Repository


import android.util.Log
import com.example.budgetmgt.Data.Dao.PurchaseDao
import com.example.budgetmgt.Data.Entity.Helper.DailyExpenseDto
import com.example.budgetmgt.Data.Entity.Purchase
import kotlinx.coroutines.flow.Flow

class PurchaseRepository(private val purchaseDao: PurchaseDao) {

     fun getPurchasesByPlan(planId: Int): Flow<List<Purchase>> =
        purchaseDao.getPurchasesByPlan(planId)


    suspend fun getPurchaseByDate(planId: Int, dateLong: Long): Purchase? {
        return purchaseDao.getPurchaseByDate(planId, dateLong)
    }

    suspend fun insertPurchase(purchase: Purchase): Long =
        purchaseDao.insertPurchase(purchase)


// In PurchaseRepository.kt

    suspend fun checkAndDeleteEmptyPurchase(planId: Int, dateLong: Long) {
        // 1. This now uses the JOIN query, so it counts actual ITEMS
        val count = purchaseDao.getItemCountForDate(planId, dateLong)

        Log.d("PurchaseRepo", "Date: $dateLong has $count items remaining.")

        // 2. If 0 items are left, the day is empty. Delete the Header.
        if (count == 0) {
            purchaseDao.deleteDailyPurchaseRecord(planId, dateLong)
            Log.d("PurchaseRepo", "Date is empty. Deleted Purchase Header.")
        }
    }

    fun getDailyStats(planId: Int): Flow<List<DailyExpenseDto>> {
        return purchaseDao.getDailyStats(planId)
    }


}
