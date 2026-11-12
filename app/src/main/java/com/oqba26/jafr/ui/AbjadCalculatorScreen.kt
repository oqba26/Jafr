package com.oqba26.jafr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.NadhiraType
import com.oqba26.jafr.ui.components.*
import com.oqba26.jafr.util.PersianNumberVisualTransformation

private const val PREFIX = "یا هو "

private fun formatWithPrefix(text: String): String {
    return when {
        text.isEmpty() -> PREFIX
        text.startsWith(PREFIX) -> text
        text.startsWith("یا هو") -> PREFIX + text.removePrefix("یا هو").trimStart()
        else -> PREFIX + text.trimStart()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AbjadCalculatorScreen(
    selectedType: AbjadType,
    historyManager: HistoryManager,
    modifier: Modifier = Modifier,
    initialText: String = "",
    viewModel: AbjadCalculatorViewModel = viewModel(
        factory = AbjadCalculatorViewModel.provideFactory(historyManager)
    ),
    onTextChange: (String) -> Unit = {}
) {
    val prefix = PREFIX
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var tfValue by remember {
        val initialCombined = formatWithPrefix(initialText)
        mutableStateOf(
            TextFieldValue(
                text = initialCombined,
                selection = TextRange(initialCombined.length)
            )
        )
    }

    LaunchedEffect(selectedType) {
        viewModel.onTypeSelected(selectedType)
    }

    LaunchedEffect(initialText) {
        val formatted = formatWithPrefix(initialText)
        if (formatted != tfValue.text) {
            tfValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )
            viewModel.onTextChanged(formatted)
        }
    }

    LaunchedEffect(tfValue.selection) {
        if (!tfValue.selection.collapsed) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(tfValue.text) {
        viewModel.onTextChanged(tfValue.text)
        onTextChange(tfValue.text)
    }

    val selectedNadhira by viewModel.selectedNadhira.collectAsState()
    val cleanUserText by viewModel.cleanText.collectAsState()
    val abjadResult by viewModel.abjadResult.collectAsState()
    val jafrResult by viewModel.jafrResult.collectAsState()
    val jafrNumericalResult by viewModel.jafrNumericalResult.collectAsState()
    val tabayeResult by viewModel.tabayeResult.collectAsState()

    // ویژگی‌های استخراج‌شده از کتاب مفتاح طلاسم کنوز
    val muwakkilResult by viewModel.muwakkilResult.collectAsState()
    val kawakibHourInfo by viewModel.kawakibHourInfo.collectAsState()
    val rijalAlGhaibInfo by viewModel.rijalAlGhaibInfo.collectAsState()
    val tabayeMizanResult by viewModel.tabayeMizanResult.collectAsState()
    val ancientScriptResult by viewModel.ancientScriptResult.collectAsState()
    val ofaqExtendedResult by viewModel.ofaqExtendedResult.collectAsState()

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
                        onClick = { viewModel.onNadhiraSelected(type) },
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
                var selStart = newValue.selection.start
                var selEnd = newValue.selection.end

                if (!newText.startsWith("یا هو")) {
                    val userPortion = newText.removePrefix("یا").removePrefix("هو").trimStart()
                    newText = prefix + userPortion
                    if (newValue.text.length < newText.length) {
                        val diff = newText.length - newValue.text.length
                        selStart += diff
                        selEnd += diff
                    }
                } else if (!newText.startsWith(prefix)) {
                    val userPortion = newText.removePrefix("یا هو").trimStart()
                    newText = prefix + userPortion
                    val diff = prefix.length - "یا هو".length
                    selStart += diff
                    selEnd += diff
                } else if (newText.length < prefix.length) {
                    newText = prefix
                }

                val minSelection = prefix.length
                val newSelStart = maxOf(minSelection, selStart).coerceAtMost(newText.length)
                val newSelEnd = maxOf(minSelection, selEnd).coerceAtMost(newText.length)

                tfValue = newValue.copy(
                    text = newText,
                    selection = TextRange(newSelStart, newSelEnd)
                )
            },
            label = { Text("متن یا نام را وارد کنید") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
            visualTransformation = PersianNumberVisualTransformation(),
            placeholder = { Text("مثلاً: آیا علی زاده زهرا طلسم شده است؟", color = Color.Gray.copy(alpha = 0.5f)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedType == AbjadType.JAFR_15) {
            val jResult = jafrResult
            if (jResult != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        JafrAnswerCard(jResult)
                    }
                    val numRes = jafrNumericalResult
                    if (numRes != null) {
                        item {
                            JafrNumericalCard(numRes)
                        }
                    }
                    val taqsimat = jResult.taqsimat
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

                    // کارت‌های استخراج‌شده از کتاب مفتاح طلاسم کنوز
                    muwakkilResult?.let { mw ->
                        item {
                            MuwakkilCard(mw)
                        }
                    }
                    kawakibHourInfo?.let { kh ->
                        item {
                            KawakibHoursCard(kh)
                        }
                    }
                    rijalAlGhaibInfo?.let { rg ->
                        item {
                            RijalAlGhaibCard(rg)
                        }
                    }
                    tabayeMizanResult?.let { tm ->
                        item {
                            TabayeMizanCard(tm)
                        }
                    }
                    ancientScriptResult?.let { ascr ->
                        item {
                            AncientScriptCard(ascr)
                        }
                    }
                    ofaqExtendedResult?.let { ofq ->
                        item {
                            OfaqExtendedCard(
                                result = ofq,
                                onSizeChange = { viewModel.onOfaqSizeSelected(it) }
                            )
                        }
                    }

                    items(jResult.rows) { row ->
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
            val numRes = jafrNumericalResult
            if (numRes != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        JafrNumericalCard(numRes)
                    }
                    ofaqExtendedResult?.let { ofq ->
                        item {
                            OfaqExtendedCard(
                                result = ofq,
                                onSizeChange = { viewModel.onOfaqSizeSelected(it) }
                            )
                        }
                    }
                    muwakkilResult?.let { mw ->
                        item {
                            MuwakkilCard(mw)
                        }
                    }
                    kawakibHourInfo?.let { kh ->
                        item {
                            KawakibHoursCard(kh)
                        }
                    }
                    rijalAlGhaibInfo?.let { rg ->
                        item {
                            RijalAlGhaibCard(rg)
                        }
                    }
                    tabayeMizanResult?.let { tm ->
                        item {
                            TabayeMizanCard(tm)
                        }
                    }
                    val tabaye = tabayeResult
                    if (tabaye != null) {
                        item {
                            TabayeCard(tabaye)
                        }
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
            val aResult = abjadResult
            if (cleanUserText.isNotBlank() && aResult != null) {
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
                                    text = AbjadUtils.toPersianNumber(aResult.total),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    if (aResult.breakdown.isNotEmpty()) {
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
                                    aResult.breakdown.forEach { (char, value) ->
                                        LetterCard(char, value)
                                    }
                                }
                            }
                        }

                        val tabaye = tabayeResult
                        if (tabaye != null) {
                            item {
                                TabayeCard(tabaye)
                            }
                        }

                        tabayeMizanResult?.let { tm ->
                            item {
                                TabayeMizanCard(tm)
                            }
                        }

                        ancientScriptResult?.let { ascr ->
                            item {
                                AncientScriptCard(ascr)
                            }
                        }

                        kawakibHourInfo?.let { kh ->
                            item {
                                KawakibHoursCard(kh)
                            }
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
