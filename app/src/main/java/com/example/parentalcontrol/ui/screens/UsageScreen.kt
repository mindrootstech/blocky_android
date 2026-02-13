package com.example.parentalcontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.utils.DetailedSession
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun UsageScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    var sessions by remember { mutableStateOf(preferenceManager.detailedSessions) }
    val isServiceRunning = isAccessibilityEnabled(context)

    // Group sessions by date
    val groupedSessions = remember(sessions) {
        sessions.groupBy { session ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(session.startTime))
        }.toSortedMap(compareByDescending { it })
    }

    var currentSessionDuration by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isServiceRunning) {
        if (isServiceRunning && preferenceManager.lastServiceStartTime > 0) {
            while (true) {
                currentSessionDuration = System.currentTimeMillis() - preferenceManager.lastServiceStartTime
                kotlinx.coroutines.delay(1000)
            }
        } else {
            currentSessionDuration = 0L
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Usage History", modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {
                preferenceManager.detailedSessions = emptyList()
                sessions = emptyList()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Status Card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isServiceRunning) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(12.dp), shape = CircleShape, color = if (isServiceRunning) Color.Green else Color.Red) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isServiceRunning) "Service is ACTIVE" else "Service is INACTIVE", fontWeight = FontWeight.Bold)
                }
                if (isServiceRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Session: ${formatDuration(currentSessionDuration)}", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (groupedSessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No usage data recorded yet.", color = Color.Gray)
            }
        } else {
            LazyColumn {
                groupedSessions.forEach { (date, daySessions) ->
                    val totalDuration = daySessions.sumOf { it.durationMs }
                    item {
                        ExpandableUsageItem(date, totalDuration, daySessions)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableUsageItem(date: String, totalDuration: Long, sessions: List<DetailedSession>) {
    var expanded by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatDate(date), fontWeight = FontWeight.Bold)
                Text("Total: ${formatDuration(totalDuration)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                sessions.reversed().forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${timeFormat.format(Date(session.startTime))} - ${timeFormat.format(Date(session.endTime))}",
                            fontSize = 13.sp
                        )
                        Text(
                            formatDuration(session.durationMs),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) {
        "%02dh %02dm %02ds".format(hours, minutes, seconds)
    } else {
        "%02dm %02ds".format(minutes, seconds)
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        formatter.format(parser.parse(dateStr)!!)
    } catch (e: Exception) {
        dateStr
    }
}
