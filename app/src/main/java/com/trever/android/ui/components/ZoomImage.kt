package com.trever.android.ui.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale

@Composable
fun ZoomImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 4f
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    // 팬은 스케일에 비례
                    offsetX += pan.x
                    offsetY += pan.y
                    scale = newScale
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

//@Composable
//fun ZoomImage(
//    bitmap: android.graphics.Bitmap,
//    modifier: Modifier = Modifier
//) {
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    val minScale = 1f
//    val maxScale = 4f
//
//    Box(
//        modifier = modifier.pointerInput(Unit) {
//            // 한 손가락 드래그는 무시하고(부모 스크롤 사용), 핀치 또는 확대 상태에서만 팬/줌
//            detectTransformGestures(panZoomLock = true) { _, pan, zoom, _ ->
//                val newScale = (scale * zoom).coerceIn(minScale, maxScale)
//                val wasZooming = scale > 1f
//                val isZooming = newScale > 1f
//
//                scale = newScale
//                if (isZooming || wasZooming) {
//                    // 확대 중에만 팬 허용 (부모 스크롤과 충돌 방지)
//                    offset += pan
//                }
//            }
//        }
//    ) {
//        Image(
//            bitmap = bitmap.asImageBitmap(),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .graphicsLayer(
//                    scaleX = scale,
//                    scaleY = scale,
//                    translationX = offset.x,
//                    translationY = offset.y
//                ),
//            contentScale = ContentScale.FillWidth
//        )
//    }
//}