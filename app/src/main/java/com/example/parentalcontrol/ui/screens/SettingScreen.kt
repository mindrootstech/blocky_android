package com.example.parentalcontrol.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.parentalcontrol.R
import com.example.parentalcontrol.receivers.AdminReceiver
import com.example.parentalcontrol.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(preferenceManager: PreferenceManager, onNavigate: (Int) -> Unit) {
    val context = LocalContext.current
    var isStrictModeEnabled by remember { mutableStateOf(isAdminActive(context)) }
    
    // Light grey color for the border
    val borderColor = Color(0xFFF0F0F0)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val blackColor = colorResource(id = R.color.blackColor)

    // Sync state when returning to screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isStrictModeEnabled = isAdminActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                        color = blackColor,
                        fontWeight = FontWeight.Bold
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
                                fontWeight = FontWeight.Medium,
                                color = blackColor
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
                                fontWeight = FontWeight.Medium,
                                color = blackColor
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isStrictModeEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        requestAdminPermission(context)
                                    } else {
                                        removeAdminPermission(context)
                                        isStrictModeEnabled = false
                                    }
                                },
                                modifier = Modifier.scale(0.8f),
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

            // Mode list
            item {
                SettingsNavigationCard("Mode list", borderColor) { onNavigate(8) }
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
    val blackColor = colorResource(id = R.color.blackColor)
    
    SettingsCard(borderColor) {
        ListItem(
            headlineContent = {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = blackColor
                )
            },
            trailingContent = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = blackColor.copy(alpha = 0.4f)
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onClick() }
        )
    }
}

fun isAdminActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    return dpm.isAdminActive(adminComponent)
}

fun requestAdminPermission(context: Context) {
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required to protect the app from being uninstalled."
        )
    }
    context.startActivity(intent)
}

fun removeAdminPermission(context: Context) {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    dpm.removeActiveAdmin(adminComponent)
}
