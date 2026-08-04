package com.example.budgetmgt.Data.Entity.Repository

import com.example.budgetmgt.Data.Entity.Dao.PurchaseItemDao
import com.example.budgetmgt.Data.Entity.Helper.PurchaseDetailDto
import com.example.budgetmgt.Data.Entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

class PurchaseItemRepository(private val purchaseItemDao: PurchaseItemDao) {


    suspend fun insertPurchaseItem(item: PurchaseItem): Long =
        purchaseItemDao.insertPurchaseItem(item)

    suspend fun deletePurchaseItemById(id: Int) {
        purchaseItemDao.deleteItemsUnderPurchase(id)
    }

    suspend fun updatePurchaseItemFields(id: Int, price: Double, quantity: Double) {
        purchaseItemDao.updatePurchaseItem(id, price, quantity)
    }

    fun getTotalSpentByPlan(planId: Int): Flow<Double> {
        return purchaseItemDao.getTotalSpentByPlan(planId)
    }
    fun getDailyPurchaseItems(planId: Int, dateLong: Long): Flow<List<PurchaseDetailDto>> {
        return purchaseItemDao.getDailyPurchaseItems(planId, dateLong)
    }



}