package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(preferenceManager: PreferenceManager, onBack: () -> Unit) {
    val primaryColor = colorResource(id = R.color.primaryColor)
    val bgColor = colorResource(id = R.color.neumorphic_bg)
    val greyColor = colorResource(id = R.color.greyColor)
    val borderColor = colorResource(id = R.color.borderColor)
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var expandedMonthDropdown by remember { mutableStateOf(false) }

    val todayDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val monthYearFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "History",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = bgColor
                )
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Date and Month Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today : ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                    Text(
                        text = todayDate,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(lightPurpleColor)
                            .clickable { expandedMonthDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = monthYearFormat.format(currentMonth.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMonthDropdown,
                        onDismissRequest = { expandedMonthDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        val tempCal = Calendar.getInstance()
                        for (i in 0 until 12) {
                            val monthCal = tempCal.clone() as Calendar
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = monthYearFormat.format(monthCal.time),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    currentMonth = monthCal
                                    expandedMonthDropdown = false
                                }
                            )
                            tempCal.add(Calendar.MONTH, -1)
                        }
                    }
                }
            }

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Grid
            CalendarGrid(currentMonth, primaryColor, borderColor, greyColor, preferenceManager)
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    primaryColor: Color,
    borderColor: Color,
    greyColor: Color,
    preferenceManager: PreferenceManager
) {
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, etc.
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Total items to show = offset + days
    val totalGridItems = startDayOfWeek + daysInMonth
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(totalGridItems) { index ->
            if (index < startDayOfWeek) {
                // Empty spacer for days before the 1st of the month
                // Synchronized aspectRatio to 0.8f for consistent row height
                Spacer(modifier = Modifier.aspectRatio(0.8f))
            } else {
                val day = index - startDayOfWeek + 1
                
                // In a real app, you'd fetch actual history for this day
                val hasHistory = day % 3 == 0 
                val usageTime = if (hasHistory) "${(0..3).random()}h ${(0..59).random()}m" else null

                Box(
                    modifier = Modifier
                        .aspectRatio(0.8f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (hasHistory) Color.White else lightPurpleColor)
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(2.dp))
                        .padding(4.dp)
                ) {
                    // Date indicator (top-left)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Centered content column
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hasHistory && usageTime != null) {
                            val timeParts = usageTime.split(" ")
                            Text(
                                text = timeParts[0],
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = timeParts[1],
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Medium,
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.app_icon),
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
