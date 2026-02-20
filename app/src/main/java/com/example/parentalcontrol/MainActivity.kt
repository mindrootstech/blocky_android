package com.example.parentalcontrol

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.parentalcontrol.services.ParentalControlService
import com.example.parentalcontrol.ui.components.CreateModeBottomSheet
import com.example.parentalcontrol.ui.components.ReadyToScanBottomSheet
import com.example.parentalcontrol.ui.screens.*
import com.example.parentalcontrol.ui.theme.ParentalcontrolTheme
import com.example.parentalcontrol.utils.PreferenceManager
import com.example.parentalcontrol.utils.Mode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var nfcAdapter: NfcAdapter? = null

    private var shouldShowLockScreen by mutableStateOf(false)
    private var isReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            delay(2000)
            isReady = true
        }

        if (preferenceManager.unlockExpiration == 0L) {
            preferenceManager.isLocked = true
        }

        handleIntent(intent)

        enableEdgeToEdge()

        setContent {
            ParentalcontrolTheme {
                AppNavigator()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)

        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
        ) {

            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            val tagId = tag?.id?.joinToString("") { "%02x".format(it) }
            if (tagId != null) handleNfcScanned(tagId)
        }
    }

    private fun handleIntent(intent: Intent?) {
        val isLockedExtra = intent?.getBooleanExtra("EXTRA_LOCKED", false) ?: false
        shouldShowLockScreen = isLockedExtra
    }


    override fun onResume() {
        super.onResume()
        handleIntent(intent)

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_MUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun startForegroundService() {
        val intent = Intent(this, ParentalControlService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, ParentalControlService::class.java)
        stopService(intent)
    }

    private fun handleNfcScanned(tagId: String) {
        Log.d("ParentalControl", "NFC Tag detected: $tagId")
        performUnlock()
    }

    private fun performUnlock() {
        val duration = 5 * 60 * 1000L
        preferenceManager.unlockExpiration = System.currentTimeMillis() + duration
        shouldShowLockScreen = false
        intent.removeExtra("EXTRA_LOCKED")
    }

    @Composable
    fun AppNavigator() {
        var currentScreen by remember {
            mutableStateOf(
                if (preferenceManager.isFirstLaunch) Screen.Onboarding else Screen.Main
            )
        }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(500),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(500),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
            }, label = "AppNavTransition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.Onboarding -> OnboardingScreen(onFinished = {
                    preferenceManager.isFirstLaunch = false
                    currentScreen = Screen.Main
                })

                Screen.Main -> MainScreen(shouldShowLockScreen)
            }
        }
    }

    @Composable
    fun MainScreen(isBlocked: Boolean) {
        var currentTab by remember { mutableIntStateOf(0) }
        var showScanSheet by remember { mutableStateOf(false) }
        
        // Navigation states for full-screen screens
        var isAppListOpen by remember { mutableStateOf(false) }
        var isSchedulesOpen by remember { mutableStateOf(false) }
        var isModeListOpen by remember { mutableStateOf(false) }
        var isHistoryOpen by remember { mutableStateOf(false) }

        // Persisted state for Mode Creation flow
        var showCreateModeSheet by remember { mutableStateOf(false) }
        var pendingModeName by remember { mutableStateOf("") }
        var pendingSelectedApps by remember { mutableStateOf(setOf<String>()) }
        var editingModeName by remember { mutableStateOf<String?>(null) }
        
        val isCurrentlyUnlocked = preferenceManager.isCurrentlyUnlocked()
        val context = LocalContext.current

        var hasTappedContinue by remember { mutableStateOf(preferenceManager.isPermissionOnboarded) }

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Update internal permission status if needed, but don't force a screen jump here
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // --- NAVIGATION LOGIC ---
        
        // Priority 1: Blocking takes absolute priority (only when triggered by AppBlockerService)
        if (isBlocked && !isCurrentlyUnlocked) {
            LockScreenUI()
            BackHandler(enabled = true) { /* Block back button when on lock screen */ }
        }
        // Priority 2: Permission screen MUST be explicitly completed before the dashboard is accessible
        else if (!hasTappedContinue) {
            PermissionScreen(
                preferenceManager = preferenceManager,
                onContinue = {
                    // This is the ONLY place where we advance from the Permission screen
                    if (areAllPermissionsGranted(context)) {
                        preferenceManager.isPermissionOnboarded = true
                        hasTappedContinue = true
                    } else {
                        Toast.makeText(context, "Please grant all permissions first.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        // Priority 3: Normal App Screens
        else if (isAppListOpen) {
            YourDistractionsScreen(
                initialSelectedApps = pendingSelectedApps,
                onBack = { 
                    isAppListOpen = false
                    showCreateModeSheet = true 
                },
                onDone = { selected ->
                    pendingSelectedApps = selected
                    isAppListOpen = false
                    showCreateModeSheet = true 
                }
            )
            BackHandler { 
                isAppListOpen = false 
                showCreateModeSheet = true
            }
        } else if (isSchedulesOpen) {
            SchedulesScreen(preferenceManager = preferenceManager, onBack = { isSchedulesOpen = false })
            BackHandler { isSchedulesOpen = false }
        } else if (isModeListOpen) {
            ModeListScreen(
                preferenceManager = preferenceManager,
                onBack = { isModeListOpen = false },
                showCreateSheet = showCreateModeSheet,
                onShowCreateSheetChange = { showCreateModeSheet = it },
                pendingModeName = pendingModeName,
                onPendingModeNameChange = { pendingModeName = it },
                pendingSelectedApps = pendingSelectedApps,
                onPendingSelectedAppsChange = { pendingSelectedApps = it },
                editingModeName = editingModeName,
                onEditingModeNameChange = { editingModeName = it },
                onSelectAppClick = { name, apps ->
                    pendingModeName = name
                    pendingSelectedApps = apps
                    showCreateModeSheet = false 
                    isAppListOpen = true
                }
            )
            BackHandler { isModeListOpen = false }
        } else if (isHistoryOpen) {
            HistoryScreen(preferenceManager = preferenceManager, onBack = { isHistoryOpen = false })
            BackHandler { isHistoryOpen = false }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
                bottomBar = {
                    CurvedBottomNavigation(
                        selectedTab = if (currentTab == 7) 7 else if (currentTab == 0) 0 else -1,
                        onTabSelected = { currentTab = it },
                        onFabClick = { showScanSheet = true }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                                0 -> {
                                    HomeScreenContent(
                                        preferenceManager = preferenceManager,
                                        showCreateSheet = showCreateModeSheet,
                                        pendingModeName = pendingModeName,
                                        pendingSelectedApps = pendingSelectedApps,
                                        onShowCreateSheetChange = { showCreateModeSheet = it },
                                        onPendingModeNameChange = { pendingModeName = it },
                                        onPendingSelectedAppsChange = { pendingSelectedApps = it },
                                        onSelectAppClick = { name, apps ->
                                            pendingModeName = name
                                            pendingSelectedApps = apps
                                            showCreateModeSheet = false 
                                            isAppListOpen = true
                                        }
                                    )
                                }
                                3 -> {
                                    isHistoryOpen = true
                                }
                                7 -> SettingScreen(onNavigate = {
                                    when (it) {
                                        2 -> isSchedulesOpen = true
                                        3 -> isHistoryOpen = true
                                        8 -> isModeListOpen = true
                                        else -> currentTab = it
                                    }
                                })
                            }
                        }
                    }
                }

                if (showScanSheet) {
                    ReadyToScanBottomSheet(onDismiss = { showScanSheet = false })
                }
            }
        }
    }

    @Composable
    fun HomeScreenContent(
        preferenceManager: PreferenceManager,
        showCreateSheet: Boolean,
        pendingModeName: String,
        pendingSelectedApps: Set<String>,
        onShowCreateSheetChange: (Boolean) -> Unit,
        onPendingModeNameChange: (String) -> Unit,
        onPendingSelectedAppsChange: (Set<String>) -> Unit,
        onSelectAppClick: (String, Set<String>) -> Unit
    ) {
        var modes by remember { mutableStateOf(preferenceManager.modes) }
        var isManualRunning by remember { mutableStateOf(preferenceManager.isServiceRunning) }
        var currentProgress by remember { mutableFloatStateOf(0f) }
        var elapsedSeconds by remember { mutableLongStateOf(0L) }
        val context = LocalContext.current

        // Use a state-backed derived value for total protection status
        var activeSchedule by remember { mutableStateOf(preferenceManager.getActiveSchedule()) }
        val isProtectionActive = isManualRunning || activeSchedule != null

        // Sync background service with combined protection state
        LaunchedEffect(isProtectionActive) {
            if (isProtectionActive) startForegroundService()
            else stopForegroundService()
        }

        // Central timer loop for both manual and scheduled modes
        LaunchedEffect(Unit) {
            while (true) {
                // Update schedule state every second
                activeSchedule = preferenceManager.getActiveSchedule()
                val currentSchedule = activeSchedule
                
                if (currentSchedule != null) {
                    // --- INCREASING Scheduled Progress ---
                    val now = Calendar.getInstance()
                    val start = currentSchedule.startTime
                    val end = currentSchedule.endTime
                    
                    val nowSecs = (now.get(Calendar.HOUR_OF_DAY) * 3600) + (now.get(Calendar.MINUTE) * 60) + now.get(Calendar.SECOND)
                    var startSecs = (start.get(Calendar.HOUR_OF_DAY) * 3600) + (start.get(Calendar.MINUTE) * 60)
                    var endSecs = (end.get(Calendar.HOUR_OF_DAY) * 3600) + (end.get(Calendar.MINUTE) * 60)
                    
                    if (endSecs <= startSecs) endSecs += 86400 // Handle midnight cross
                    
                    val totalDuration = endSecs - startSecs
                    var elapsed = nowSecs - startSecs
                    if (elapsed < 0) elapsed += 86400 // Handled for early check
                    
                    if (totalDuration > 0) {
                        currentProgress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                        elapsedSeconds = elapsed.toLong()
                    } else {
                        // NO END TIME (Start and End same): Use 1-hour increasing session loop
                        val hourElapsed = (System.currentTimeMillis() / 1000) % 3600
                        currentProgress = hourElapsed.toFloat() / 3600f
                        elapsedSeconds = hourElapsed
                    }
                } else if (isManualRunning) {
                    // --- Manual Mode Increasing Progress (1-hour cyclic sessions) ---
                    val startTime = preferenceManager.lastServiceStartTime
                    val totalElapsedSecs = (System.currentTimeMillis() - startTime) / 1000
                    val hourElapsed = totalElapsedSecs % 3600
                    
                    currentProgress = hourElapsed.toFloat() / 3600f
                    elapsedSeconds = hourElapsed
                } else {
                    currentProgress = 0f
                    elapsedSeconds = 0L
                }
                delay(1000)
            }
        }

        HomeContent(
            isRunning = isProtectionActive,
            progress = currentProgress,
            elapsedSeconds = elapsedSeconds,
            modes = modes,
            onToggle = {
                if (isProtectionActive) {
                    // STOP LOGIC: Turn off manual protection AND any active schedule
                    preferenceManager.isServiceRunning = false
                    isManualRunning = false
                    
                    // Automatically disable the active schedule if one exists
                    activeSchedule?.let { schedule ->
                        val updatedSchedules = preferenceManager.schedules.map {
                            if (it.id == schedule.id) it.copy(isEnabled = false) else it
                        }
                        preferenceManager.schedules = updatedSchedules
                    }
                    
                    // Also disable all manual modes
                    val updatedModes = modes.map { it.copy(isEnabled = false) }
                    preferenceManager.modes = updatedModes
                    modes = updatedModes
                    
                    Toast.makeText(context, "Protection Stopped", Toast.LENGTH_SHORT).show()
                } else {
                    // START LOGIC: Manual Start
                    if (modes.none { it.isEnabled }) {
                        Toast.makeText(context, "Please select a mode first to start", Toast.LENGTH_SHORT).show()
                    } else {
                        val newState = true
                        preferenceManager.isServiceRunning = newState
                        isManualRunning = newState
                        preferenceManager.lastServiceStartTime = System.currentTimeMillis()
                    }
                }
            },
            onCreateModeClick = {
                onPendingModeNameChange("")
                onPendingSelectedAppsChange(emptySet())
                onShowCreateSheetChange(true)
            },
            onModeToggle = { mode, enabled ->
                val updatedModes = modes.map { 
                    it.copy(isEnabled = if (it.name == mode.name) enabled else false) 
                }
                preferenceManager.modes = updatedModes
                modes = updatedModes
            }
        )

        if (showCreateSheet) {
            CreateModeBottomSheet(
                initialName = pendingModeName,
                selectedPackageNames = pendingSelectedApps,
                existingModes = modes,
                onDismiss = { 
                    onShowCreateSheetChange(false)
                    onPendingModeNameChange("")
                    onPendingSelectedAppsChange(emptySet())
                },
                onSelectApp = { name -> onSelectAppClick(name, pendingSelectedApps) },
                onSave = { name ->
                    val newMode = Mode(name, pendingSelectedApps, false)
                    val updatedModes = modes + newMode
                    preferenceManager.modes = updatedModes
                    modes = updatedModes
                    onShowCreateSheetChange(false)
                    onPendingModeNameChange("")
                    onPendingSelectedAppsChange(emptySet())
                }
            )
        }
    }
}

class LiquidCurvedShape(
    private val barHeight: Dp,
    private val bulgeRadius: Dp,
    private val curveControl: Dp
) : Shape {
    override fun createOutline(
        shapeSize: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val bH = with(density) { barHeight.toPx() }
        val bR = with(density) { bulgeRadius.toPx() }
        val cC = with(density) { curveControl.toPx() }
        val corner = with(density) { 30.dp.toPx() }

        val path = Path().apply {
            moveTo(0f, shapeSize.height)
            lineTo(shapeSize.width, shapeSize.height)
            lineTo(shapeSize.width, shapeSize.height - bH + corner)
            quadraticTo(shapeSize.width, shapeSize.height - bH, shapeSize.width - corner, shapeSize.height - bH)

            lineTo(shapeSize.width / 2 + bR + cC, shapeSize.height - bH)
            cubicTo(
                shapeSize.width / 2 + bR, shapeSize.height - bH,
                shapeSize.width / 2 + bR, 0f,
                shapeSize.width / 2, 0f
            )
            cubicTo(
                shapeSize.width / 2 - bR, 0f,
                shapeSize.width / 2 - bR, shapeSize.height - bH,
                shapeSize.width / 2 - bR - cC, shapeSize.height - bH
            )

            lineTo(corner, shapeSize.height - bH)
            quadraticTo(0f, shapeSize.height - bH, 0f, shapeSize.height - bH + corner)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CurvedBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    val primaryColor = colorResource(id = R.color.primaryColor)
    val greyColor = colorResource(id = R.color.greyColor)

    val barHeightDp = 60.dp
    val bulgeRadiusDp = 40.dp
    val curveControlDp = 20.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(12.dp, LiquidCurvedShape(barHeightDp, bulgeRadiusDp, curveControlDp)),
            color = Color.White,
            shape = LiquidCurvedShape(barHeightDp, bulgeRadiusDp, curveControlDp)
        ) {}

        FloatingActionButton(
            onClick = onFabClick,
            containerColor = primaryColor,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "App Icon",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 35.dp),
                        onClick = { onTabSelected(0) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == 0) R.drawable.home_selected else R.drawable.home_unselected
                        ),
                        contentDescription = "Home",
                        tint = if (selectedTab == 0) primaryColor else greyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Home",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedTab == 0) primaryColor else greyColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(90.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 35.dp),
                        onClick = { onTabSelected(7) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == 7) R.drawable.setting_selected else R.drawable.setting_unselected
                        ),
                        contentDescription = "Setting",
                        tint = if (selectedTab == 7) primaryColor else greyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Setting",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedTab == 7) primaryColor else greyColor
                    )
                }
            }
        }
    }
}

enum class Screen {
    Onboarding,
    Main
}
