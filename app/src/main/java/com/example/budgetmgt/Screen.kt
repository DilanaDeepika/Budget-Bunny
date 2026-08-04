package com.example.budgetmgt

sealed class Screen(val route: String) {
    object DailyPurchaseScreen : Screen("dailyPurchases/{dateString}/{planID}") {
        fun createRoute(dateString: String, planId: Double) = "dailyPurchases/$dateString/$planId"
    }
}
