package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.PreferenceManager

@Composable
fun HomeScreen(preferenceManager: PreferenceManager) {
    val primaryColor = colorResource(id = R.color.primaryColor)
    val redColor = Color(0xFFE53935)
    
    // Use a state to track the running status for UI updates
    var isRunning by remember { mutableStateOf(preferenceManager.isServiceRunning) }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRunning) "Protection is ACTIVE" else "Protection is INACTIVE",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isRunning) primaryColor else Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isRunning) 
                    "Restricted apps will be blocked." 
                else 
                    "Tap below to start blocking restricted apps.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    val newState = !isRunning
                    preferenceManager.isServiceRunning = newState
                    isRunning = newState
                    
                    // Also track the session start time when protection starts
                    if (newState) {
                        preferenceManager.lastServiceStartTime = System.currentTimeMillis()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) redColor else primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isRunning) "Stop Protection" else "Start Protection",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
