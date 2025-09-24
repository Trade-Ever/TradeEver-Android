package com.trever.android.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun CommonDialog(
    showDialog: Boolean,
    title: String = "",
    message: String,
    confirmButtonText: String = "확인",
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss?.invoke() ?: onConfirm()  // 바깥 터치 시 기본은 confirm
            },
            title = {
                if (title.isNotEmpty()) Text(title)
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                dismissButtonText?.let {
                    TextButton(onClick = { onDismiss?.invoke() }) {
                        Text(it)
                    }
                }
            }
        )
    }
}