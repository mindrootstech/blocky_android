package com.example.parentalcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.ui.components.CreateModeBottomSheet
import com.example.parentalcontrol.ui.components.SelectedAppsIcons
import com.example.parentalcontrol.utils.Mode
import com.example.parentalcontrol.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeListScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    showCreateSheet: Boolean,
    onShowCreateSheetChange: (Boolean) -> Unit,
    pendingModeName: String,
    onPendingModeNameChange: (String) -> Unit,
    pendingSelectedApps: Set<String>,
    onPendingSelectedAppsChange: (Set<String>) -> Unit,
    editingModeName: String?,
    onEditingModeNameChange: (String?) -> Unit,
    onSelectAppClick: (String, Set<String>) -> Unit
) {
    var modes by remember { mutableStateOf(preferenceManager.modes) }
    val primaryColor = colorResource(id = R.color.primaryColor)

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mode List",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.neumorphic_bg)
                )
            )
        },
        bottomBar = {
            if (modes.isNotEmpty()) {
                Surface(
                    color = colorResource(id = R.color.neumorphic_bg),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { 
                            onEditingModeNameChange(null)
                            onPendingModeNameChange("")
                            onPendingSelectedAppsChange(emptySet())
                            onShowCreateSheetChange(true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = colorResource(id = R.color.neumorphic_bg)
    ) { padding ->
        if (modes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mode_empty),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "No Modes Yet",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                
                Spacer(modifier = Modifier.height(5.dp))
                
                Text(
                    text = "Create a custom profile to stay focused.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = R.color.greyColor),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = { 
                        onEditingModeNameChange(null)
                        onPendingModeNameChange("")
                        onPendingSelectedAppsChange(emptySet())
                        onShowCreateSheetChange(true)
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create new mode",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modes) { mode ->
                    ModeListItem(
                        mode = mode,
                        onEditClick = {
                            onEditingModeNameChange(it.name)
                            onPendingModeNameChange(it.name)
                            onPendingSelectedAppsChange(it.packageNames)
                            onShowCreateSheetChange(true)
                        }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateModeBottomSheet(
            initialName = pendingModeName,
            selectedPackageNames = pendingSelectedApps,
            isEditing = editingModeName != null,
            existingModes = modes,
            onDismiss = { 
                onShowCreateSheetChange(false)
                onEditingModeNameChange(null)
            },
            onSelectApp = { name -> onSelectAppClick(name, pendingSelectedApps) },
            onSave = { name ->
                val newMode = Mode(name, pendingSelectedApps, false)
                val updatedModes = if (editingModeName != null) {
                    modes.map { if (it.name == editingModeName) newMode else it }
                } else {
                    modes + newMode
                }
                preferenceManager.modes = updatedModes
                modes = updatedModes
                onShowCreateSheetChange(false)
                onPendingModeNameChange("")
                onPendingSelectedAppsChange(emptySet())
                onEditingModeNameChange(null)
            },
            onDelete = {
                if (editingModeName != null) {
                    val updatedModes = modes.filter { it.name != editingModeName }
                    preferenceManager.modes = updatedModes
                    modes = updatedModes
                    onShowCreateSheetChange(false)
                    onPendingModeNameChange("")
                    onPendingSelectedAppsChange(emptySet())
                    onEditingModeNameChange(null)
                }
            }
        )
    }
}

@Composable
fun ModeListItem(mode: Mode, onEditClick: (Mode) -> Unit) {
    val context = LocalContext.current
    val whiteColor = Color.White
    val borderColor = colorResource(id = R.color.borderColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = whiteColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // TOP ROW: Name and Edit Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    painter = painterResource(id = R.drawable.edit_mode),
                    contentDescription = "Edit Mode",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onEditClick(mode) }
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ROW: Icons (Moved where time was)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectedAppsIcons(context, mode.packageNames, useBodySmall = true)
            }
        }
    }
}
