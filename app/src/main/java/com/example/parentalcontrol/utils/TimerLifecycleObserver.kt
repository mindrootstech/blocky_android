package com.example.parentalcontrol.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.example.parentalcontrol.services.TimerOverlayService

class TimerLifecycleObserver : Application.ActivityLifecycleCallbacks {
    private var activityCount = 0

    override fun onActivityStarted(activity: Activity) {
        if (activityCount == 0) {
            // App coming to foreground, hide overlay
            val intent = Intent(activity, TimerOverlayService::class.java).apply {
                action = TimerOverlayService.ACTION_HIDE
            }
            activity.startService(intent)
        }
        activityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0) {
            // App going to background, show overlay
            val intent = Intent(activity, TimerOverlayService::class.java).apply {
                action = TimerOverlayService.ACTION_SHOW
            }
            activity.startService(intent)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
