package com.example.budgetmgt.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey(autoGenerate = true) val planId: Int = 0,
    val name: String,
    val totalBudget: Long,
    val createdAt: Long = System.currentTimeMillis()
)
