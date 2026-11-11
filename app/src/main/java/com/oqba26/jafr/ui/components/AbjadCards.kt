package com.oqba26.jafr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.Element
import com.oqba26.jafr.Jafr15Result
import com.oqba26.jafr.JafrRuleType
import com.oqba26.jafr.SpellAnalysis
import com.oqba26.jafr.TabayeAnalysis
import com.oqba26.jafr.TopicAnalysis
import com.oqba26.jafr.util.JafrNumericalResult

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
    "ازدواج" -> Color(0xFFEC4899)
    "سفر" -> Color(0xFF3B82F6)
    "کسب‌وکار" -> Color(0xFF10B981)
    "فرزند" -> Color(0xFFF59E0B)
    "بیماری و درمان" -> Color(0xFFEF4444)
    "خرید و فروش" -> Color(0xFF8B5CF6)
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
