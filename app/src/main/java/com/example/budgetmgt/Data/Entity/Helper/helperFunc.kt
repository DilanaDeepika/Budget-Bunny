package com.example.budgetmgt.Data.Entity.Helper

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale


data class CategoryWithAmount(
    val categoryId: Int,
    val categoryName: String,
    val allocatedAmount: Double
)

data class PurchaseDetailDto(
    val purchaseItemId: Int,
    val itemName: String,
    val categoryName: String,
    val quantity: Double,
    val price: Double
)

data class BudgetVsSpentDto(
    val categoryName: String,
    val budgetLimit: Double,
    val spentAmount: Double
)

data class DailyExpenseDto(
    val date: Long,
    val totalSpent: Double
)


data class PurchaseContextDto(
    val planId: Int,
    val categoryId: Int,
    val price: Double,
    val quantity: Double
)


fun convertDateToMillis(dateString: String): Long {
    return try {
        // ⚠️ IMPORTANT: Change "yyyy-MM-dd" to match your actual date format
        // If your app uses "28/11/2025", change this to "dd/MM/yyyy"
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.parse(dateString)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

object DateUtils {
    private const val DATE_PATTERN = "yyyy-MM-dd"

    fun toMillis(dateString: String): Long {
        return try {
            val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            formatter.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            Log.e("DateUtils", "Error parsing '$dateString': ${e.message}")
            0L
        }
    }
}