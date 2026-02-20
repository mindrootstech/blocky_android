package com.example.parentalcontrol.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.PreferenceManager
import java.util.Calendar
import java.util.Locale

class TimerOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var preferenceManager: PreferenceManager
    private var overlayView: View? = null
    private var timerTextView: TextView? = null
    
    private val handler = Handler(Looper.getMainLooper())
    
    private val timerRunnable = object : Runnable {
        override fun run() {
            val activeSchedule = preferenceManager.getActiveSchedule()
            
            val startMs = when {
                activeSchedule != null -> {
                    // Use schedule's start time for accurate elapsed count
                    val start = activeSchedule.startTime
                    val now = Calendar.getInstance()
                    val startCal = now.clone() as Calendar
                    startCal.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY))
                    startCal.set(Calendar.MINUTE, start.get(Calendar.MINUTE))
                    startCal.set(Calendar.SECOND, 0)
                    if (startCal.after(now)) startCal.add(Calendar.DAY_OF_YEAR, -1)
                    startCal.timeInMillis
                }
                preferenceManager.isServiceRunning -> {
                    // Use manual start time
                    if (preferenceManager.lastServiceStartTime > 0) 
                        preferenceManager.lastServiceStartTime 
                    else System.currentTimeMillis()
                }
                else -> System.currentTimeMillis()
            }
            
            val elapsedMs = System.currentTimeMillis() - startMs
            val totalSeconds = (elapsedMs / 1000).coerceAtLeast(0)
            
            // Apply 1-hour session loop for manual/no-end-time modes
            val displayedSeconds = if (activeSchedule == null || 
                (activeSchedule.startTime.get(Calendar.HOUR_OF_DAY) == activeSchedule.endTime.get(Calendar.HOUR_OF_DAY) && 
                 activeSchedule.startTime.get(Calendar.MINUTE) == activeSchedule.endTime.get(Calendar.MINUTE))) {
                totalSeconds % 3600
            } else {
                totalSeconds
            }

            val hours = displayedSeconds / 3600
            val minutes = (displayedSeconds % 3600) / 60
            val seconds = displayedSeconds % 60
            
            val timeString = if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
            
            timerTextView?.text = timeString
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferenceManager = PreferenceManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        val isAnyProtectionActive = preferenceManager.isServiceRunning || preferenceManager.getActiveSchedule() != null

        if (!isAnyProtectionActive) {
            hideOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_SHOW) {
            showOverlay()
        } else if (action == ACTION_HIDE) {
            hideOverlay()
            stopSelf()
        }
        return START_STICKY
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) return
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_timer, null)
        timerTextView = overlayView?.findViewById(R.id.timer_text)

        try {
            windowManager.addView(overlayView, params)
            handler.post(timerRunnable)
        } catch (e: Exception) {
            e.printStackTrace()
            overlayView = null
        }
    }

    private fun hideOverlay() {
        handler.removeCallbacks(timerRunnable)
        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) { }
            overlayView = null
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_HIDE = "ACTION_HIDE"
    }
}
