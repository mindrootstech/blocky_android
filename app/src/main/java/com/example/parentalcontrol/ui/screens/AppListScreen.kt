package com.example.parentalcontrol.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    initialSelectedApps: Set<String>,
    onBack: () -> Unit,
    onDone: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    val apps = remember { getInstalledApps(context) }
    var selectedApps by remember { mutableStateOf(initialSelectedApps) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    val filteredApps = if (searchQuery.isEmpty()) {
        apps
    } else {
        apps.filter { it.label.contains(searchQuery, ignoreCase = true) } }

    val groupedApps = filteredApps.groupBy { it.category }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            Surface(
                color = colorResource(id = R.color.neumorphic_bg),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Your Distraction",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = {
                            Text(
                                "Search",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(id = R.color.white),
                            focusedContainerColor = colorResource(id = R.color.white),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Please select atleast one app:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.blackColor),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                color = colorResource(id = R.color.neumorphic_bg),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (selectedApps.isNotEmpty()) {
                        Text(
                            text = "${selectedApps.size}/50",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = { onDone(selectedApps) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp),
                        enabled = selectedApps.isNotEmpty(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primaryColor)
                        )
                    ) {
                        Text(
                            "Done",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = colorResource(id = R.color.neumorphic_bg)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            groupedApps.forEach { (category, categoryApps) ->
                item {
                    CategoryHeader(
                        category = category,
                        appCount = categoryApps.size,
                        isExpanded = expandedCategories.contains(category),
                        onExpandClick = {
                            expandedCategories = if (expandedCategories.contains(category)) {
                                expandedCategories - category
                            } else {
                                expandedCategories + category
                            }
                        }
                    )
                }

                if (expandedCategories.contains(category) || searchQuery.isNotEmpty()) {
                    items(categoryApps) { app ->
                        AppItem(
                            app = app,
                            isSelected = selectedApps.contains(app.packageName),
                            onCheckedChange = { isChecked ->
                                selectedApps = if (isChecked) {
                                    selectedApps + app.packageName
                                } else {
                                    selectedApps - app.packageName
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getCategoryIcon(category: String): Int {
    return when (category) {
        "Games" -> R.drawable.games
        "Audio" -> R.drawable.audio
        "Video" -> R.drawable.video
        "Social" -> R.drawable.social
        "Productivity" -> R.drawable.productivity
        else -> R.drawable.others
    }
}

@Composable
fun CategoryHeader(
    category: String,
    appCount: Int,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    val iconBgColor = colorResource(id = R.color.icon_bg_grey)
    val dividerColor = colorResource(id = R.color.divider_color)
    val greyColor = colorResource(id = R.color.greyColor)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onExpandClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getCategoryIcon(category)),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "($appCount)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = greyColor
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = dividerColor
        )
    }
}

@Composable
fun AppItem(
    app: AppInfoData,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!isSelected) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        app.icon?.let {
            Image(
                bitmap = it.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = app.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        RadioButton(
            selected = isSelected,
            onClick = { onCheckedChange(!isSelected) },
            colors = RadioButtonDefaults.colors(
                selectedColor = colorResource(id = R.color.primaryColor),
                unselectedColor = colorResource(id = R.color.greyColor).copy(alpha = 0.5f)
            ),
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun getInstalledApps(context: Context): List<AppInfoData> {
    val pm = context.packageManager
    val apps = try {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
    } catch (e: Exception) {
        emptyList<ApplicationInfo>()
    }
    
    return apps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map {
            val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (it.category) {
                    ApplicationInfo.CATEGORY_GAME -> "Games"
                    ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                    ApplicationInfo.CATEGORY_VIDEO -> "Video"
                    ApplicationInfo.CATEGORY_IMAGE -> "Images"
                    ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                    ApplicationInfo.CATEGORY_NEWS -> "News"
                    ApplicationInfo.CATEGORY_MAPS -> "Maps"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                    else -> "Others"
                }
            } else {
                "Others"
            }
            AppInfoData(it.loadLabel(pm).toString(), it.packageName, it.loadIcon(pm), category)
        }
        .sortedBy { it.label }
}

data class AppInfoData(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val category: String
)

@Preview(showBackground = true)
@Composable
fun AppListScreenPreview() {
    AppListScreen(
        initialSelectedApps = emptySet(),
        onBack = {},
        onDone = {}
    )
}
