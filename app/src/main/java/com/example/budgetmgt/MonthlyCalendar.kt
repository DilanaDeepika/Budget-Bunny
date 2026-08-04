import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Make sure these imports match your project structure
import com.example.budgetmgt.LightPurple
import com.example.budgetmgt.PrimaryPurple
import com.example.budgetmgt.TealAccent
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyCalendar(
    modifier: Modifier = Modifier,
    purchaseDates: List<LocalDate>,
    initialMonth: YearMonth = YearMonth.now(), // Allow passing a starting month
    onDateClick: (String) -> Unit,
    // Optional: Add a callback if parent needs to know when month changes to load new data
    // onMonthChanged: (YearMonth) -> Unit = {}
) {
    val today = LocalDate.now()

    // --- 1. STATE FOR CURRENTLY DISPLAYED MONTH ---
    // This holds the month we are currently looking at.
    var currentDisplayedMonth by remember { mutableStateOf(initialMonth) }

    // State for the dropdown menu visibility
    var isMonthMenuExpanded by remember { mutableStateOf(false) }

    // State for the selected date (stays the same)
    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    // --- Calculations based on the stateful currentDisplayedMonth ---
    val daysInMonth = currentDisplayedMonth.lengthOfMonth()
    // Make Sunday = 0 for grid alignment
    val firstDayOfWeek = currentDisplayedMonth.atDay(1).dayOfWeek.value % 7

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Column(modifier = modifier.fillMaxWidth()) {

        // --- 2. UPDATED MONTH HEADER WITH NAVIGATION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Changed to SpaceBetween for arrows
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Month Button
            IconButton(onClick = {
                currentDisplayedMonth = currentDisplayedMonth.minusMonths(1)
                // onMonthChanged(currentDisplayedMonth) // Call callback if used
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = PrimaryPurple
                )
            }

            // Month Name (Clickable Box for Dropdown)
            Box {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { isMonthMenuExpanded = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentDisplayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentDisplayedMonth.year}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Month",
                        tint = PrimaryPurple
                    )
                }

                // --- 3. DROPDOWN MENU ---
                DropdownMenu(
                    expanded = isMonthMenuExpanded,
                    onDismissRequest = { isMonthMenuExpanded = false },
                    modifier = Modifier.heightIn(max = 300.dp) // Limit height so it scrolls
                ) {
                    // List all 12 months for the currently selected year
                    Month.values().forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    fontWeight = if (month == currentDisplayedMonth.month) FontWeight.Bold else FontWeight.Normal,
                                    color = if (month == currentDisplayedMonth.month) PrimaryPurple else Color.Black
                                )
                            },
                            onClick = {
                                // Update the state to the new month within the same year
                                currentDisplayedMonth = currentDisplayedMonth.withMonth(month.value)
                                isMonthMenuExpanded = false
                                // onMonthChanged(currentDisplayedMonth) // Call callback if used
                            }
                        )
                    }
                }
            }

            // Next Month Button
            IconButton(onClick = {
                currentDisplayedMonth = currentDisplayedMonth.plusMonths(1)
                // onMonthChanged(currentDisplayedMonth) // Call callback if used
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = PrimaryPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday labels (No changes here)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid (Minor update to use currentDisplayedMonth)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Empty boxes for offset
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Actual days
            items(daysInMonth) { index ->
                val day = index + 1
                // 🟢 IMPORTANT: Use currentDisplayedMonth here, not 'today'
                val date = currentDisplayedMonth.atDay(day)

                // Highlight today only if we are viewing the current month/year
                val isToday = date == today

                val isSelected = selectedDate == date
                val hasPurchase = purchaseDates.contains(date)

                // Determine Background Color
                val bgColor = when {
                    isSelected -> TealAccent
                    hasPurchase -> LightPurple
                    else -> Color.Transparent
                }

                // Determine Text Color
                val txtColor = if (isSelected) Color.White else Color.Black

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(
                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                            color = if (isToday) PrimaryPurple else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            // Update selected date based on the displayed month
                            selectedDate = date
                            val dateString = date.format(formatter)
                            onDateClick(dateString)
                        }
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = txtColor
                    )
                }
            }
        }
    }
}