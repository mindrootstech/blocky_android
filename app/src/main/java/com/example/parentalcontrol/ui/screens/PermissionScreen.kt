package com.example.parentalcontrol.ui.screens

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.parentalcontrol.R
import com.example.parentalcontrol.receivers.AdminReceiver
import com.example.parentalcontrol.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(preferenceManager: PreferenceManager, onContinue: () -> Unit = {}) {
    val blackColor = colorResource(id = R.color.blackColor)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val context = LocalContext.current

    // IMPORTANT: Ensure protection is OFF while we are setting up permissions.
    // This prevents blocking from starting just because a permission was granted.
    LaunchedEffect(Unit) {
        preferenceManager.isServiceRunning = false
    }

    // Track each permission as state to ensure UI updates when they change
    var accessibilityGranted by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var adminGranted by remember { mutableStateOf(isAdminActive(context)) }
    var usageGranted by remember { mutableStateOf(isUsageStatsPermissionGranted(context)) }
    var notificationGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

    val allGranted = accessibilityGranted && overlayGranted && adminGranted && usageGranted && notificationGranted

    // Use LifecycleObserver to refresh permissions when returning to the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityGranted = isAccessibilityEnabled(context)
                overlayGranted = Settings.canDrawOverlays(context)
                adminGranted = isAdminActive(context)
                usageGranted = isUsageStatsPermissionGranted(context)
                notificationGranted = isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Permissions Required",
                        style = MaterialTheme.typography.bodyLarge,
                        color = blackColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.lightGreyColor)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = colorResource(id = R.color.lightGreyColor),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = { 
                        if (allGranted) {
                            onContinue()
                        }
                    },
                    enabled = allGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        disabledContainerColor = primaryColor.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        },
        containerColor = colorResource(id = R.color.lightGreyColor)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            PermissionCard(
                title = "Accessibility Service",
                description = "Required to detect and block restricted apps in real-time.",
                isGranted = accessibilityGranted,
                onClick = { openAccessibilitySettings(context) }
            )
            PermissionCard(
                title = "Overlay Permission",
                description = "Allows the app to show the lock screen over other apps.",
                isGranted = overlayGranted,
                onClick = { requestOverlayPermission(context) }
            )
            PermissionCard(
                title = "Device Admin",
                description = "Prevents the app from being uninstalled without permission.",
                isGranted = adminGranted,
                onClick = { requestAdminPermission(context) }
            )
            PermissionCard(
                title = "Usage Access",
                description = "Used to track app usage time and statistics.",
                isGranted = usageGranted,
                onClick = { requestUsageStatsPermission(context) }
            )
            PermissionCard(
                title = "Notification Access",
                description = "Allows the app to restrict notifications from blocked apps.",
                isGranted = notificationGranted,
                onClick = { openNotificationAccessSettings(context) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionCard(title: String, description: String, isGranted: Boolean, onClick: () -> Unit) {
    val blackColor = colorResource(id = R.color.blackColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val whiteColor = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (!isGranted) onClick() },
        colors = CardDefaults.cardColors(containerColor = whiteColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = blackColor
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGranted) primaryColor else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isGranted) primaryColor else greyColor,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = greyColor
            )
        }
    }
}

private fun safeStartActivity(context: Context, intent: Intent) {
    try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Could not open settings. Please open them manually.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun isAccessibilityEnabled(context: Context): Boolean {
    val expectedServiceName =
        "${context.packageName}/com.example.parentalcontrol.services.AppBlockerService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(expectedServiceName) == true
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(context.packageName) == true
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

fun requestOverlayPermission(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    safeStartActivity(context, intent)
}

fun isUsageStatsPermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun areAllPermissionsGranted(context: Context): Boolean {
    return isAccessibilityEnabled(context) &&
            Settings.canDrawOverlays(context) &&
            isAdminActive(context) &&
            isNotificationServiceEnabled(context) &&
            isUsageStatsPermissionGranted(context)
}

fun requestUsageStatsPermission(context: Context) =
    safeStartActivity(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

fun openAccessibilitySettings(context: Context) =
    safeStartActivity(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

fun openNotificationAccessSettings(context: Context) =
    safeStartActivity(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
