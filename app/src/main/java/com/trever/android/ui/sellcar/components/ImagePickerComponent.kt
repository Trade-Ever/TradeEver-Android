package com.trever.android.ui.sellcar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerComponent(
    modifier: Modifier = Modifier,
    onImageSelected: () -> Unit
) {
    Button(
        onClick = onImageSelected, 
        modifier = modifier,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = "Select Image")
        Text(text = "이미지 선택", color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePickerComponentPreview() {
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        ImagePickerComponent(onImageSelected = {})
    }
}
