package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.model.Schedule
import com.example.parentalcontrol.ui.components.CreateScheduleBottomSheet
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(preferenceManager: PreferenceManager, onBack: () -> Unit) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var schedules by remember { mutableStateOf(preferenceManager.schedules) }
    val modes = remember { preferenceManager.modes }
    val primaryColor = colorResource(id = R.color.primaryColor)

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Schedules",
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
                    containerColor = colorResource(id = R.color.neumorphic_bg)
                )
            )
        },
        bottomBar = {
            if (schedules.isNotEmpty()) {
                Surface(
                    color = colorResource(id = R.color.neumorphic_bg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { 
                            editingSchedule = null
                            showCreateSheet = true 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = colorResource(id = R.color.neumorphic_bg)
    ) { padding ->
        if (schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.schedules_empty),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Time to Focus",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                
                Spacer(modifier = Modifier.height(5.dp))
                
                Text(
                    text = "Schedule your digital downtime and let Blocky do the discipline for you.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = R.color.greyColor),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = { 
                        editingSchedule = null
                        showCreateSheet = true 
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Schedule",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedules) { schedule ->
                    ScheduleListItem(
                        schedule = schedule,
                        onToggle = { isEnabled ->
                            val updatedSchedules = schedules.map {
                                if (it.id == schedule.id) it.copy(isEnabled = isEnabled) else it
                            }
                            preferenceManager.schedules = updatedSchedules
                            schedules = updatedSchedules
                        },
                        onEditClick = {
                            editingSchedule = schedule
                            showCreateSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateScheduleBottomSheet(
            onDismiss = { 
                showCreateSheet = false
                editingSchedule = null
            },
            modes = modes,
            initialSchedule = editingSchedule,
            onSave = { name, start, end, mode, days ->
                val updatedSchedules = if (editingSchedule != null) {
                    schedules.map {
                        if (it.id == editingSchedule!!.id) {
                            it.copy(name = name, startTime = start, endTime = end, mode = mode, days = days)
                        } else it
                    }
                } else {
                    val newSchedule = Schedule(
                        name = name,
                        startTime = start,
                        endTime = end,
                        mode = mode,
                        days = days
                    )
                    schedules + newSchedule
                }
                preferenceManager.schedules = updatedSchedules
                schedules = updatedSchedules
                showCreateSheet = false
                editingSchedule = null
            },
            onDelete = {
                if (editingSchedule != null) {
                    val updatedSchedules = schedules.filter { it.id != editingSchedule!!.id }
                    preferenceManager.schedules = updatedSchedules
                    schedules = updatedSchedules
                    showCreateSheet = false
                    editingSchedule = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListItem(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    val primaryColor = colorResource(id = R.color.primaryColor)
    val borderColor = colorResource(id = R.color.borderColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    val displayDays = remember(schedule.days) {
        if (schedule.days.size == 1) {
            when (schedule.days[0]) {
                "SUNDAY" -> "Sunday"
                "MONDAY" -> "Monday"
                "TUESDAY" -> "Tuesday"
                "WEDNESDAY" -> "Wednesday"
                "THURSDAY" -> "Thursday"
                "FRIDAY" -> "Friday"
                "SATURDAY" -> "Saturday"
                else -> schedule.days[0]
            }
        } else {
            schedule.days.joinToString(", ") {
                it.take(3).lowercase().replaceFirstChar { char -> char.uppercase() }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Row 1: Name and Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = schedule.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )
                Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        Switch(
                            checked = schedule.isEnabled,
                            onCheckedChange = onToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray,
                                uncheckedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Time and Edit Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${timeFormatter.format(schedule.startTime.time)} - ${timeFormatter.format(schedule.endTime.time)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Icon(
                    painter = painterResource(id = R.drawable.edit_mode),
                    contentDescription = "Edit",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onEditClick
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Days
            Text(
                text = displayDays,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Row 4: Mode
            Text(
                text = "Mode : ${schedule.mode.name}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = greyColor
            )
        }
    }
}
