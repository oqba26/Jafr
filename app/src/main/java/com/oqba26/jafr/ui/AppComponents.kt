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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JafrRowCard(row: JafrRow) {
    val isFinal = row.title.contains("نهایی") || row.title.contains("مستحصله")
    val letters = row.letters.split("  ").filter { it.isNotBlank() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFinal) 6.dp else 2.dp),
        colors = if (isFinal) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) else CardDefaults.cardColors(),
        border = if (isFinal) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isFinal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                letters.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isFinal) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isFinal) MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
