package com.trever.android.ui.sellcar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerComponent(
    modifier: Modifier = Modifier,
    onImageSelected: () -> Unit // 실제로는 선택된 이미지 URI 등을 전달해야 함
) {
    Button(onClick = onImageSelected, modifier = modifier) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = "Select Image")
        Text(text = "이미지 선택")
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePickerComponentPreview() {
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        ImagePickerComponent(onImageSelected = {})
    }
}
