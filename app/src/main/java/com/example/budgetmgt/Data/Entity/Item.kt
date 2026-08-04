package com.example.budgetmgt.Data.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])] // required for foreign keys
)
data class Item(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0,
    val categoryId: Int,
    val name: String,
    val price: Double
)
