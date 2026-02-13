package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    var history by remember { mutableStateOf(preferenceManager.blockHistory.reversed()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val pm = context.packageManager

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Block History", modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            IconButton(onClick = {
                preferenceManager.blockHistory = emptyList()
                history = emptyList()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.Gray)
            }
            IconButton(onClick = {
                history = preferenceManager.blockHistory.reversed()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No restriction history yet.", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(history) { event ->
                    val appLabel = remember(event.packageName) {
                        try {
                            pm.getApplicationLabel(pm.getApplicationInfo(event.packageName, 0)).toString()
                        } catch (e: Exception) {
                            event.packageName
                        }
                    }
                    val appIcon = remember(event.packageName) {
                        try {
                            pm.getApplicationIcon(event.packageName)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    ListItem(
                        headlineContent = { Text(appLabel) },
                        supportingContent = { Text(dateFormat.format(Date(event.timestamp))) },
                        leadingContent = {
                            appIcon?.let {
                                Image(
                                    bitmap = it.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            } ?: Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(32.dp))
                        },
                        trailingContent = {
                            Text("Blocked", color = Color.Red, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}
