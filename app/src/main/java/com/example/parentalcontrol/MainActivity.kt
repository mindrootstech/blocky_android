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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        // Persisted state for Mode Creation flow
        var showCreateModeSheet by remember { mutableStateOf(false) }
        var pendingModeName by remember { mutableStateOf("") }
        var pendingSelectedApps by remember { mutableStateOf(setOf<String>()) }
        
        val isCurrentlyUnlocked = preferenceManager.isCurrentlyUnlocked()
        val context = LocalContext.current

        var allPermissionsGranted by remember { mutableStateOf(areAllPermissionsGranted(context)) }
        var hasTappedContinue by remember { mutableStateOf(preferenceManager.isPermissionOnboarded) }

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    allPermissionsGranted = areAllPermissionsGranted(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        if (!allPermissionsGranted || !hasTappedContinue) {
            PermissionScreen(
                preferenceManager = preferenceManager,
                onContinue = {
                    if (allPermissionsGranted) {
                        preferenceManager.isPermissionOnboarded = true
                        hasTappedContinue = true
                    }
                }
            )
        } else if (isBlocked && !isCurrentlyUnlocked) {
            LockScreenUI()
            BackHandler(enabled = true) { /* Block */ }
        } else if (isAppListOpen) {
            AppListScreen(
                initialSelectedApps = pendingSelectedApps,
                onBack = { 
                    isAppListOpen = false
                    showCreateModeSheet = true // Return to the sheet
                },
                onDone = { selected ->
                    pendingSelectedApps = selected
                    isAppListOpen = false
                    showCreateModeSheet = true // Return to the sheet with apps
                }
            )
            BackHandler { 
                isAppListOpen = false 
                showCreateModeSheet = true
            }
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
                                        onSelectAppClick = { name, apps ->
                                            pendingModeName = name
                                            pendingSelectedApps = apps
                                            showCreateModeSheet = false // Hide sheet before opening full screen
                                            isAppListOpen = true
                                        }
                                    )
                                    LaunchedEffect(preferenceManager.isServiceRunning) {
                                        if (preferenceManager.isServiceRunning) startForegroundService()
                                        else stopForegroundService()
                                    }
                                }
                                2 -> GroupManagementScreen(preferenceManager)
                                3 -> HistoryScreen(preferenceManager)
                                4 -> UsageScreen(preferenceManager)
                                5 -> RestrictedNotificationsScreen(preferenceManager)
                                7 -> ProfileScreen(onNavigate = { currentTab = it })
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
        onSelectAppClick: (String, Set<String>) -> Unit
    ) {
        var modes by remember { mutableStateOf(preferenceManager.modes) }
        var isRunning by remember { mutableStateOf(preferenceManager.isServiceRunning) }
        var currentProgress by remember { mutableFloatStateOf(0f) }
        val context = LocalContext.current

        LaunchedEffect(isRunning) {
            if (isRunning) {
                while (true) {
                    val startTime = preferenceManager.lastServiceStartTime
                    val elapsed = System.currentTimeMillis() - startTime
                    val oneHourMs = 3600000L
                    currentProgress = (elapsed % oneHourMs).toFloat() / oneHourMs.toFloat()
                    delay(1000)
                }
            } else {
                currentProgress = 0f
            }
        }

        HomeContent(
            isRunning = isRunning,
            progress = currentProgress,
            modes = modes,
            onToggle = {
                if (!isRunning && modes.none { it.isEnabled }) {
                    Toast.makeText(context, "Please select a mode first to start", Toast.LENGTH_SHORT).show()
                } else {
                    val newState = !isRunning
                    preferenceManager.isServiceRunning = newState
                    isRunning = newState
                    if (newState) {
                        preferenceManager.lastServiceStartTime = System.currentTimeMillis()
                    }
                }
            },
            onCreateModeClick = {
                onPendingModeNameChange("")
                // Note: apps are reset separately or kept depending on desired UX
                onShowCreateSheetChange(true)
            },
            onModeToggle = { mode, enabled ->
                // Only one mode can be enabled at a time based on the radio button behavior
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
                onDismiss = { onShowCreateSheetChange(false) },
                onSelectApp = { name -> onSelectAppClick(name, pendingSelectedApps) },
                onSave = { name ->
                    val newMode = Mode(name, pendingSelectedApps, false)
                    val updatedModes = modes + newMode
                    preferenceManager.modes = updatedModes
                    modes = updatedModes
                    onShowCreateSheetChange(false)
                    onPendingModeNameChange("")
                }
            )
        }
    }

    @Composable
    fun LockScreenUI() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "APP RESTRICTED",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Please scan parent NFC card to continue.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Custom Shape that follows the liquid curve exactly.
 */
class LiquidCurvedShape(
    private val barHeight: Dp,
    private val bulgeRadius: Dp,
    private val curveControl: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val bH = with(density) { barHeight.toPx() }
        val bR = with(density) { bulgeRadius.toPx() }
        val cC = with(density) { curveControl.toPx() }
        val corner = with(density) { 30.dp.toPx() }

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, size.height - bH + corner)
            quadraticBezierTo(size.width, size.height - bH, size.width - corner, size.height - bH)

            // Curve transition
            lineTo(size.width / 2 + bR + cC, size.height - bH)
            cubicTo(
                size.width / 2 + bR, size.height - bH,
                size.width / 2 + bR, 0f,
                size.width / 2, 0f
            )
            cubicTo(
                size.width / 2 - bR, 0f,
                size.width / 2 - bR, size.height - bH,
                size.width / 2 - bR - cC, size.height - bH
            )

            lineTo(corner, size.height - bH)
            quadraticBezierTo(0f, size.height - bH, 0f, size.height - bH + corner)
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

    val barHeight = 60.dp
    val bulgeRadius = 40.dp
    val curveControl = 20.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- LIQUID CURVE BACKGROUND (Truly Transparent Outside) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(12.dp, LiquidCurvedShape(barHeight, bulgeRadius, curveControl)),
            color = Color.White,
            shape = LiquidCurvedShape(barHeight, bulgeRadius, curveControl)
        ) {}

        // --- INTEGRATED CENTER BUTTON WITH APP ICON ---
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

        // --- NAVIGATION ITEMS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Left Tab: Home
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

            // Right Tab: Setting
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

@Preview(showBackground = true)
@Composable
fun PreviewCurvedBottomNavigation() {
    ParentalcontrolTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.BottomCenter
        ) {
            CurvedBottomNavigation(selectedTab = 0, onTabSelected = {}, onFabClick = {})
        }
    }
}

enum class Screen {
    Onboarding,
    Main
}
