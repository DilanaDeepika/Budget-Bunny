package com.example.budgetmgt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgetmgt.Graph


class BudgetViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(
                Graph.planRepository,
                Graph.purchaseRepository,
                Graph.purchaseItemRepository,
                Graph.categoryRepository,
                Graph.itemRepository,
                Graph.planCategoryBudgetRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
