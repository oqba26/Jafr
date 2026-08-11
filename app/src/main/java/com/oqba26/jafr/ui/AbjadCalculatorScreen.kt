package com.oqba26.jafr.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.Jafr15Result
import com.oqba26.jafr.NadhiraType
import com.oqba26.jafr.SpellAnalysis
import com.oqba26.jafr.TopicAnalysis
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
    modifier: Modifier = Modifier,
    initialText: String = "",
    onTextChange: (String) -> Unit = {}
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var selectedNadhira by remember { mutableStateOf(NadhiraType.ABJAD) }
    val coroutineScope = rememberCoroutineScope()
    
    // همگام سازی متن داخلی با تغییرات بیرونی
    LaunchedEffect(text) {
        onTextChange(text)
    }

    val names = remember(text) { AbjadUtils.extractNames(text) }
    val isQuestionComplete = remember(text, names) {
        val trimmed = text.trim()
        (trimmed.endsWith("؟") || trimmed.endsWith("?")) && 
        names.first != null && names.second != null && 
        trimmed.length > 10 // حداقل طول برای جلوگیری از ورودی‌های خیلی کوتاه مثل «؟»
    }

    val result = remember(text, selectedType) { AbjadUtils.calculate(text, selectedType) }
    val jafrResult = remember(text, selectedType, selectedNadhira, isQuestionComplete) { 
        if (selectedType == AbjadType.JAFR_15 && isQuestionComplete) 
            AbjadUtils.calculateJafr15(text, selectedNadhira, PersianDate()) 
        else null 
    }

    // حذف ذخیره‌سازی خودکار برای انواع دیگر ابجد و جلوگیری از تکرار در جفر
    var lastSavedText by remember { mutableStateOf("") }
    var lastSavedType by remember { mutableStateOf<AbjadType?>(null) }

    LaunchedEffect(jafrResult) {
        val trimmedText = text.trim()
        if (jafrResult != null && isQuestionComplete && selectedType == AbjadType.JAFR_15) {
            // فقط اگر متن (بدون فاصله‌های اضافه) یا نوع تغییر کرده باشد و قبلاً ذخیره نشده باشد
            if (trimmedText != lastSavedText || selectedType != lastSavedType) {
                val pDate = PersianDate()
                val formatter = PersianDateFormat("Y/m/d H:i:s")
                val timestamp = formatter.format(pDate)

                val currentItem = HistoryItem(
                    text = trimmedText,
                    firstName = names.first,
                    motherName = names.second,
                    result = 0,
                    answer = jafrResult.answer,
                    type = selectedType,
                    timestamp = timestamp
                )
                coroutineScope.launch {
                    historyManager.addHistoryItem(currentItem)
                    lastSavedText = trimmedText
                    lastSavedType = selectedType
                }
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
                            SpellCard(taqsimat.spell!!, taqsimat.direction)
                        }
                    }
                    taqsimat?.topics?.forEach { topic ->
                        item(key = "topic-${topic.topic}") {
                            TopicCard(topic)
                        }
                    }
                    items(jafrResult.rows) { row ->
                        JafrRowCard(row)
                    }
                }
            } else if (text.isNotBlank()) {
                // نمایش راهنما در صورت ناقص بودن سوال
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

private val SaadBadgeColor = Color(0xFF66BB6A)
private val NahsBadgeColor = Color(0xFFEF5350)
private val NeutralBadgeColor = Color(0xFFFFCA28)

@Composable
private fun JafrAnswerCard(result: Jafr15Result) {
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
private fun ReportItem(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun ConfidenceBadge(confidence: String) {
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
private fun DispositionBadge(disposition: String) {
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
private fun LevelBadge(level: String) {
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

private fun topicAccent(topicName: String): Color = when (topicName) {
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
private fun TopicCard(topic: TopicAnalysis) {
    val accent = topicAccent(topic.topic)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "تحلیل موضوع: ${topic.topic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                DispositionBadge(topic.level)
            }

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

@Composable
private fun TopicHighlightChip(key: String, value: String, accent: Color) {
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
private fun SpellCard(spell: SpellAnalysis, direction: String) {
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

            Spacer(modifier = Modifier.height(4.dp))
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
