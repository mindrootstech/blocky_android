package com.example.parentalcontrol.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import com.example.parentalcontrol.receivers.AdminReceiver

fun isAdminActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    val isActive = dpm.isAdminActive(adminComponent)
    Log.d("ParentalControl", "isAdminActive: $isActive")
    return isActive
}

fun requestAdminPermission(context: Context) {
    Log.d("ParentalControl", "requestAdminPermission called")
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required to protect the app from being uninstalled."
        )
    }
    
    val activity = context.findActivity()
    if (activity != null) {
        activity.startActivity(intent)
    } else {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    Log.d("ParentalControl", "requestAdminPermission intent started")
}

fun removeAdminPermission(context: Context) {
    Log.d("ParentalControl", "removeAdminPermission called")
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    dpm.removeActiveAdmin(adminComponent)
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
