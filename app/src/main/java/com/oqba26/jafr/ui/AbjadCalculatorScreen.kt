package com.oqba26.jafr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.NadhiraType
import com.oqba26.jafr.model.HistoryItem
import com.oqba26.jafr.util.PersianNumberVisualTransformation
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AbjadCalculatorScreen(
    selectedType: AbjadType,
    historyManager: HistoryManager,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var selectedNadhira by remember { mutableStateOf(NadhiraType.ABJAD) }
    val coroutineScope = rememberCoroutineScope()
    
    val result = remember(text, selectedType) { AbjadUtils.calculate(text, selectedType) }
    val jafrResult = remember(text, selectedType, selectedNadhira) { 
        if (selectedType == AbjadType.JAFR_15) AbjadUtils.calculateJafr15(text, selectedNadhira) else null 
    }

    LaunchedEffect(text, selectedType) {
        if (text.isNotBlank() && selectedType != AbjadType.JAFR_15) {
            delay(1500.milliseconds)
            
            val pDate = PersianDate()
            val formatter = PersianDateFormat("Y/m/d H:i:s")
            val timestamp = formatter.format(pDate)

            val currentItem = HistoryItem(
                text = text,
                result = result.total,
                type = selectedType,
                timestamp = timestamp
            )
            
            coroutineScope.launch {
                historyManager.addHistoryItem(currentItem)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedType == AbjadType.JAFR_15) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("نوع نظیره:")
                Spacer(modifier = Modifier.width(8.dp))
                NadhiraType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedNadhira == type,
                        onClick = { selectedNadhira = type },
                        label = { Text(type.label) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("متن یا نام را وارد کنید") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
            visualTransformation = PersianNumberVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedType == AbjadType.JAFR_15 && jafrResult != null) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("پاسخ استخراج شده (نطق):", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = jafrResult.answer,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp
                            )
                        }
                    }
                }
                items(jafrResult.rows) { row ->
                    JafrRowCard(row)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("مقدار نهایی (${selectedType.label}):")
                    Text(
                        text = AbjadUtils.toPersianNumber(result.total),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (result.breakdown.isNotEmpty()) {
                Text(
                    "تفکیک حروف:",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    result.breakdown.forEach { (char, value) ->
                        LetterCard(char, value)
                    }
                }
            }
        }
    }
}
