package com.example.budgetmgt

import MonthlyCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgetmgt.Data.Entity.Plan
import com.example.budgetmgt.viewmodel.BudgetViewModel
import java.time.Instant
import java.time.ZoneId

// Colors
val PrimaryPurple = Color(0xFF6200EE)
val LightPurple = Color(0xFFE8DDFF)
val TealAccent = Color(0xFF03DAC5)
val DangerRed = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAppScreen(
    onAddBudgetClick: () -> Unit,
    navController: NavController,
    viewModel: BudgetViewModel
) {
    // 1. GET ALL PLANS
    val availablePlansList by viewModel.getAllPlans().collectAsState(initial = emptyList())

    // 2. STATE FOR SELECTION
    var selectedPlanId by remember { mutableIntStateOf(-1) }
    var isPlanMenuExpanded by remember { mutableStateOf(false) }

    // 3. AUTO-SELECT LOGIC
    LaunchedEffect(availablePlansList) {
        if (availablePlansList.isNotEmpty()) {
            val selectionExists = availablePlansList.any { it.planId == selectedPlanId }
            if (selectedPlanId == -1 || !selectionExists) {
                selectedPlanId = availablePlansList.last().planId
            }
        }
    }

    // 4. FIND THE PLAN OBJECT
    val selectedPlanObj = remember(availablePlansList, selectedPlanId) {
        availablePlansList.find { it.planId == selectedPlanId }
    }

    // 5. GET VARIABLES SAFELY
    val currentPlanId = selectedPlanObj?.planId ?: 0
    val totalBudget = selectedPlanObj?.totalBudget?.toDouble() ?: 0.0

    // 6. OBSERVE SPENT AMOUNT
    val planTotalSpent by remember(currentPlanId) {
        viewModel.getPlanTotalSpent(currentPlanId)
    }.collectAsState(initial = 0.0)

    val remainingAmount = totalBudget - planTotalSpent

    // 7. GET PURCHASE DATES FOR CALENDAR
    val purchaseList by remember(currentPlanId) {
        viewModel.getPurchasesByPlan(currentPlanId)
    }.collectAsState(initial = emptyList())

    val daysWithPurchases = remember(purchaseList) {
        purchaseList.map {
            Instant.ofEpochMilli(it.purchasedAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    //  8. GET CHART DATA (New addition)
    // Fetch stats grouped by category for the pie chart
    val categoryStats by remember(currentPlanId) {
        viewModel.getBudgetVsSpentStats(currentPlanId)
    }.collectAsState(initial = emptyList())

    //  9. GET DAILY HISTORY CHART DATA (New Addition)
    val dailyHistory by remember(currentPlanId) {
        viewModel.getDailyStats(currentPlanId)
    }.collectAsState(initial = emptyList())


    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<Plan?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (availablePlansList.isEmpty()) {
                        Text("Budget Bunny", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isPlanMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // --- HEADER (SELECTED PLAN) ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedPlanObj?.name ?: "Select Plan",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isPlanMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Plan",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text("Tap to switch plan", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))

                            // --- DROPDOWN MENU ---
                            DropdownMenu(
                                expanded = isPlanMenuExpanded,
                                onDismissRequest = { isPlanMenuExpanded = false },
                                offset = DpOffset(x = (-80).dp, y = 0.dp),
                                modifier = Modifier
                                    .background(Color.White)
                                    .width(250.dp)
                            ) {
                                availablePlansList.forEach { plan ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // 1. PLAN NAME
                                                Text(
                                                    text = plan.name,
                                                    fontWeight = if (plan.planId == selectedPlanId) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (plan.planId == selectedPlanId) PrimaryPurple else Color.Black,
                                                    modifier = Modifier.weight(1f),
                                                    // 🟢 2. FIX: Center the text itself
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )


                                                IconButton(
                                                    onClick = {
                                                        isPlanMenuExpanded = false
                                                        navController.navigate("editPlan/${plan.planId}")
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Update plan",
                                                        tint = PrimaryPurple,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                // DELETE
                                                IconButton(
                                                    onClick = {
                                                        planToDelete = plan
                                                        showDeleteConfirmDialog = true
                                                        isPlanMenuExpanded = false
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = DangerRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedPlanId = plan.planId
                                            isPlanMenuExpanded = false
                                        }
                                    )
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                },
                actions = {
                    Text(
                        text = "v1.1.3",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryPurple),
                navigationIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudgetClick,
                containerColor = TealAccent,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        }
    ) { padding ->


        if (availablePlansList.isEmpty()) {

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                EmptyStateView(onAddBudgetClick)
            }

        } else {

            // PATH 2: DASHBOARD CONTENT (Scrollable)
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
            ) {
                // --- DASHBOARD CONTENT ---

                // 1. Calendar
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    MonthlyCalendar(
                        modifier = Modifier.padding(16.dp),
                        purchaseDates = daysWithPurchases,
                        onDateClick = { dateString ->
                            navController.navigate(Screen.DailyPurchaseScreen.createRoute(dateString, currentPlanId.toDouble()))
                        }
                    )
                }

                // 2. Statistics Title
                Text(
                    text = "Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // 3. Summary Card (Progress Bar)
                BudgetSummaryCard(
                    total = totalBudget,
                    spent = planTotalSpent,
                    remaining = remainingAmount
                )

                // 🟢 4. PIE CHART SECTION (Added Here)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Spending Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // This uses the PieChart component we created earlier
                        BudgetBarChart(data = categoryStats)
                    }
                }

                // Extra space at bottom so FAB doesn't cover content
                Spacer(modifier = Modifier.height(80.dp))


                Text(
                    text = "Daily History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        // This uses the DailyBarChart component we created
                        DailyBarChart(data = dailyHistory)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDeleteConfirmDialog && planToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Plan?") },
            text = { Text("Are you sure you want to delete '${planToDelete?.name}'? This will delete all expenses associated with it.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Call ViewModel to delete
                        viewModel.deletePlan(planToDelete!!)

                        // If we deleted the CURRENT plan, reset selection
                        if (planToDelete!!.planId == selectedPlanId) {
                            selectedPlanId = -1 // Logic will auto-select next one
                        }

                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}
