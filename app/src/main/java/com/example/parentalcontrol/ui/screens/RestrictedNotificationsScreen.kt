package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RestrictedNotificationsScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf(preferenceManager.capturedNotifications.reversed()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val pm = context.packageManager

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Restricted Notifications", modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {
                preferenceManager.capturedNotifications = emptyList()
                notifications = emptyList()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Notifs", tint = Color.Gray)
            }
            IconButton(onClick = {
                notifications = preferenceManager.capturedNotifications.reversed()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No captured notifications.", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(notifications) { notif ->
                    val appLabel = remember(notif.packageName) {
                        try {
                            pm.getApplicationLabel(pm.getApplicationInfo(notif.packageName, 0)).toString()
                        } catch (e: Exception) {
                            notif.packageName
                        }
                    }
                    val appIcon = remember(notif.packageName) {
                        try {
                            pm.getApplicationIcon(notif.packageName)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    ListItem(
                        headlineContent = { Text(notif.title, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Column {
                                Text(notif.content)
                                Text(dateFormat.format(Date(notif.timestamp)), fontSize = 11.sp, color = Color.Gray)
                            }
                        },
                        leadingContent = {
                            appIcon?.let {
                                Image(
                                    bitmap = it.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            } ?: Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(32.dp))
                        },
                        overlineContent = { Text(appLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}
