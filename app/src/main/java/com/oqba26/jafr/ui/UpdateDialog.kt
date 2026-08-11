package com.oqba26.jafr.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UpdateDialog(
    versionName: String,
    changeLog: String,
    isForceUpdate: Boolean = false,
    onDownloadRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isForceUpdate) onDismiss() },
        title = {
            Text(
                text = if (isForceUpdate) "بروزرسانی اجباری ($versionName)"
                else "بروزرسانی جدید ($versionName)"
            )
        },
        text = { Text(text = changeLog.ifBlank { "نسخه جدید برنامه منتشر شده است." }) },
        confirmButton = {
            Button(onClick = onDownloadRequest) {
                Text("دانلود و نصب")
            }
        },
        dismissButton = {
            if (!isForceUpdate) {
                TextButton(onClick = onDismiss) {
                    Text("بعداً")
                }
            }
        }
    )
}
