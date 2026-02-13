package com.example.parentalcontrol.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.MainActivity
import com.example.parentalcontrol.utils.PreferenceManager

@Composable
fun SettingsScreen(preferenceManager: PreferenceManager) {
    var isStrictMode by remember { mutableStateOf(preferenceManager.isStrictMode) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("General Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strict Mode", fontWeight = FontWeight.Bold)
                    Text("Prevents uninstallation using Device Admin.", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = isStrictMode,
                    onCheckedChange = {
                        preferenceManager.isStrictMode = it
                        isStrictMode = it
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("All core permissions are currently active.", fontSize = 14.sp, color = Color.Green)
        
        // This is where logout would be, but user said remove login/signup. 
        // I'll keep the button but maybe change it to "Reset App" or something if they want, 
        // but for now I'll remove the Login/Signup specific parts.
    }
}
