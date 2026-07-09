package com.example.parentalcontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.parentalcontrol.services.ParentalControlService
import com.example.parentalcontrol.utils.PreferenceManager

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("ScheduleReceiver", "Schedule alarm received. Action: ${intent?.action}")
        
        val preferenceManager = PreferenceManager(context)
        val activeSchedule = preferenceManager.getActiveSchedule()
        
        Log.i("ScheduleReceiver", "Active schedule found: ${activeSchedule?.name ?: "None"}")
        
        val serviceIntent = Intent(context, ParentalControlService::class.java)
        
        if (activeSchedule != null || preferenceManager.isServiceRunning) {
            Log.i("ScheduleReceiver", "Starting ParentalControlService due to active schedule or manual run")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            Log.i("ScheduleReceiver", "Stopping ParentalControlService as no active schedule and manual run is off")
            context.stopService(serviceIntent)
        }
        
        // Always schedule the next alarm whenever a transition occurs
        preferenceManager.updateAlarms()
    }
}
