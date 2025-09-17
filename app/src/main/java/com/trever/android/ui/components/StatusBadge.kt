package com.trever.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(
    text: String,
    background: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    padding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
) {
    Row(
        modifier = modifier
            .background(background, shape)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** 프리셋 */
@Composable
fun AuctionBadge(modifier: Modifier = Modifier) =
    StatusBadge(text = "경매", background = com.trever.android.ui.theme.Red_2, modifier = modifier)

@Composable
fun SellingBadge(modifier: Modifier = Modifier) =
    StatusBadge(text = "판매중", background = Color(0xFF00C364), modifier = modifier)