package com.example.parentalcontrol.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.utils.PreferenceManager

@Composable
fun AppListScreen(preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    val apps = remember { getInstalledApps(context) }
    var restrictedSet by remember { mutableStateOf(preferenceManager.restrictedApps) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Restricted Apps", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        LazyColumn {
            items(apps) { app ->
                val isRestricted = restrictedSet.contains(app.packageName)
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    app.icon?.let { Image(bitmap = it.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(app.label, modifier = Modifier.weight(1f))
                    Checkbox(checked = isRestricted, onCheckedChange = {
                        preferenceManager.toggleAppRestriction(app.packageName)
                        restrictedSet = preferenceManager.restrictedApps
                    })
                }
            }
        }
    }
}

private fun getInstalledApps(context: Context): List<AppInfoData> {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return apps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { AppInfoData(it.loadLabel(pm).toString(), it.packageName, it.loadIcon(pm)) }
        .sortedBy { it.label }
}

data class AppInfoData(val label: String, val packageName: String, val icon: Drawable?)
