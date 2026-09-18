package com.oqba26.jafr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.oqba26.jafr.model.Screen
import com.oqba26.jafr.ui.*
import com.oqba26.jafr.ui.theme.JafrTheme
import com.oqba26.jafr.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager(this)
        
        setContent {
            val historyManager = remember { HistoryManager { settingsManager.getOrCreateDeviceId() } }
            val selectedFont by settingsManager.selectedFont.collectAsState(initial = "vazirmatn")
            val defaultTypeStr by settingsManager.defaultType.collectAsState(initial = "JAFR_15")
            val showKabir by settingsManager.showKabir.collectAsState(initial = false)
            val showSaghir by settingsManager.showSaghir.collectAsState(initial = false)
            val showWasait by settingsManager.showWasait.collectAsState(initial = false)

            val visibleTypes = remember(showKabir, showSaghir, showWasait) {
                buildList {
                    add(AbjadType.JAFR_15)
                    add(AbjadType.JAFR_NUMERICAL)
                    if (showKabir) add(AbjadType.KABIR)
                    if (showSaghir) add(AbjadType.SAGHIR)
                    if (showWasait) add(AbjadType.WASAIT)
                }
            }

            val defaultType = remember(defaultTypeStr, visibleTypes) {
                val parsed = try {
                    AbjadType.valueOf(defaultTypeStr)
                } catch (_: Exception) {
                    AbjadType.JAFR_15
                }
                if (parsed in visibleTypes) parsed else AbjadType.JAFR_15
            }

            val fontFamily = remember(selectedFont) { getFontFamily(selectedFont) }
            val customTypography = remember(fontFamily) { createTypography(fontFamily) }
            var currentScreen by remember { mutableStateOf(Screen.CALCULATOR) }
            var selectedType by remember { mutableStateOf(AbjadType.JAFR_15) }
            var calculatorText by remember { mutableStateOf("") }
            var showExitDialog by remember { mutableStateOf(value = false) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            var downloadProgress by remember { mutableFloatStateOf(0f) }
            var isDownloading by remember { mutableStateOf(value = false) }
            val scope = rememberCoroutineScope()
            val updateManager = remember { UpdateManager(this@MainActivity) }

            // Update selectedType when defaultType or visibleTypes change
            LaunchedEffect(defaultType, visibleTypes) {
                if (selectedType !in visibleTypes) {
                    selectedType = defaultType
                }
            }

            // Check for updates
            LaunchedEffect(Unit) {
                updateInfo = updateManager.checkForUpdate()
            }

            // Handle Back Press for Navigation and Exit
            BackHandler(enabled = true) {
                if ((currentScreen == Screen.CALCULATOR) && (selectedType == defaultType)) {
                    showExitDialog = true
                } else {
                    currentScreen = Screen.CALCULATOR
                    selectedType = defaultType
                }
            }

            JafrTheme(typography = customTypography) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (currentScreen) {
                                            Screen.CALCULATOR -> "میزان الحروف"
                                            Screen.HISTORY -> "تاریخچه محاسبات"
                                            Screen.SETTINGS -> "تنظیمات"
                                        },
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                navigationIcon = {
                                    if (currentScreen != Screen.CALCULATOR) {
                                        IconButton(onClick = { currentScreen = Screen.CALCULATOR }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                                        }
                                    }
                                },
                                actions = {
                                    if ((currentScreen == Screen.CALCULATOR) || (currentScreen == Screen.HISTORY)) {
                                        IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                                            Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                )
                            )
                        },
                        bottomBar = {
                            AppBottomBar(
                                currentScreen = currentScreen,
                                selectedType = selectedType,
                                visibleTypes = visibleTypes,
                                onScreenSelected = { currentScreen = it }
                            ) { selectedType = it }
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (currentScreen) {
                                Screen.CALCULATOR -> {
                                    AbjadCalculatorScreen(
                                        selectedType = selectedType,
                                        historyManager = historyManager,
                                        initialText = calculatorText,
                                    ) { calculatorText = it }
                                }
                                Screen.HISTORY -> {
                                    HistoryScreen(
                                        historyManager = historyManager,
                                        onItemClick = { text ->
                                            calculatorText = text
                                            currentScreen = Screen.CALCULATOR
                                        }
                                    )
                                }
                                Screen.SETTINGS -> {
                                    SettingsScreen(
                                        settingsManager = settingsManager,
                                        currentFont = selectedFont
                                    )
                                }
                            }
                        }
                    }

                    if (showExitDialog) {
                        ConfirmationDialog(
                            title = "خروج",
                            message = "آیا می‌خواهید از برنامه خارج شوید؟",
                            onConfirm = { finish() },
                            onDismiss = { showExitDialog = false },
                            confirmText = "خروج",
                            dismissText = "ماندن"
                        )
                    }

                    if (isDownloading) {
                        Dialog(onDismissRequest = { }) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                tonalElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "در حال دانلود به‌روزرسانی...",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${(downloadProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    updateInfo?.let { info ->
                        UpdateDialog(
                            versionName = info.versionName,
                            changeLog = info.releaseNotes,
                            isForceUpdate = info.isForceUpdate,
                            onDownloadRequest = {
                                val fileName = "jafr-update-${info.versionName}.apk"
                                val id = updateManager.downloadAndInstall(info.url, fileName)
                                if (id != -1L) {
                                    isDownloading = true
                                    scope.launch {
                                        updateManager.getDownloadProgress(id).collect { progress ->
                                            downloadProgress = progress
                                            if (progress >= 1f) {
                                                isDownloading = false
                                                if (!info.isForceUpdate) {
                                                    updateInfo = null
                                                }
                                            }
                                        }
                                    }
                                    if (!info.isForceUpdate) {
                                        updateInfo = null
                                    }
                                }
                            },
                            onDismiss = { if (!info.isForceUpdate) updateInfo = null }
                        )
                    }
                }
            }
        }
    }
}
