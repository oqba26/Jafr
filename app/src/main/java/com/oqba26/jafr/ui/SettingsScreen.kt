package com.oqba26.jafr.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.SettingsManager
import com.oqba26.jafr.util.getFontFamily
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsManager: SettingsManager, currentFont: String) {
    val coroutineScope = rememberCoroutineScope()
    val currentDefaultType by settingsManager.defaultType.collectAsState(initial = "JAFR_15")

    val fonts = listOf(
        "vazirmatn" to "وزیر متن",
        "estedad" to "استعداد",
        "byekan" to "یکان",
        "iraniansans" to "ایران سنس",
        "sahel" to "ساحل",
    )

    val abjadTypes = listOf(
        com.oqba26.jafr.AbjadType.JAFR_15,
        com.oqba26.jafr.AbjadType.KABIR,
        com.oqba26.jafr.AbjadType.SAGHIR,
        com.oqba26.jafr.AbjadType.WASAIT
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "صفحه اصلی پیش‌فرض:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        abjadTypes.forEach { type ->
            val isSelected = currentDefaultType == type.name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        coroutineScope.launch {
                            settingsManager.saveDefaultType(type.name)
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Text(
                    text = type.label,
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

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
