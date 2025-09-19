package com.trever.android.ui.components
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trever.android.ui.theme.G_300
import com.trever.android.ui.theme.backgroundColor

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    @DrawableRes leadingIconRes: Int? = null,
    @DrawableRes trailingIconRes: Int? = null,
    iconSize: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)
) {
    val cs = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = shape,
        contentPadding = contentPadding,
        border = BorderStroke(1.dp, if (enabled) cs.primary else cs.G_300),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = cs.backgroundColor,
            contentColor = cs.primary,
            disabledContainerColor = cs.backgroundColor,
            disabledContentColor = cs.G_300
        )
    ) {
        if (leadingIconRes != null) {
            Icon(painterResource(leadingIconRes), null, Modifier.size(iconSize))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        if (trailingIconRes != null) {
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(trailingIconRes), null, Modifier.size(iconSize))
        }
    }
}