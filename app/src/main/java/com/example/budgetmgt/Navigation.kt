package com.example.budgetmgt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.budgetmgt.viewmodel.BudgetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    budgetViewModel: BudgetViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            BudgetAppScreen(
                onAddBudgetClick = { navController.navigate("initialPlan") },
                navController = navController,
                viewModel = budgetViewModel
            )
        }
        composable("initialPlan") {
            InitialPlanScreen(
                // --- FIX STARTS HERE ---
                // The InitialPlanScreen now returns 3 values. We must accept them in the lambda.
                onCreateClick = { planName, totalBudget, categories ->

                    budgetViewModel.savePlanWithAllocations(
                        planName = planName,
                        totalBudgetStr = totalBudget,
                        allocations = categories
                    )
                    budgetViewModel.viewModelScope.launch {
                        delay(100)
                        navController.popBackStack()
                    }
                },
                // --- FIX ENDS HERE ---

                onCancelClick = { navController.popBackStack() },
                viewModel = budgetViewModel
            )
        }

        composable(
            route = "editPlan/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.IntType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("planId") ?: return@composable
            val plan by budgetViewModel.getAllPlans().collectAsState(initial = emptyList())
            val planToEdit = plan.find { it.planId == planId }

            if (planToEdit != null) {
                InitialPlanScreen(
                    editPlanId = planId,
                    onCreateClick = { planName, totalBudget, categories ->
                        budgetViewModel.updatePlanWithAllocations(
                            plan = planToEdit,
                            planName = planName,
                            totalBudgetStr = totalBudget,
                            allocations = categories
                        )
                        navController.popBackStack()
                    },
                    onCancelClick = { navController.popBackStack() },
                    viewModel = budgetViewModel
                )
            }
        }


        composable(
            // The route string MUST match exactly what you defined in Screen object
            route = "dailyPurchases/{dateString}/{planID}",

            // Define the arguments so Navigation knows how to parse them
            arguments = listOf(
                navArgument("dateString") { type = NavType.StringType },
                navArgument("planID") { type = NavType.StringType }
                // We use StringType for safety, then convert to Double manually
            )
        ) { backStackEntry ->

            // Extract arguments safely
            val dateString = backStackEntry.arguments?.getString("dateString") ?: "Unknown Date"
            val planIdStr = backStackEntry.arguments?.getString("planID") ?: "0.0"
            val planIdDouble = planIdStr.toDoubleOrNull() ?: 0.0

            // Pass them to the screen
            DailyPurchaseScreen(
                planeID = planIdDouble,
                dateString = dateString,
                navController = navController,
                viewModel = budgetViewModel
            )
        }
    }
}
