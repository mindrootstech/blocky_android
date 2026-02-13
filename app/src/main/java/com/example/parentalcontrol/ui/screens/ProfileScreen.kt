package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.parentalcontrol.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigate: (Int) -> Unit) {
    val menuItems = listOf(
        MenuItem("App Management", Icons.Default.Apps, 1),
        MenuItem("Group Management", Icons.Default.Group, 2),
        MenuItem("Activity History", Icons.Default.History, 3),
        MenuItem("Usage Statistics", Icons.Default.BarChart, 4),
        MenuItem("Notification Logs", Icons.Default.Notifications, 5)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menu", style = MaterialTheme.typography.bodyLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.lightGreyColor)
                )
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(menuItems) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    leadingContent = { Icon(item.icon, contentDescription = null, tint = colorResource(id = R.color.primaryColor)) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigate(item.tabIndex) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector, val tabIndex: Int)
