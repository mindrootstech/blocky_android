package com.example.parentalcontrol

import android.app.Application
import com.example.parentalcontrol.utils.TimerLifecycleObserver

class ParentalControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(TimerLifecycleObserver())
    }
}
