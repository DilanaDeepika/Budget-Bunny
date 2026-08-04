package com.example.budgetmgt


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetmgt.Data.Entity.Helper.DailyExpenseDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyBarChart(
    data: List<DailyExpenseDto>
) {
    if (data.isEmpty()) {
        Text("No history yet", color = Color.Gray, modifier = Modifier.padding(16.dp))
        return
    }

    // 1. Find the highest spent amount to scale the bars relative to it
    val maxAmount = remember(data) { data.maxOfOrNull { it.totalSpent } ?: 1.0 }

    // Scroll state for horizontal scrolling if many days
    val scrollState = rememberScrollState()

    // Chart Dimensions
    val barWidth = 40.dp
    val spacing = 16.dp
    val chartHeight = 200.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight + 40.dp) // Extra space for text below
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dayName = formatter.format(Date(item.date))

            // Calculate bar height relative to max
            // We coerceAtLeast 0.05f so even small amounts show a tiny bar
            val fillPercentage = (item.totalSpent / maxAmount).toFloat().coerceAtLeast(0.05f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = spacing)
            ) {
                // Price Label (Top)
                Text(
                    text = "${item.totalSpent.toInt()}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // The Bar
                Canvas(
                    modifier = Modifier
                        .width(barWidth)
                        .height(chartHeight * fillPercentage)
                ) {
                    drawRoundRect(
                        color = Color(0xFF03DAC5), // Teal Color
                        size = size,
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                // Date Label (Bottom)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dayName,
                    fontSize = 10.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}