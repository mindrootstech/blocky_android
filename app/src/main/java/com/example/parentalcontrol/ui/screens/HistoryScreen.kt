package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.DetailedSession
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(preferenceManager: PreferenceManager, onBack: () -> Unit) {
    val primaryColor = colorResource(id = R.color.primaryColor)
    val bgColor = colorResource(id = R.color.neumorphic_bg)
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)
    val borderColor = colorResource(id = R.color.borderColor)

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var expandedMonthDropdown by remember { mutableStateOf(false) }
    var selectedDaySessions by remember { mutableStateOf<List<DetailedSession>?>(null) }

    val todayDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val monthYearFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "History",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
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
                    Text(text = "Today : ", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    Text(text = todayDate, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Black)
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
                        Text(text = monthYearFormat.format(currentMonth.time), style = MaterialTheme.typography.labelSmall, color = primaryColor)
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
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
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black
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
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }

            // Calendar Grid
            CalendarGrid(
                currentMonth = currentMonth,
                primaryColor = primaryColor,
                borderColor = borderColor,
                preferenceManager = preferenceManager,
                onDayClick = { sessions -> selectedDaySessions = sessions }
            )
        }
    }

    // Detail Dialog
    selectedDaySessions?.let { sessions ->
        SessionDetailDialog(
            sessions = sessions,
            onDismiss = { selectedDaySessions = null }
        )
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    primaryColor: Color,
    borderColor: Color,
    preferenceManager: PreferenceManager,
    onDayClick: (List<DetailedSession>) -> Unit
) {
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalGridItems = startDayOfWeek + daysInMonth
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)

    // Observe changes to detailedSessions
    val sessions = preferenceManager.detailedSessions

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(totalGridItems) { index ->
            if (index < startDayOfWeek) {
                Spacer(modifier = Modifier.aspectRatio(0.8f))
            } else {
                val day = index - startDayOfWeek + 1
                val dateCal = currentMonth.clone() as Calendar
                dateCal.set(Calendar.DAY_OF_MONTH, day)
                
                // Find sessions for this specific day
                val daySessions = sessions.filter { session ->
                    val sessionCal = Calendar.getInstance().apply { timeInMillis = session.startTime }
                    sessionCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                    sessionCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
                }

                val hasHistory = daySessions.isNotEmpty()
                val totalMs = daySessions.sumOf { it.durationMs }
                
                val (line1, line2) = when {
                    totalMs >= 31536000000L -> { // 365 days
                        val years = totalMs / 31536000000L
                        val months = (totalMs % 31536000000L) / 2592000000L
                        "${years}y" to "${months}M"
                    }
                    totalMs >= 2592000000L -> { // 30 days
                        val months = totalMs / 2592000000L
                        val weeks = (totalMs % 2592000000L) / 604800000L
                        "${months}M" to "${weeks}w"
                    }
                    totalMs >= 604800000L -> { // 7 days
                        val weeks = totalMs / 604800000L
                        val days = (totalMs % 604800000L) / 86400000L
                        "${weeks}w" to "${days}d"
                    }
                    totalMs >= 86400000L -> { // 1 day
                        val days = totalMs / 86400000L
                        val hours = (totalMs % 86400000L) / 3600000L
                        "${days}d" to "${hours}h"
                    }
                    totalMs >= 3600000L -> { // 1 hour
                        val hours = totalMs / 3600000L
                        val minutes = (totalMs % 3600000L) / 60000L
                        "${hours}h" to "${minutes}m"
                    }
                    else -> { // minutes and seconds
                        val minutes = totalMs / 60000L
                        val seconds = (totalMs % 60000L) / 1000L
                        "${minutes}m" to "${seconds}s"
                    }
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(0.8f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (hasHistory) Color.White else lightPurpleColor)
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(2.dp))
                        .clickable(enabled = hasHistory) { onDayClick(daySessions) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(16.dp).clip(CircleShape).background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = day.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hasHistory) {
                            Text(text = line1, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), fontWeight = FontWeight.Medium)
                            Text(text = line2, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Medium)
                        } else {
                            Icon(painter = painterResource(id = R.drawable.app_icon), contentDescription = null, tint = primaryColor.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionDetailDialog(sessions: List<DetailedSession>, onDismiss: () -> Unit) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val primaryColor = colorResource(id = R.color.primaryColor)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Usage Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = dateFormat.format(Date(sessions[0].startTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(sessions) { session ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = session.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${timeFormat.format(Date(session.startTime))} - ${timeFormat.format(Date(session.endTime))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            
                            val h = session.durationMs / 3600000
                            val m = (session.durationMs % 3600000) / 60000
                            val s = (session.durationMs % 60000) / 1000
                            
                            Text(
                                text = if (h > 0) "${h}h ${m}m" else "${m}m ${s}s",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
