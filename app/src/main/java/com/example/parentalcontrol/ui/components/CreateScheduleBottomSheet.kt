package com.example.parentalcontrol.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.model.Schedule
import com.example.parentalcontrol.utils.Mode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, Calendar, Calendar, Mode, List<String>) -> Unit,
    modes: List<Mode>,
    initialSchedule: Schedule? = null,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.sheet_bg_grey),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        CreateScheduleBottomSheetContent(
            onDismiss = onDismiss,
            onSave = onSave,
            modes = modes,
            initialSchedule = initialSchedule,
            onDelete = onDelete
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleBottomSheetContent(
    onDismiss: () -> Unit,
    onSave: (String, Calendar, Calendar, Mode, List<String>) -> Unit,
    modes: List<Mode>,
    initialSchedule: Schedule? = null,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bgColor = colorResource(id = R.color.sheet_bg_grey)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val iconBgColor = colorResource(id = R.color.icon_bg_grey)
    val hintColor = colorResource(id = R.color.hint_color)
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)

    var scheduleName by remember { mutableStateOf(initialSchedule?.name ?: "") }

    // Using nullable Calendars to track if time was selected
    var startTime by remember { 
        mutableStateOf<Calendar?>(initialSchedule?.let { 
            Calendar.getInstance().apply { timeInMillis = it.startTimeMs } 
        }) 
    }
    var endTime by remember { 
        mutableStateOf<Calendar?>(initialSchedule?.let { 
            Calendar.getInstance().apply { timeInMillis = it.endTimeMs } 
        }) 
    }

    var selectedMode by remember { mutableStateOf<Mode?>(initialSchedule?.mode) }
    val selectedDays = remember { 
        mutableStateListOf<String>().apply {
            initialSchedule?.days?.let { addAll(it) }
        }
    }

    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val valueTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        color = Color.Black,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.End
    )

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showStartTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                startTime = cal
                showStartTimePicker = false
            },
            startTime?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY),
            startTime?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE),
            false
        ).apply {
            setOnCancelListener { showStartTimePicker = false }
            show()
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                endTime = cal
                showEndTimePicker = false
            },
            endTime?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            endTime?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE),
            false
        ).apply {
            setOnCancelListener { showEndTimePicker = false }
            show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 4.dp),
                        tint = Color.Black
                    )
                }
            }

            Text(
                text = if (initialSchedule == null) "New Schedule" else "Edit Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        // Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                // Schedule Name Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule Name :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    TextField(
                        value = scheduleName,
                        onValueChange = { scheduleName = it },
                        textStyle = valueTextStyle,
                        placeholder = {
                            Text(
                                "e.g Morning Routine",
                                color = hintColor,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Start Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { showStartTimePicker = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start Time :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (startTime == null) "Select Time" else timeFormatter.format(
                                startTime!!.time
                            ),
                            style = if (startTime == null) MaterialTheme.typography.labelSmall.copy(
                                color = hintColor
                            ) else valueTextStyle
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // End Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { showEndTimePicker = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "End Time :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (endTime == null) "Select Time" else timeFormatter.format(
                                endTime!!.time
                            ),
                            style = if (endTime == null) MaterialTheme.typography.labelSmall.copy(
                                color = hintColor
                            ) else valueTextStyle
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Select Mode
                var expanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Mode :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedMode?.name ?: "Select a mode",
                                style = if (selectedMode == null) MaterialTheme.typography.labelSmall.copy(
                                    color = hintColor,
                                    textAlign = TextAlign.End
                                ) else valueTextStyle.copy(textAlign = TextAlign.End),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(x = 0.dp, y = 0.dp)
                        ) {
                            modes.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    onClick = {
                                        selectedMode = mode
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Repeat Days
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    Text(
                        text = "Repeat :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val dayIdentifiers = remember {
                        listOf(
                            "SUNDAY",
                            "MONDAY",
                            "TUESDAY",
                            "WEDNESDAY",
                            "THURSDAY",
                            "FRIDAY",
                            "SATURDAY"
                        )
                    }
                    val dayDisplays = remember { listOf("S", "M", "T", "W", "T", "F", "S") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayIdentifiers.forEachIndexed { index, dayIdentifier ->
                            val displayChar = dayDisplays[index]
                            val isSelected = selectedDays.contains(dayIdentifier)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) primaryColor else lightPurpleColor)
                                    .clickable {
                                        if (isSelected) selectedDays.remove(dayIdentifier) else selectedDays.add(
                                            dayIdentifier
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayChar,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                if (startTime != null && endTime != null && selectedMode != null) {
                    onSave(scheduleName, startTime!!, endTime!!, selectedMode!!, selectedDays)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            enabled = scheduleName.isNotBlank() && startTime != null && endTime != null && selectedMode != null && selectedDays.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                if (initialSchedule == null) "Save Schedule" else "Update Schedule",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = Color.White
                ),
            )
        }
        
        if (initialSchedule != null && onDelete != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text(
                    "Delete Schedule",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
