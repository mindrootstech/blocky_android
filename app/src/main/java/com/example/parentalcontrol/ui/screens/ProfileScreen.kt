package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.parentalcontrol.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigate: (Int) -> Unit) {
    var isStrictModeEnabled by remember { mutableStateOf(false) }
    // Light grey color for the border
    val borderColor = Color(0xFFF0F0F0)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val greyColor = colorResource(id = R.color.greyColor)

    Scaffold(
        topBar = {
            Surface(
                color = colorResource(id = R.color.white)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Blocky Emergency
            item {
                SettingsCard(borderColor) {
                    ListItem(
                        headlineContent = {
                            Text(
                                "Blocky Emergency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.Black)) {
                                        append("5 ")
                                    }
                                    withStyle(style = SpanStyle(color = greyColor)) {
                                        append("Remaining")
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { /* Handle Emergency Click */ }
                    )
                }
            }

            // Strict Mode
            item {
                SettingsCard(borderColor) {
                    ListItem(
                        headlineContent = {
                            Text(
                                "Strict mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isStrictModeEnabled,
                                onCheckedChange = { isStrictModeEnabled = it },
                                modifier = Modifier.scale(0.8f), // Reduced the size of the switch
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = primaryColor,
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFE0E0E0),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Schedules
            item {
                SettingsNavigationCard("Schedules", borderColor) { onNavigate(2) }
            }

            // Model list
            item {
                SettingsNavigationCard("Model list", borderColor) { /* Navigate to Model list */ }
            }

            // History
            item {
                SettingsNavigationCard("History", borderColor) { onNavigate(3) }
            }

            // Privacy policy
            item {
                SettingsNavigationCard("Privacy policy", borderColor) { /* Navigate to Privacy Policy */ }
            }

            // About blocky
            item {
                SettingsNavigationCard("About blocky", borderColor) { /* Navigate to About */ }
            }
        }
    }
}

@Composable
fun SettingsCard(borderColor: Color, content: @Composable () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SettingsNavigationCard(title: String, borderColor: Color, onClick: () -> Unit) {
    SettingsCard(borderColor) {
        ListItem(
            headlineContent = {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingContent = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onClick() }
        )
    }
}
