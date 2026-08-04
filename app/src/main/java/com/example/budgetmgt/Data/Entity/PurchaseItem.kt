package com.example.budgetmgt.Data.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["purchaseId"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),

    ],
    indices = [Index("purchaseId"), Index("itemId")]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val purchaseId: Int,
    val itemId: Int,
    val quantity: Double,
    val price: Double
)