package com.oqba26.jafr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.JafrRow
import com.oqba26.jafr.model.Screen

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    selectedType: AbjadType,
    onScreenSelected: (Screen) -> Unit,
    onTypeSelected: (AbjadType) -> Unit
) {
    if (currentScreen == Screen.CALCULATOR || currentScreen == Screen.HISTORY) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp
        ) {
            AbjadType.entries.forEach { type ->
                val icon = when (type) {
                    AbjadType.KABIR -> Icons.Default.Calculate
                    AbjadType.SAGHIR -> Icons.Default.KeyboardDoubleArrowDown
                    AbjadType.WASAIT -> Icons.Default.FilterCenterFocus
                    AbjadType.JAFR_15 -> Icons.Default.ViewComfy
                }
                NavigationBarItem(
                    selected = currentScreen == Screen.CALCULATOR && selectedType == type,
                    onClick = { 
                        onTypeSelected(type)
                        onScreenSelected(Screen.CALCULATOR)
                    },
                    icon = { Icon(icon, contentDescription = null) },
                    label = { 
                        Text(
                            text = type.label,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                )
            }
            NavigationBarItem(
                selected = currentScreen == Screen.HISTORY,
                onClick = { onScreenSelected(Screen.HISTORY) },
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                label = { Text("تاریخچه", fontSize = 10.sp) }
            )
        }
    }
}

@Composable
fun JafrRowCard(row: JafrRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = row.letters,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
fun LetterCard(char: Char, value: Int) {
    Column(
        modifier = Modifier
            .width(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = char.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = AbjadUtils.toPersianNumber(value), style = MaterialTheme.typography.bodySmall)
    }
}
