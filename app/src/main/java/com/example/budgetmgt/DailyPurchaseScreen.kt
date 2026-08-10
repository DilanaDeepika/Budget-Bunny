package com.example.budgetmgt


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgetmgt.Data.Entity.Item
import com.example.budgetmgt.viewmodel.BudgetViewModel

// 1. DATA CLASS FOR UI
data class PurchaseEntryUI(
    val id: String,
    val category: String,
    val itemName: String,
    val price: Double,
    val quantity: Double,
    val unit: String
)

// Helper class
data class CategoryData(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPurchaseScreen(
    planeID: Double,
    dateString: String,
    navController: NavController,
    viewModel: BudgetViewModel
) {
    val planIdInt = planeID.toInt()

    // --- 1. FETCH DATA ---
    val budgetedCategories by viewModel.getCategoryBudgetsForPlan(planIdInt).collectAsState(initial = emptyList())

    val categoryStats by remember(planIdInt) {
        viewModel.getBudgetVsSpentStats(planIdInt)
    }.collectAsState(initial = emptyList())



    // --- 2. CREATE LOOKUP MAP ---
    val dropdownMap = remember(categoryStats, budgetedCategories) {
        categoryStats.mapNotNull { stat ->
            val match = budgetedCategories.find { it.categoryName == stat.categoryName }
            if (match != null) {
                val remaining = stat.budgetLimit - stat.spentAmount
                // The Display String
                val displayStr = "${stat.categoryName} (Left: Rs.${remaining.toInt()})"
                // Map String -> Data
                displayStr to CategoryData(match.categoryId, match.categoryName)
            } else {
                null
            }
        }.toMap()
    }

    val categoryOptions = dropdownMap.keys.toList()
    val units = listOf("Item", "Kg", "g", "L", "ml")

    // --- 3. SUMMARY DATA ---
    val realTotalSpent by viewModel.getPlanTotalSpent(planIdInt).collectAsState(initial = 0.0)
    var realTotalBudget by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(planIdInt) {
        val plan = viewModel.getPlanById(planIdInt)
        realTotalBudget = plan?.totalBudget?.toDouble() ?: 0.0
    }
    val realRemaining = realTotalBudget - realTotalSpent

    // --- 4. UI STATE: SELECTION (THE FIX) ---

    // 🟢 FIX: Store the ID, not the String. ID is stable.
    var selectedCategoryId by remember { mutableIntStateOf(-1) }

    // 🟢 FIX: Find the Text dynamically based on the ID
    // Whenever 'dropdownMap' updates (due to spending), this recalculates automatically!
    val currentDropdownText = dropdownMap.entries.find { it.value.id == selectedCategoryId }?.key ?: ""


    // --- 5. OBSERVE ITEMS ---
    val itemsList by if (selectedCategoryId != -1) {
        viewModel.getItemsByCategory(selectedCategoryId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Item>()) }
    }

    val itemNames = itemsList.map { it.name } + "+ Add New Item"

    // --- INPUTS ---
    var selectedItemName by remember { mutableStateOf("") }
    var customItemName by remember { mutableStateOf("") }
    val isCustomItemMode = selectedItemName == "+ Add New Item"

    var priceInput by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("Item") }


    



    val dailyPurchases by viewModel.getDailyList(planIdInt, dateString)
        .collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PurchaseEntryUI?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Spending", fontSize = 16.sp, color = Color.White)
                        Text(dateString, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EE))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // SUMMARY
            DailySummaryInfo(
                totalBudget = realTotalBudget,
                totalSpent = realTotalSpent,
                remaining = realRemaining
            )

            // --- ADD NEW PURCHASE CARD ---
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Expense", fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Dropdown
                    SmartDropdown(
                        label = "Select Category",
                        options = categoryOptions,
                        // 🟢 FIX: Use the dynamically calculated text
                        selectedOption = currentDropdownText,
                        onOptionSelected = { selectedString ->
                            // 🟢 FIX: Look up the ID from the string and store the ID
                            val data = dropdownMap[selectedString]
                            if (data != null) {
                                selectedCategoryId = data.id
                                selectedItemName = ""
                                customItemName = ""
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Item Dropdown
                    SmartDropdown(
                        label = "Select Item",
                        options = if (selectedCategoryId != -1) itemNames else emptyList(),
                        selectedOption = selectedItemName,
                        onOptionSelected = { selectedItemName = it },
                        isEnabled = selectedCategoryId != -1
                    )

                    // Custom Name
                    if (isCustomItemMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customItemName,
                            onValueChange = { customItemName = it },
                            label = { Text("Enter New Item Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price & Qty
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min), // 🟢 1. Forces all children to be the same height
                        verticalAlignment = Alignment.CenterVertically // 🟢 2. Aligns them perfectly in the middle
                    ) {




                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) quantityInput = it },
                            label = { Text("Qty") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // For SmartDropdown, ensure it fills the height provided by the Row
                        SmartDropdown(
                            label = "Unit",
                            options = units,
                            selectedOption = selectedUnit,
                            onOptionSelected = { selectedUnit = it },
                            modifier = Modifier
                                .width(70.dp)
                                .fillMaxHeight() // 🟢 4. Make dropdown fill the row height
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) priceInput = it },
                            // 🟢 3. FIX: Prevent the Label from wrapping to a second line
                            label = {
                                Text(
                                    "Cost (Rs.)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))


                        // Add Button
                        Button(
                            onClick = {
                                val finalItemName = if (isCustomItemMode) customItemName else selectedItemName
                                val priceVal = priceInput.toDoubleOrNull()
                                val qtyVal = quantityInput.toDoubleOrNull()

                                if (selectedCategoryId != -1 && finalItemName.isNotEmpty() &&
                                    priceVal != null && qtyVal != null) {

                                    viewModel.addSingleExpense(
                                        planId = planIdInt,
                                        dateString = dateString,
                                        categoryId = selectedCategoryId,
                                        itemName = finalItemName,
                                        price = priceVal,
                                        quantity = qtyVal
                                    )

                                    // Clear Inputs only (Dropdown stays selected but updates text automatically)
                                    selectedItemName = ""
                                    customItemName = ""
                                    priceInput = ""
                                    quantityInput = "1"
                                    selectedUnit = "Item"
                                }
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }
                }
            }

            // --- LIST ---
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
// Inside DailyPurchaseScreen -> LazyColumn

                items(dailyPurchases) { item ->  // <--- 'item' is defined here
                    PurchaseItemRow(
                        item = item,
                        onEditClick = {
                            itemToEdit = item // This prepares for editing
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            // 🟢 FIX: Use 'item', not 'itemToEdit!!'
                            val itemCategoryName = item.category

                            // Find the ID based on the name
                            val correctCategoryId = dropdownMap.entries.find {
                                it.value.name == itemCategoryName
                            }?.value?.id ?: -1

                            if (correctCategoryId != -1) {
                                viewModel.deletePurchaseItem(
                                    planId = planIdInt,
                                    categoryId = correctCategoryId,
                                    oldPrice = item.price, // 🟢 FIX: Use 'item.price'
                                    itemIdString = item.id,
                                    dateString = dateString
                                )
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- EDIT DIALOG ---
    if (showEditDialog && itemToEdit != null) {
        EditPurchaseDialog(
            item = itemToEdit!!,
            units = units,
            onDismiss = { showEditDialog = false },
            onConfirm = { newPrice, newQty, newUnit ->

                // 1. Find the Category ID for the item being edited
                // We look for the entry in your map where the name matches
                val itemCategoryName = itemToEdit!!.category
                val correctCategoryId = dropdownMap.entries.find {
                    it.value.name == itemCategoryName
                }?.value?.id ?: -1

                if (correctCategoryId != -1) {
                    viewModel.updatePurchaseItem(
                        planId = planIdInt,
                        categoryId = correctCategoryId, // <--- USE CORRECT ID
                        oldPrice = itemToEdit!!.price,  // <--- USE ITEM PRICE, NOT INPUT
                        itemIdString = itemToEdit!!.id,
                        newPrice = newPrice,
                        newQty = newQty
                    )
                }

                showEditDialog = false
            }
        )
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun PurchaseItemRow(
    item: PurchaseEntryUI,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.category, fontSize = 12.sp, color = Color.Gray)
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    "Rs.${item.price}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    fontSize = 16.sp
                )
                val qtyString = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                val unitDisplay = if (item.unit == "Item") "x" else " ${item.unit}"
                Text(
                    text = "$qtyString$unitDisplay",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                }
            }
        }
    }
}

@Composable
fun EditPurchaseDialog(
    item: PurchaseEntryUI,
    units: List<String>,
    onDismiss: () -> Unit,
    onConfirm: ( Double, Double, String) -> Unit
) {
    var newPrice by remember { mutableStateOf(item.price.toString()) }
    var newQty by remember { mutableStateOf(item.quantity.toString()) }
    var newUnit by remember { mutableStateOf(item.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Purchase") },
        text = {
            Column {
                Text("Item: ${item.itemName}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newPrice = it },
                    label = { Text("Total Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newQty,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newQty = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SmartDropdown(
                        label = "Unit",
                        options = units,
                        selectedOption = newUnit,
                        onOptionSelected = { newUnit = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        newPrice.toDoubleOrNull() ?: item.price,
                        newQty.toDoubleOrNull() ?: item.quantity,
                        newUnit
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color.White
    )
}

@Composable
fun SmartDropdown(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isEnabled: Boolean = true,

) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isEnabled) { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF0F0F0),
                disabledBorderColor = Color.LightGray
            ),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = isEnabled) { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(150.dp).background(Color.White)
        ) {
            options.forEach { option ->
                val isAddOption = option == "+ Add New Item"
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if(isAddOption) Color(0xFF6200EE) else Color.Black,
                            fontWeight = if(isAddOption) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DailySummaryInfo(
    totalBudget: Double,
    totalSpent: Double,
    remaining: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp), // Padding matches other cards
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)), // Light Indigo background
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Plan Budget", fontSize = 12.sp, color = Color.Gray)
                Text("Rs.${totalBudget.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))

            // Column 2: Spent
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Spent", fontSize = 12.sp, color = Color.Gray)
                Text("Rs.${totalSpent.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFF5252))
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))

            // Column 3: Left
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remaining", fontSize = 12.sp, color = Color.Gray)
                Text("Rs.${remaining.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4CAF50))
            }
        }
    }
}