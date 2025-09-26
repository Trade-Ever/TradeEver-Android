package com.trever.android.ui.sellcar.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trever.android.ui.theme.textPrimaryColor
import com.trever.android.ui.theme.textSecondaryColor

@Composable
fun CustomInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.textPrimaryColor) },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.textPrimaryColor,
            unfocusedTextColor = MaterialTheme.colorScheme.textPrimaryColor,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.textSecondaryColor
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun CustomInputFieldPreview() {
    var text by remember { mutableStateOf("") }
    CustomInputField(label = "예시 입력", value = text, onValueChange = { text = it })
}
