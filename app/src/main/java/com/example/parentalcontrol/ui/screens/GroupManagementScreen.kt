package com.example.parentalcontrol.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.utils.AppGroup
import com.example.parentalcontrol.utils.PreferenceManager
import java.util.*

@Composable
fun GroupManagementScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    var groups by remember { mutableStateOf(preferenceManager.appGroups) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AppGroup?>(null) }
    val apps = remember { getInstalledApps(context) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingGroup = null
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Restriction Groups", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)

            LazyColumn {
                items(groups) { group ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(group.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = group.isEnabled,
                                    onCheckedChange = { isEnabled ->
                                        val updatedGroups = groups.map {
                                            if (it.name == group.name) it.copy(isEnabled = isEnabled) else it
                                        }
                                        groups = updatedGroups
                                        preferenceManager.appGroups = updatedGroups
                                    }
                                )
                                IconButton(onClick = {
                                    editingGroup = group
                                    showAddDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Group")
                                }
                                IconButton(onClick = {
                                    groups = groups.filter { it != group }
                                    preferenceManager.appGroups = groups
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Group")
                                }
                            }
                            Text("Restricted: ${group.startTime} - ${group.endTime}", fontSize = 14.sp)
                            Text("Apps: ${group.packageNames.size}", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf(editingGroup?.name ?: "") }
            var startTime by remember { mutableStateOf(editingGroup?.startTime ?: "09:00") }
            var endTime by remember { mutableStateOf(editingGroup?.endTime ?: "17:00") }
            val selectedApps = remember { mutableStateOf(editingGroup?.packageNames ?: setOf()) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (editingGroup == null) "Create Group" else "Edit Group") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Group Name") },
                            enabled = editingGroup == null
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            TimePickerButton("Start: $startTime") { time -> startTime = time }
                            Spacer(modifier = Modifier.width(8.dp))
                            TimePickerButton("End: $endTime") { time -> endTime = time }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Apps:", fontWeight = FontWeight.SemiBold)
                        Box(modifier = Modifier.height(200.dp)) {
                            LazyColumn {
                                items(apps) { app ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                        selectedApps.value = if (selectedApps.value.contains(app.packageName)) selectedApps.value - app.packageName else selectedApps.value + app.packageName
                                    }) {
                                        Checkbox(checked = selectedApps.value.contains(app.packageName), onCheckedChange = null)
                                        Text(app.label)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotEmpty()) {
                            val newGroup = AppGroup(name, selectedApps.value, startTime, endTime, editingGroup?.isEnabled ?: true)
                            if (editingGroup == null) {
                                groups = groups + newGroup
                            } else {
                                groups = groups.map { if (it.name == editingGroup!!.name) newGroup else it }
                            }
                            preferenceManager.appGroups = groups
                            showAddDialog = false
                            editingGroup = null
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        editingGroup = null
                    }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun TimePickerButton(label: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    Button(onClick = {
        val calendar = Calendar.getInstance()
        TimePickerDialog(context, { _, h, m ->
            onTimeSelected("%02d:%02d".format(h, m))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }) { Text(label) }
}

private fun getInstalledApps(context: android.content.Context): List<AppInfoData> {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
    return apps.filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { AppInfoData(it.loadLabel(pm).toString(), it.packageName, it.loadIcon(pm)) }
        .sortedBy { it.label }
}
