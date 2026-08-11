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
        val historyManager = HistoryManager(this)
        
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState(initial = "vazirmatn")
            val fontFamily = remember(selectedFont) { getFontFamily(selectedFont) }
            val customTypography = remember(fontFamily) { createTypography(fontFamily) }
            var currentScreen by remember { mutableStateOf(Screen.CALCULATOR) }
            var selectedType by remember { mutableStateOf(AbjadType.KABIR) }
            var calculatorText by remember { mutableStateOf("") }
            var showExitDialog by remember { mutableStateOf(value = false) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            var downloadProgress by remember { mutableFloatStateOf(0f) }
            var isDownloading by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val updateManager = remember { UpdateManager(this@MainActivity) }

            // Check for updates
            LaunchedEffect(Unit) {
                updateInfo = updateManager.checkForUpdate()
            }

            // Handle Back Press for Navigation and Exit
            BackHandler(enabled = true) {
                if (currentScreen == Screen.CALCULATOR && selectedType == AbjadType.KABIR) {
                    showExitDialog = true
                } else {
                    currentScreen = Screen.CALCULATOR
                    selectedType = AbjadType.KABIR
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
                                            Screen.CALCULATOR -> "مـحـاسـبـه‌گـر جـفـر"
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
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        },
                        bottomBar = {
                            AppBottomBar(
                                currentScreen = currentScreen,
                                selectedType = selectedType,
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
                                        onTextChange = { calculatorText = it }
                                    )
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
