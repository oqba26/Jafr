package com.oqba26.jafr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.model.HistoryItem
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(historyManager: HistoryManager) {
    val history by historyManager.historyList.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showDeleteAllDialog by remember { mutableStateOf(value = false) }
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (history.isNotEmpty()) {
            TextButton(
                onClick = { showDeleteAllDialog = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("حذف همه")
            }
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("تاریخچه خالی است", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    HistoryRowWithActions(item = item) {
                        itemToDelete = item
                    }
                }
            }
        }
    }

    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "حذف کل تاریخچه",
            message = "آیا از حذف تمام موارد تاریخچه اطمینان دارید؟ این عمل غیرقابل بازگشت است.",
            onConfirm = {
                coroutineScope.launch {
                    historyManager.clearHistory()
                    showDeleteAllDialog = false
                }
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }

    itemToDelete?.let { item ->
        ConfirmationDialog(
            title = "حذف مورد",
            message = "آیا از حذف این مورد اطمینان دارید؟",
            onConfirm = {
                coroutineScope.launch {
                    historyManager.deleteHistoryItem(item.id)
                    itemToDelete = null
                }
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun HistoryRowWithActions(item: HistoryItem, onDelete: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(item.text, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.type.label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = com.oqba26.jafr.AbjadUtils.toPersianNumber(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = com.oqba26.jafr.AbjadUtils.toPersianNumber(item.result),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}
