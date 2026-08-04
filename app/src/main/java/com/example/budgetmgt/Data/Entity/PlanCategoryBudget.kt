package com.example.budgetmgt.Data.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_category_budget",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["planId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"]), Index(value = ["categoryId"])]
)
data class PlanCategoryBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int,
    val categoryId: Int,
    val estimatedBudget: Double,
    val spentAmount: Double = 0.0
)
