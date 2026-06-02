package com.example.parentalcontrol

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parentalcontrol.ui.screens.LockScreenUI
import com.example.parentalcontrol.ui.theme.ParentalcontrolTheme
import com.example.parentalcontrol.utils.PreferenceManager

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure this activity shows over the lockscreen and wakes up the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        enableEdgeToEdge()
        
        val preferenceManager = PreferenceManager(this)

        setContent {
            ParentalcontrolTheme {
                val isCurrentlyUnlocked = preferenceManager.isCurrentlyUnlocked()
                
                if (!isCurrentlyUnlocked) {
                    LockScreenUI()
                    BackHandler(enabled = true) { /* Block back button */ }
                } else {
                    // If somehow opened while unlocked, just close
                    finish()
                }
            }
        }
    }
}
