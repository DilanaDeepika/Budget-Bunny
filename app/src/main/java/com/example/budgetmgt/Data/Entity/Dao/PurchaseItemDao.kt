package com.example.budgetmgt.Data.Entity.Dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgetmgt.Data.Entity.Helper.PurchaseContextDto
import com.example.budgetmgt.Data.Entity.Helper.PurchaseDetailDto
import com.example.budgetmgt.Data.Entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchaseItem(purchaseItem: PurchaseItem): Long    // OK

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Int): Flow<List<PurchaseItem>>


    @Query("""
        SELECT 
            pi.id as purchaseItemId,
            i.name as itemName,
            c.name as categoryName,
            pi.quantity,
            pi.price
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.purchaseId
        INNER JOIN items i ON pi.itemId = i.itemId
        INNER JOIN categories c ON i.categoryId = c.id
        WHERE p.planId = :planId AND p.purchasedAt = :dateLong
    """)
    fun getDailyPurchaseItems(planId: Int, dateLong: Long): Flow<List<PurchaseDetailDto>>


    @Query("""
        SELECT COALESCE(SUM(pi.price), 0.0)
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.purchaseId
        WHERE p.planId = :planId
    """)
    fun getTotalSpentByPlan(planId: Int): Flow<Double>

    @Query("DELETE FROM purchase_items WHERE id = :id")
    suspend fun deleteItemsUnderPurchase(id: Int)

    // 🟢 2. UPDATE Single Item (Price & Quantity)
    @Query("UPDATE purchase_items SET price = :price, quantity = :quantity WHERE id = :id")
    suspend fun updatePurchaseItem(id: Int, price: Double, quantity: Double)

    @Query("""
        SELECT 
            p.planId,
            i.categoryId,
            pi.price,
            pi.quantity
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.purchaseId
        INNER JOIN items i ON pi.itemId = i.itemId
        WHERE pi.id = :purchaseItemId
    """)
    suspend fun getPurchaseContext(purchaseItemId: Int): PurchaseContextDto?



    @Query("UPDATE purchase_items SET price = :price, quantity = :quantity WHERE id = :id")
    suspend fun updatePurchaseItemFields(id: Int, price: Double, quantity: Double)


    @Query("SELECT COUNT(*) FROM purchases WHERE planId = :planId AND purchasedAt = :dateLong")
    suspend fun getItemCountForDate(planId: Int, dateLong: Long): Int

    @Query("SELECT  COUNT(*) FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemCountForPurchase(purchaseId: Int): Int

}

