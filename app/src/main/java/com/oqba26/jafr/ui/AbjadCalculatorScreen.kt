package com.oqba26.jafr.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.Element
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.Jafr15Result
import com.oqba26.jafr.JafrRuleType
import com.oqba26.jafr.NadhiraType
import com.oqba26.jafr.SpellAnalysis
import com.oqba26.jafr.TabayeAnalysis
import com.oqba26.jafr.TopicAnalysis
import com.oqba26.jafr.model.HistoryItem
import com.oqba26.jafr.util.JafrNumericalResult
import com.oqba26.jafr.util.JafrNumericalUtils
import com.oqba26.jafr.util.PersianNumberVisualTransformation
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AbjadCalculatorScreen(
    selectedType: AbjadType,
    historyManager: HistoryManager,
    modifier: Modifier = Modifier,
    initialText: String = "",
    onTextChange: (String) -> Unit = {},
) {
    val prefix = "یا هو "
    var tfValue by remember(initialText) {
        val initialCombined = when {
            initialText.isEmpty() -> prefix
            initialText.startsWith(prefix) -> initialText
            initialText.startsWith("یا هو") -> prefix + initialText.removePrefix("یا هو").trimStart()
            else -> prefix + initialText.trimStart()
        }
        mutableStateOf(
            TextFieldValue(
                text = initialCombined,
                selection = TextRange(initialCombined.length)
            )
        )
    }
    var selectedNadhira by remember { mutableStateOf(NadhiraType.ABJAD) }
    
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // همگام سازی متن داخلی با تغییرات بیرونی
    LaunchedEffect(tfValue.text) {
        onTextChange(tfValue.text)
    }

    val cleanUserText = remember(tfValue.text) { AbjadUtils.stripYaHoo(tfValue.text) }

    val names = remember(cleanUserText) { AbjadUtils.extractNames(cleanUserText) }
    val isQuestionComplete = remember(cleanUserText) {
        val trimmed = cleanUserText.trim()
        (trimmed.endsWith("؟") || trimmed.endsWith("?")) && (trimmed.length > 10)
    }

    val result = remember(cleanUserText, selectedType) { AbjadUtils.calculate(cleanUserText, selectedType) }
    val jafrResult = remember(cleanUserText, selectedType, selectedNadhira, isQuestionComplete) { 
        if (selectedType == AbjadType.JAFR_15 && isQuestionComplete && cleanUserText.isNotEmpty()) 
            AbjadUtils.calculateJafr15(cleanUserText, selectedNadhira, PersianDate()) 
        else null 
    }
    val jafrNumericalResult = remember(cleanUserText, selectedType, isQuestionComplete) {
        if (selectedType == AbjadType.JAFR_NUMERICAL && isQuestionComplete && cleanUserText.isNotEmpty())
            JafrNumericalUtils.calculateNumericalJafr(cleanUserText)
        else null
    }

    // ذخیره‌سازی همزمان جفر ۱۵ سطری و جفر عددی/وفقی برای هر سوال کامل
    var lastSavedText by remember { mutableStateOf("") }

    LaunchedEffect(cleanUserText, isQuestionComplete) {
        if (isQuestionComplete && cleanUserText.isNotEmpty()) {
            delay(1500.milliseconds) // وقفه ۱.۵ ثانیه‌ای برای اطمینان از پایان تایپ (Debounce)
            val trimmedText = cleanUserText.trim()

            if (trimmedText != lastSavedText) {
                val pDate = PersianDate()
                val formatter = PersianDateFormat("Y/m/d H:i:s")
                val timestamp = formatter.format(pDate)

                // 1. محاسبه و ذخیره جفر ۱۵ سطری
                val j15Res = AbjadUtils.calculateJafr15(trimmedText, selectedNadhira, pDate)
                val item15 = HistoryItem(
                    text = trimmedText,
                    firstName = names.first,
                    motherName = names.second,
                    result = 0,
                    answer = j15Res.answer,
                    type = AbjadType.JAFR_15,
                    timestamp = timestamp,
                )
                historyManager.addHistoryItem(item15)

                // 2. محاسبه و ذخیره جفر عددی و وفقی
                val jNumRes = JafrNumericalUtils.calculateNumericalJafr(trimmedText)
                val itemNum = HistoryItem(
                    text = trimmedText,
                    firstName = names.first,
                    motherName = names.second,
                    result = jNumRes.wafd,
                    answer = jNumRes.verdict,
                    type = AbjadType.JAFR_NUMERICAL,
                    timestamp = timestamp,
                )
                historyManager.addHistoryItem(itemNum)

                lastSavedText = trimmedText
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    if (scale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
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
            value = tfValue,
            onValueChange = { newValue ->
                var newText = newValue.text
                if (!newText.startsWith("یا هو")) {
                    val userPortion = newText.removePrefix("یا").removePrefix("هو").trimStart()
                    newText = prefix + userPortion
                } else if (newText.length < prefix.length) {
                    newText = prefix
                }

                val minSelection = prefix.length
                val newSelStart = maxOf(minSelection, newValue.selection.start)
                val newSelEnd = maxOf(minSelection, newValue.selection.end)

                tfValue = newValue.copy(
                    text = newText,
                    selection = TextRange(newSelStart, newSelEnd)
                )
            },
            label = { Text("متن یا نام را وارد کنید") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
            visualTransformation = PersianNumberVisualTransformation(),
            placeholder = { Text("مثلاً: آیا علی زاده زهرا طلسم شده است؟", color = Color.Gray.copy(alpha = 0.5f)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedType == AbjadType.JAFR_15) {
            if (jafrResult != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        JafrAnswerCard(jafrResult)
                    }
                    val taqsimat = jafrResult.taqsimat
                    if (taqsimat?.spell != null) {
                        item {
                            SpellCard(taqsimat.spell, taqsimat.direction)
                        }
                    }
                    taqsimat?.topics?.forEach { topic ->
                        item(key = "topic-${topic.topic}") {
                            TopicCard(topic)
                        }
                    }
                    taqsimat?.tabaye?.let { tb ->
                        item {
                            TabayeCard(tb)
                        }
                    }
                    item {
                        JafrRulesCard(cleanUserText)
                    }
                    items(jafrResult.rows) { row ->
                        JafrRowCard(row)
                    }
                }
            } else if (cleanUserText.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "متن وارد شده ناقص است.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "لطفاً سوال را به صورت کامل همراه با نام، نام مادر و علامت سوال در انتها وارد کنید.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "نمونه صحیح: آیا محمد زاده مریم در کار خود موفق می‌شود؟",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else if (selectedType == AbjadType.JAFR_NUMERICAL) {
            if (jafrNumericalResult != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        JafrNumericalCard(jafrNumericalResult)
                    }
                    val tabaye = AbjadUtils.analyzeTabaye(cleanUserText)
                    item {
                        TabayeCard(tabaye)
                    }
                }
            } else if (cleanUserText.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "متن وارد شده ناقص است.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "لطفاً سوال را به صورت کامل همراه با نام، نام مادر و علامت سوال در انتها وارد کنید.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "نمونه صحیح: آیا محمد زاده مریم در کار خود موفق می‌شود؟",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            if (cleanUserText.isNotBlank()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
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
                    }

                    if (result.breakdown.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "تفکیک حروف:",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    result.breakdown.forEach { (char, value) ->
                                        LetterCard(char, value)
                                    }
                                }
                            }
                        }

                        val tabaye = AbjadUtils.analyzeTabaye(cleanUserText)
                        item {
                            TabayeCard(tabaye)
                        }

                        item {
                            JafrRulesCard(cleanUserText)
                        }
                    }
                }
            }
        }
    }
}

val SaadBadgeColor = Color(0xFF66BB6A)
val NahsBadgeColor = Color(0xFFEF5350)
val NeutralBadgeColor = Color(0xFFFFCA28)

@Composable
fun JafrAnswerCard(result: Jafr15Result) {
    val t = result.taqsimat
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        if (t == null) {
            Text(
                text = result.answer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            return@Card
        }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("پاسخ استخراج شده (نطق):", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "جمع ابجد کبیر",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = AbjadUtils.toPersianNumber(t.total),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReportItem("جهت", t.direction)
                ReportItem("روز", t.day)
                ReportItem("کوکب", t.kawkab)
                ReportItem("برج", t.burj)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            t.person?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "طالع شخص (${p.firstName} زاده ${p.motherName})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "برج ${p.burj}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "طبع ${p.element}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "کوکب ${p.kawkab}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "منزل قمر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${t.manzel.name} — «${t.manzel.meaning}»",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                DispositionBadge(t.manzel.disposition)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حروف مستحصله",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "سعد ${AbjadUtils.toPersianNumber(t.saadCount)} | نحس ${AbjadUtils.toPersianNumber(t.nahsCount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                DispositionBadge(t.dominant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            t.cross?.let { c ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "همسویی سه مسیر",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        ConfidenceBadge(c.confidence)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    c.paths.forEach { p ->
                        Text(
                            text = "${p.label} (${AbjadUtils.toPersianNumber(p.total)}): منزل ${p.manzel.name} (${p.manzel.disposition})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نظیره‌ها: " + c.nadhiraPolarities.joinToString(" | ") { (label, pol) -> "$label: $pol" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    t.time?.let { tm ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قرینه زمان (${tm.weekday}): روز ${tm.dayKawkab} | ساعت ${tm.hourKawkab}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = tm.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "حکم نطق:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t.verdict,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ReportItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfidenceBadge(confidence: String) {
    val color = when (confidence) {
        "همسو" -> SaadBadgeColor
        "نسبتاً همسو" -> NeutralBadgeColor
        else -> NahsBadgeColor
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = confidence,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DispositionBadge(disposition: String) {
    val color = when (disposition) {
        "سعد" -> SaadBadgeColor
        "نحس" -> NahsBadgeColor
        else -> NeutralBadgeColor
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = disposition,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LevelBadge(level: String) {
    val color = when (level) {
        "قوی" -> NahsBadgeColor
        "متوسط" -> NeutralBadgeColor
        else -> SaadBadgeColor
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = level,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

fun topicAccent(topicName: String): Color = when (topicName) {
    "ازدواج" -> Color(0xFFEC4899)   // صورتی - محبت
    "سفر" -> Color(0xFF3B82F6)      // آبی - حرکت
    "کسب‌وکار" -> Color(0xFF10B981)  // سبز - رزق
    "فرزند" -> Color(0xFFF59E0B)     // کهربایی - فرزند
    "بیماری و درمان" -> Color(0xFFEF4444) // قرمز - بیماری
    "خرید و فروش" -> Color(0xFF8B5CF6)    // بنفش - معامله
    else -> Color(0xFF8B5CF6)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicCard(
    topic: TopicAnalysis,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    val accent = topicAccent(topic.topic)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "تحلیل موضوع: ${topic.topic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                DispositionBadge(topic.level)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = accent
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topic.highlights.forEach { (key, value) ->
                            TopicHighlightChip(key, value, accent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    topic.indicators.forEach { indicator ->
                        Text(
                            text = "• $indicator",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نتیجه:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = topic.verdict,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "یادآوری: قرائن حروف نشانه‌گر است نه علم غیب؛ تصمیم نهایی با شماست.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    topic.notice?.let { notice ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = topicAccent(topic.topic).copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicHighlightChip(key: String, value: String, accent: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpellCard(spell: SpellAnalysis, direction: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "تحلیل طلسم / سحر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                LevelBadge(spell.level)
            }

            Spacer(modifier = Modifier.height(8.dp))
            spell.indicators.forEach { indicator ->
                Text(
                    text = "• $indicator",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "حروف استخراجی عامل (تخلیص):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
            Text(
                text = spell.khalesLetters.map { it.toString() }.joinToString("  "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (spell.bastLetters.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "بسط ملفوظی (نام حروف):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = spell.bastLetters.map { it.toString() }.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            if (spell.factorMustahsalah.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "نطق مستحصله عامل (جواب درونی):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = spell.factorMustahsalah.map { it.toString() }.joinToString("  "),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "برج ${spell.factorBurj} | طبع ${spell.factorElement} | ${spell.factorGender} | کوکب ${spell.factorKawkab}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "جهت اثر: $direction",
                style = MaterialTheme.typography.bodySmall
            )
            if (spell.factorRelation.isNotBlank()) {
                Text(
                    text = spell.factorRelation,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "حکم طلسم:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
            Text(
                text = spell.verdict,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "یادآوری: در منابع سنتی تصریح شده که جفر علم غیب نیست؛ تطبیق نهایی اسم بر حروف، با بصیرت سائل است.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TabayeCard(
    tabaye: TabayeAnalysis,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "تحلیل طبایع چهارگانه حروف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(tabaye.dominantElement.colorHex).copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "غالب: ${tabaye.dominantElement.label}",
                        color = Color(tabaye.dominantElement.colorHex),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElementBarItem("آتش (ناری)", tabaye.fireCount, tabaye.firePercent, Color(Element.FIRE.colorHex))
                        ElementBarItem("باد (هوایی)", tabaye.airCount, tabaye.airPercent, Color(Element.AIR.colorHex))
                        ElementBarItem("آب (مائی)", tabaye.waterCount, tabaye.waterPercent, Color(Element.WATER.colorHex))
                        ElementBarItem("خاک (ترابی)", tabaye.earthCount, tabaye.earthPercent, Color(Element.EARTH.colorHex))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tabaye.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ElementBarItem(label: String, count: Int, percent: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${AbjadUtils.toPersianNumber(percent)}٪",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "(${AbjadUtils.toPersianNumber(count)} حرف)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun JafrRulesCard(
    text: String,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var selectedRule by remember { mutableStateOf(JafrRuleType.TARAQQI) }
    val ruleResult = remember(text, selectedRule) { AbjadUtils.applyJafrRule(text, selectedRule) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "قواعد چهارگانه جفر (سیر مراتب حروف)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        JafrRuleType.entries.forEach { rule ->
                            FilterChip(
                                selected = selectedRule == rule,
                                onClick = { selectedRule = rule },
                                label = { Text(rule.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ruleResult.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "سطر حاصل (${selectedRule.label}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ruleResult.transformedText.map { it.toString() }.joinToString("  "),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JafrNumericalCard(result: JafrNumericalResult) {
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
            Text(
                text = "جفر عددی و وفق ۳x۳ (خزانة الأسرار)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val badgeColor = when {
                result.polarity.contains("سعد") -> SaadBadgeColor
                result.polarity.contains("نحس") -> NahsBadgeColor
                else -> NeutralBadgeColor
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = result.polarity,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "جدول وفق مثلث عددی (بطن المجمع):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.matrix3x3.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { cellVal ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AbjadUtils.toPersianNumber(cellVal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReportItem("مفتاح (اول)", AbjadUtils.toPersianNumber(result.miftah))
                ReportItem("مغلاق (آخر)", AbjadUtils.toPersianNumber(result.maghlaq))
                ReportItem("عدد وفق (ضلع)", AbjadUtils.toPersianNumber(result.wafd))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "اسقاطات چهارگانه طالع وفق:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IsqatChip("برج", result.burjName)
                    IsqatChip("کوکب", result.kawkabName)
                    IsqatChip("عنصر", result.elementName)
                    IsqatChip("منزل", result.manzelName)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "حکم جفر عددی و وفقی:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.verdict,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IsqatChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

