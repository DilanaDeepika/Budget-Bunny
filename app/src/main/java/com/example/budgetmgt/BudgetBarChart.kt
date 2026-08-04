package com.example.budgetmgt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmgt.Data.Entity.Helper.BudgetVsSpentDto

@Composable
fun BudgetBarChart(
    data: List<BudgetVsSpentDto>
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        data.forEach { item ->
            val remaining = item.budgetLimit - item.spentAmount

            // Logic: If spent > budget, bar is Red. Else, Purple/Green.
            val isOverBudget = item.spentAmount > item.budgetLimit
            val barColor = if (isOverBudget) Color(0xFFFF5252) else Color(0xFF6200EE)
            val trackColor = Color(0xFFE0E0E0)

            // Calculate Fill Percentage (Cap at 100% for drawing purposes)
            val progress = if (item.budgetLimit > 0) (item.spentAmount / item.budgetLimit).toFloat() else 0f
            val drawProgress = progress.coerceIn(0f, 1f)

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // TEXT INFO (Left Side)
                Column(modifier = Modifier.weight(0.35f)) {
                    Text(text = item.categoryName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = if(remaining < 0) "Over by Rs.${-remaining.toInt()}" else "Rs.${remaining.toInt()} left",
                        fontSize = 10.sp,
                        color = if(remaining < 0) Color.Red else Color.Gray
                    )
                }

                // BAR CHART (Right Side)
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(30.dp) // Height of the bar area
                        .padding(start = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = 12.dp.toPx()
                        val centerY = size.height / 2 - barHeight / 2

                        // 1. Draw Background Bar (Limit)
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(0f, centerY),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        // 2. Draw Foreground Bar (Spent)
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(0f, centerY),
                            size = Size(size.width * drawProgress, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
            }
        }
    }
}