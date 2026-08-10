package com.example.budgetmgt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmgt.viewmodel.BudgetViewModel
import kotlinx.coroutines.flow.first

// ---------------------------------------------------------
// 1. THE HELPER CLASS (Draft Object)
// We use this for the UI only. It holds the ID and Name safely.
// ---------------------------------------------------------
data class BudgetAllocation(
    val categoryId: Int,      // Needed for Database
    val categoryName: String, // Needed for UI Display
    val amount: String = ""   // String for the TextField
)

// ... (Imports remain the same)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialPlanScreen(
    onCreateClick: (String, String, List<BudgetAllocation>) -> Unit,
    onCancelClick: () -> Unit = {},
    viewModel: BudgetViewModel,
    editPlanId: Int? = null
) {

    // --- DATA: OBSERVE CATEGORIES FROM DB ---
    val categoryList by viewModel.getAllCategories().collectAsState(initial = emptyList())

    // --- STATE VARIABLES ---
    var planName by remember { mutableStateOf("") }
    var totalBudgetStr by remember { mutableStateOf("") }

    // This list uses our helper class 'BudgetAllocation'
    var allocatedCategories by remember { mutableStateOf(listOf<BudgetAllocation>()) }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var editDataLoaded by remember(editPlanId) { mutableStateOf(false) }

    LaunchedEffect(editPlanId) {
        if (editPlanId != null && !editDataLoaded) {
            val plan = viewModel.getPlanById(editPlanId)
            val budgets = viewModel.getCategoryBudgetsForPlan(editPlanId).first()
            if (plan != null) {
                planName = plan.name
                totalBudgetStr = plan.totalBudget.toString()
                allocatedCategories = budgets.map {
                    BudgetAllocation(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        amount = it.allocatedAmount.toString()
                    )
                }
            }
            editDataLoaded = true
        }
    }

    // --- MATH CALCULATIONS ---
    val totalBudget = totalBudgetStr.toDoubleOrNull() ?: 0.0
    val currentAllocated = allocatedCategories.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val remainingBudget = totalBudget - currentAllocated

    val remainingColor = if (remainingBudget < 0) Color(0xFFFF5252) else Color(0xFF4CAF50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editPlanId == null) "Create Monthly Plan" else "Update Monthly Plan", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        // Pass the data back to AppNavigation -> ViewModel
                        onCreateClick(planName, totalBudgetStr, allocatedCategories)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),

                    // 🟢 1. ADDED VALIDATION CHECKS HERE
                    enabled = planName.isNotEmpty() &&
                            totalBudget > 0 &&
                            allocatedCategories.isNotEmpty() && // Must have at least one category
                            remainingBudget == 0.0              // Remaining must be zero
                ) {
                    Text(if (editPlanId == null) "Save Plan" else "Update Plan")
                }
            }
        }
    ) { paddingValues ->

        // ... (The rest of the UI code is exactly the same as before) ...

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SECTION 1: PLAN DETAILS ---
            item {
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Plan Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = planName,
                            onValueChange = { planName = it },
                            label = { Text("Plan Name (e.g., Nov Budget)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = totalBudgetStr,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) totalBudgetStr = it },
                            label = { Text("Total Estimated Budget") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("Rs. ") },
                            singleLine = true
                        )
                    }
                }
            }

            // --- SECTION 2: SUMMARY CARD ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Allocated", fontSize = 12.sp, color = Color.Gray)
                            Text("Rs.${currentAllocated}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Vertical Divider
                        Box(modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.Gray.copy(alpha = 0.3f)))

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "Rs.${remainingBudget}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = remainingColor
                            )
                        }
                    }
                }
            }

            // --- SECTION 3: ADD CATEGORIES ---
            item {
                Text("Allocate to Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=8.dp))

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)) {
                    OutlinedButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Category to Plan")
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 300.dp)
                    ) {
                        // Loop through the REAL Database Objects
                        categoryList.forEach { categoryObj ->

                            // Check if this ID is already in the list
                            val isAlreadyAdded = allocatedCategories.any { it.categoryId == categoryObj.id }

                            if (!isAlreadyAdded) {
                                DropdownMenuItem(
                                    text = { Text(categoryObj.name) },
                                    onClick = {
                                        // Create the DRAFT object with ID and Name
                                        val newAllocation = BudgetAllocation(
                                            categoryId = categoryObj.id,
                                            categoryName = categoryObj.name
                                        )
                                        allocatedCategories = allocatedCategories + newAllocation
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: LIST OF ADDED CATEGORIES ---
            items(allocatedCategories) { item ->
                CategoryBudgetRow(
                    item = item,
                    onAmountChanged = { newAmount ->
                        // Find the item by ID and update its amount
                        allocatedCategories = allocatedCategories.map {
                            if (it.categoryId == item.categoryId) it.copy(amount = newAmount) else it
                        }
                    },
                    onDelete = {
                        allocatedCategories = allocatedCategories.filter { it.categoryId != item.categoryId }
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryBudgetRow(
    item: BudgetAllocation,
    onAmountChanged: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.categoryName, // Display the Name
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // Budget Input
            OutlinedTextField(
                value = item.amount,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onAmountChanged(it) },
                placeholder = { Text("0") },
                modifier = Modifier
                    .width(100.dp)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF9F9F9),
                    focusedContainerColor = Color.White
                )
            )

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray)
            }
        }
    }
}
