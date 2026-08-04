package com.example.budgetmgt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetmgt.viewmodel.BudgetViewModel
import com.example.budgetmgt.viewmodel.BudgetViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database singleton
        Graph.init(this)


        lifecycleScope.launch {
            // Just trying to read something (even if empty) wakes up the DB
            // If .firstOrNull() is still red, try .collect {} instead
            Graph.database.planDao().getAllPlans().firstOrNull()
            Log.d("DEBUG", "Database opened!")
        }

        setContent {
            // Create ViewModel using factory
            val budgetViewModel: BudgetViewModel = viewModel(
                factory = BudgetViewModelFactory()
            )

            // Pass ViewModel to your Compose Navigation or Screens
            AppNavigation(budgetViewModel)
        }
    }
}



