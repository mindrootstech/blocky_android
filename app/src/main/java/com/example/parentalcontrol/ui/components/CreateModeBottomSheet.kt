package com.example.parentalcontrol.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.Mode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModeBottomSheet(
    initialName: String = "",
    selectedPackageNames: Set<String> = emptySet(),
    isEditing: Boolean = false,
    existingModes: List<Mode> = emptyList(),
    onDismiss: () -> Unit,
    onSelectApp: (String) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.sheet_bg_grey),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        CreateModeBottomSheetContent(
            initialName = initialName,
            selectedPackageNames = selectedPackageNames,
            isEditing = isEditing,
            existingModes = existingModes,
            onDismiss = onDismiss,
            onSelectApp = onSelectApp,
            onSave = onSave,
            onDelete = onDelete
        )
    }
}

@Composable
fun CreateModeBottomSheetContent(
    initialName: String = "",
    selectedPackageNames: Set<String> = emptySet(),
    isEditing: Boolean = false,
    existingModes: List<Mode> = emptyList(),
    onDismiss: () -> Unit,
    onSelectApp: (String) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bgColor = colorResource(id = R.color.sheet_bg_grey)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val iconBgColor = colorResource(id = R.color.icon_bg_grey)
    val hintColor = colorResource(id = R.color.hint_color)
    var modeName by remember { mutableStateOf(initialName) }

    val isSaveEnabled = modeName.isNotBlank() && selectedPackageNames.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp).padding(start = 4.dp),
                        tint = Color.Black
                    )
                }
            }

            Text(
                text = if (isEditing) "Edit Mode" else "New Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        // Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Mode Name Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode name :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    TextField(
                        value = modeName,
                        onValueChange = { modeName = it },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.End,
                            fontSize = 12.sp,
                            color = Color.Black
                        ),
                        placeholder = { 
                            Text(
                                "e.g work mode, Gym mode", 
                                color = hintColor, 
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            ) 
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Block App Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Block app :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        SelectedAppsIcons(context, selectedPackageNames)
                    }

                    Button(
                        modifier = Modifier.height(30.dp),
                        onClick = { onSelectApp(modeName) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            "Select app",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp, color = Color.White),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = { 
                if (isSaveEnabled) {
                    val nameExists = existingModes.any { 
                        it.name.equals(modeName.trim(), ignoreCase = true) && 
                        (!isEditing || it.name != initialName)
                    }
                    if (nameExists) {
                        Toast.makeText(context, "This name is already in use", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(modeName)
                    }
                } 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            enabled = isSaveEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                text = if (isEditing) "Update Mode" else "Save new Mode",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = Color.White),
            )
        }

        if (isEditing && onDelete != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Delete mode",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                modifier = Modifier.clickable { onDelete() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SelectedAppsIcons(
    context: Context, 
    packageNames: Set<String>,
    useBodySmall: Boolean = false
) {
    val pm = context.packageManager
    val icons = packageNames.take(4).mapNotNull { 
        try {
            pm.getApplicationIcon(it)
        } catch (e: Exception) {
            null
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        icons.forEachIndexed { index, drawable ->
            Box(
                modifier = Modifier
                    .offset(x = (index * (-8)).dp)
                    .size(if (useBodySmall) 20.dp else 24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    bitmap = drawable.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        if (packageNames.size > 4) {
            Text(
                text = "+${packageNames.size - 4} other",
                style = if (useBodySmall) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                color = colorResource(id = R.color.greyColor),
                modifier = Modifier.offset(x = (3 * (-8) + 4).dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateModeBottomSheetPreview() {
    CreateModeBottomSheetContent(
        onDismiss = {},
        onSelectApp = {},
        onSave = {}
    )
}
