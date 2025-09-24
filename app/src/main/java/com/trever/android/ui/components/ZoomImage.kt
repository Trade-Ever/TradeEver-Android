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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale



@Composable
fun ZoomImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 4f
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(scale) {
                awaitEachGesture {
                    // 제스처 루프
                    do {
                        val event = awaitPointerEvent()

                        // 핀치/팬 변화량 계산
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        // 두 손가락(핀치) 중이거나 확대 상태면 내가 처리(소비)
                        val isMultiTouch = event.changes.size > 1
                        val willZoom = kotlin.math.abs(zoomChange - 1f) > 0.01f
                        val shouldHandle = isMultiTouch || scale > 1f || willZoom

                        if (shouldHandle) {
                            // 줌/팬 적용
                            val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                            scale = newScale
                            offset += panChange

                            // 내가 처리했으니 소비
                            event.changes.forEach { change ->
                                if (change.positionChanged()) change.consume()
                            }
                        } else {
                            // 1x + 한 손가락 드래그 ⇒ 소비하지 않음 (부모 verticalScroll이 받음)
                        }

                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
    }
}