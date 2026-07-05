package com.oqba26.jafr.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.SettingsManager
import com.oqba26.jafr.util.getFontFamily
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsManager: SettingsManager, currentFont: String) {
    val coroutineScope = rememberCoroutineScope()
    val fonts = listOf(
        "vazirmatn" to "وزیر متن",
        "estedad" to "استعداد",
        "byekan" to "یکان",
        "iraniansans" to "ایران سنس",
        "sahel" to "ساحل"
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            "انتخاب فونت اصلی برنامه:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        fonts.forEach { (fontKey, fontLabel) ->
            val isSelected = currentFont == fontKey
            Surface(
                onClick = {
                    coroutineScope.launch {
                        settingsManager.saveFont(fontKey)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fontLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = getFontFamily(fontKey)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
