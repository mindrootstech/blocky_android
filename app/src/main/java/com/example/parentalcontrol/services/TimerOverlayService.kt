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
import java.util.Locale

class TimerOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var preferenceManager: PreferenceManager
    private var overlayView: View? = null
    private var timerTextView: TextView? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    
    private val timerRunnable = object : Runnable {
        override fun run() {
            // Update timer based on when the service was actually started in preferences
            val start = if (preferenceManager.lastServiceStartTime > 0) 
                preferenceManager.lastServiceStartTime 
            else startTime
            
            val millis = System.currentTimeMillis() - start
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60
            
            val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            timerTextView?.text = timeString
            
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferenceManager = PreferenceManager(this)
        startTime = System.currentTimeMillis()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        // IMPORTANT: Never show overlay if protection is disabled in settings
        if (!preferenceManager.isServiceRunning) {
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
            y = 120 // Slightly adjusted position
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
            } catch (e: Exception) {
                // View might already be gone
            }
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
