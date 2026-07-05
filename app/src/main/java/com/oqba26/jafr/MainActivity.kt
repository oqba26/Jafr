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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import com.oqba26.jafr.model.Screen
import com.oqba26.jafr.ui.*
import com.oqba26.jafr.ui.theme.JafrTheme
import com.oqba26.jafr.util.*

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
            var showExitDialog by remember { mutableStateOf(value = false) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
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
                                        historyManager = historyManager
                                    )
                                }
                                Screen.HISTORY -> {
                                    HistoryScreen(historyManager = historyManager)
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

                    updateInfo?.let { info ->
                        ConfirmationDialog(
                            title = "بروزرسانی جدید",
                            message = "نسخه ${info.versionName} موجود است.\n\nتغییرات:\n${info.releaseNotes}",
                            onConfirm = {
                                updateManager.downloadAndInstall(info.url, "jafr-update-${info.versionName}.apk")
                                updateInfo = null
                            },
                            onDismiss = { updateInfo = null },
                            confirmText = "آپدیت",
                            dismissText = "بعداً"
                        )
                    }
                }
            }
        }
    }
}
