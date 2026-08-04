package com.example.budgetmgt.Data.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["planId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) val purchaseId: Int = 0,
    val planId: Int,
    val purchasedAt: Long = System.currentTimeMillis()

)
