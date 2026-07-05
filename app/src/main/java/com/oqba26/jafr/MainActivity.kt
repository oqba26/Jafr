package com.oqba26.jafr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.jafr.ui.theme.JafrTheme
import kotlinx.coroutines.launch

import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PersianNumberVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val input = text.text
        val sb = StringBuilder()
        
        // Simple mapping for demonstration - in real app would need more complex digit/comma mapping
        for (char in input) {
            if (char.isDigit()) {
                sb.append(persianDigits[char - '0'])
            } else {
                sb.append(char)
            }
        }
        
        return TransformedText(
            androidx.compose.ui.text.AnnotatedString(sb.toString()),
            OffsetMapping.Identity
        )
    }
}

data class HistoryItem(val text: String, val result: Int, val type: AbjadType)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager(this)
        setContent {
            val selectedFont by settingsManager.selectedFont.collectAsState(initial = "vazirmatn_regular")
            val fontFamily = remember(selectedFont) { getFontFamily(selectedFont) }
            
            val customTypography = Typography(
                bodyLarge = TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
                headlineLarge = TextStyle(fontFamily = fontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold),
                titleMedium = TextStyle(fontFamily = fontFamily, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                labelMedium = TextStyle(fontFamily = fontFamily, fontSize = 12.sp)
            )

            JafrTheme(typography = customTypography) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("مـحـاسـبـه‌گـر جـفـر") },
                            actions = {
                                var showSettings by remember { mutableStateOf(false) }
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                                }
                                if (showSettings) {
                                    SettingsDialog(
                                        onDismiss = { showSettings = false },
                                        settingsManager = settingsManager,
                                        currentFont = selectedFont
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AbjadCalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(onDismiss: () -> Unit, settingsManager: SettingsManager, currentFont: String) {
    val coroutineScope = rememberCoroutineScope()
    val fonts = listOf(
        "vazirmatn_regular", "vazirmatn_bold", "vazirmatn_light", "vazirmatn_medium",
        "estedad_regular", "estedad_bold", "estedad_light", "estedad_medium",
        "byekan", "iraniansans", "sahel_bold", "sahel_black"
    )
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیمات ظاهری") },
        text = {
            Column {
                Text("انتخاب فونت:")
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentFont)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        fonts.forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font) },
                                onClick = {
                                    coroutineScope.launch {
                                        settingsManager.saveFont(font)
                                        expanded = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

fun getFontFamily(fontName: String): FontFamily {
    val resId = when (fontName) {
        "vazirmatn_regular" -> R.font.vazirmatn_regular
        "vazirmatn_bold" -> R.font.vazirmatn_bold
        "vazirmatn_light" -> R.font.vazirmatn_light
        "vazirmatn_medium" -> R.font.vazirmatn_medium
        "estedad_regular" -> R.font.estedad_regular
        "estedad_bold" -> R.font.estedad_bold
        "estedad_light" -> R.font.estedad_light
        "estedad_medium" -> R.font.estedad_medium
        "byekan" -> R.font.byekan
        "iraniansans" -> R.font.iraniansans
        "sahel_bold" -> R.font.sahel_bold
        "sahel_black" -> R.font.sahel_black
        else -> R.font.vazirmatn_regular
    }
    return FontFamily(Font(resId))
}

@Composable
fun AbjadCalculatorScreen(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AbjadType.KABIR) }
    var selectedNadhira by remember { mutableStateOf(NadhiraType.ABJAD) }
    
    val result = remember(text, selectedType) { AbjadUtils.calculate(text, selectedType) }
    val jafrResult = remember(text, selectedType, selectedNadhira) { 
        if (selectedType == AbjadType.JAFR_15) AbjadUtils.calculateJafr15(text, selectedNadhira) else null 
    }
    val history = remember { mutableStateListOf<HistoryItem>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Abjad Type Selector
        ScrollableTabRow(
            selectedTabIndex = selectedType.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            AbjadType.entries.forEach { type ->
                Tab(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    text = { Text(type.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedType == AbjadType.JAFR_15) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("متن یا نام را وارد کنید") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
            visualTransformation = PersianNumberVisualTransformation(),
            trailingIcon = {
                if (text.isNotEmpty() && selectedType != AbjadType.JAFR_15) {
                    IconButton(onClick = {
                        history.add(0, HistoryItem(text, result.total, selectedType))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "ذخیره در تاریخچه")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedType == AbjadType.JAFR_15 && jafrResult != null) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(result.breakdown) { (char, value) ->
                        LetterCard(char, value)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تاریخچه محاسبات", style = MaterialTheme.typography.titleMedium)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { item ->
                    HistoryRow(item)
                }
            }
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

@Composable
fun HistoryRow(item: HistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(AbjadUtils.toPersianNumber(item.result), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Column(horizontalAlignment = Alignment.End) {
                Text(item.text, style = MaterialTheme.typography.bodyLarge)
                Text(item.type.label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
