package com.example.parentalcontrol

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.example.parentalcontrol.services.ParentalControlService
import com.example.parentalcontrol.ui.screens.* 
import com.example.parentalcontrol.ui.theme.ParentalcontrolTheme
import com.example.parentalcontrol.utils.PreferenceManager
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
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {

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
        shouldShowLockScreen = if (isLockedExtra) {
            true
        } else {
            intent?.removeExtra("EXTRA_LOCKED")
            false
        }
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

    @OptIn(ExperimentalAnimationApi::class)
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
                ) with
                slideOutHorizontally(
                    animationSpec = tween(500),
                    targetOffsetX = { fullWidth -> -fullWidth }
                )
            }
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
        var currentTab by remember { mutableStateOf(0) }
        val isCurrentlyUnlocked = preferenceManager.isCurrentlyUnlocked()
        val context = LocalContext.current

        var allPermissionsGranted by remember { mutableStateOf(areAllPermissionsGranted(context)) }
        var hasTappedContinue by remember { mutableStateOf(false) }

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
                        hasTappedContinue = true 
                    }
                }
            )
        } else if (isBlocked && !isCurrentlyUnlocked) {
            LockScreenUI()
            BackHandler(enabled = true) { /* Block */ }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    CurvedBottomNavigation(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { currentTab = 6 }, // Center Profile/Menu Tab
                        containerColor = colorResource(id = R.color.primaryColor),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .offset(y = 42.dp)
                            .size(64.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                },
                floatingActionButtonPosition = FabPosition.Center
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (currentTab) {
                        0 -> {
                            HomeScreen(preferenceManager)
                            // Side effect to sync foreground service with preference state
                            LaunchedEffect(preferenceManager.isServiceRunning) {
                                if (preferenceManager.isServiceRunning) {
                                    startForegroundService()
                                } else {
                                    stopForegroundService()
                                }
                            }
                        }
                        1 -> AppListScreen(preferenceManager)
                        2 -> GroupManagementScreen(preferenceManager)
                        3 -> HistoryScreen(preferenceManager)
                        4 -> UsageScreen(preferenceManager)
                        5 -> RestrictedNotificationsScreen(preferenceManager)
                        6 -> ProfileScreen(onNavigate = { currentTab = it })
                        7 -> SettingsScreen(preferenceManager)
                    }
                }
            }
        }
    }

    @Composable
    fun CurvedBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
        val primaryColor = colorResource(id = R.color.primaryColor)
        val greyColor = colorResource(id = R.color.greyColor)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { onTabSelected(0) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = if (selectedTab == 0) primaryColor else greyColor
                        )
                        Text("Home", style = MaterialTheme.typography.labelSmall, color = if (selectedTab == 0) primaryColor else greyColor)
                    }
                }

                Spacer(modifier = Modifier.width(64.dp))

                IconButton(onClick = { onTabSelected(6) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (selectedTab == 6) primaryColor else greyColor
                        )
                        Text("Profile", style = MaterialTheme.typography.labelSmall, color = if (selectedTab == 6) primaryColor else greyColor)
                    }
                }
            }
        }
    }

    @Composable
    fun LockScreenUI() {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Red)
            Spacer(modifier = Modifier.height(24.dp))
            Text("APP RESTRICTED", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Please scan parent NFC card to continue.", fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }

}

enum class Screen {
    Onboarding,
    Main
}
