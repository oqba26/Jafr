package com.oqba26.jafr.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.NadhiraType
import kotlinx.coroutines.launch
import com.oqba26.jafr.model.HistoryItem
import saman.zamani.persiandate.PersianDate

@Composable
fun HistoryScreen(
    historyManager: HistoryManager,
    onItemClick: (String) -> Unit = {}
) {
    val history by historyManager.historyList.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // فیلتر کردن لیست برای نمایش فقط موارد جفر ۱۵ سطری که نام و نام مادر دارند
    val filteredHistory = remember(history) {
        history.filter { 
            it.firstName != null && 
            it.motherName != null && 
            it.type == AbjadType.JAFR_15 
        }
    }

    // گروه‌بندی بر اساس نام شخص و سپس تاریخ
    val groupedHistory = remember(filteredHistory) {
        filteredHistory.groupBy { item ->
            "${item.firstName} زاده ${item.motherName}"
        }.mapValues { entry ->
            entry.value.groupBy { it.timestamp.split(" ")[0] }
        }
    }

    val expandedPersons = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
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
    ) {
        if (filteredHistory.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "حذف همه", tint = Color.Red)
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("تاریخچه هنوز خالی است", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedHistory.forEach { (personName, datesMap) ->
                    item(key = personName) {
                        val isExpanded = expandedPersons[personName] ?: false
                        PersonGroupCard(
                            name = personName,
                            totalItems = datesMap.values.sumOf { it.size },
                            isExpanded = isExpanded,
                            onToggle = { expandedPersons[personName] = !isExpanded },
                            datesContent = {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    datesMap.forEach { (date, items) ->
                                        DateSubGroup(
                                            date = date,
                                            items = items,
                                            onItemClick = onItemClick,
                                            onDeleteItem = { itemToDelete = it }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "حذف کل تاریخچه",
            message = "آیا از حذف تمام موارد اطمینان دارید؟",
            onConfirm = {
                coroutineScope.launch { historyManager.clearHistory(); showDeleteAllDialog = false }
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }

    itemToDelete?.let { item ->
        ConfirmationDialog(
            title = "حذف مورد",
            message = "این مورد حذف شود؟",
            onConfirm = {
                coroutineScope.launch { historyManager.deleteHistoryItem(item.id); itemToDelete = null }
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun PersonGroupCard(
    name: String,
    totalItems: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    datesContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(AbjadUtils.toPersianNumber(totalItems))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                datesContent()
            }
        }
    }
}

@Composable
fun DateSubGroup(
    date: String,
    items: List<HistoryItem>,
    onItemClick: (String) -> Unit,
    onDeleteItem: (HistoryItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AbjadUtils.toPersianNumber(date),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(start = 8.dp), color = Color.Gray.copy(alpha = 0.2f))
        }
        
        items.forEach { item ->
            HistoryItemFullCard(item, onItemClick, onDeleteItem)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HistoryItemFullCard(
    item: HistoryItem,
    onItemClick: (String) -> Unit,
    onDeleteItem: (HistoryItem) -> Unit
) {
    val jafrResult = remember(item.text) {
        AbjadUtils.calculateJafr15(item.text, NadhiraType.ABJAD, PersianDate())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.type.label} • ${AbjadUtils.toPersianNumber(item.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = { onItemClick(item.text) }) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش/محاسبه مجدد", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDeleteItem(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Full 15-line Jafar Cards
            JafrAnswerCard(jafrResult)
            
            val taqsimat = jafrResult.taqsimat
            if (taqsimat?.spell != null) {
                Spacer(modifier = Modifier.height(12.dp))
                SpellCard(taqsimat.spell, taqsimat.direction)
            }
            
            taqsimat?.topics?.forEach { topic ->
                Spacer(modifier = Modifier.height(12.dp))
                TopicCard(topic)
            }
            
            jafrResult.rows.forEach { row ->
                Spacer(modifier = Modifier.height(8.dp))
                JafrRowCard(row)
            }
        }
    }
}
