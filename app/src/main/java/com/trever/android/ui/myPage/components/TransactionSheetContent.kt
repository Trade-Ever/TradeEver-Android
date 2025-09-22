package com.trever.android.ui.myPage.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.R
import java.text.NumberFormat
import java.util.Locale

fun formatAmountToManwon(amount: Long): String {
    if (amount < 10000) return "${NumberFormat.getNumberInstance(Locale.KOREA).format(amount)}원"
    return "${amount / 10000}만원"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheetContent(
    title: String,
    bankName: String,
    accountNumber: String,
    bankLogoResId: Int,
    preSetAmounts: List<Long>,
    actionButtonText: String,
    onActionClick: (amount: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var directInputAmountText by remember { mutableStateOf("") }
    var visuallySelectedButtonAmount by remember { mutableStateOf<Long?>(null) }

    val currentAmountForAction = remember(directInputAmountText) {
        directInputAmountText.toLongOrNull() ?: 0L
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.Start)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = bankLogoResId),
                contentDescription = "$bankName 로고",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = bankName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = accountNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (preSetAmounts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                preSetAmounts.forEach { amount ->
                    AmountButton(
                        amount = amount,
                        isSelected = visuallySelectedButtonAmount == amount,
                        onClick = { 
                            directInputAmountText = amount.toString()
                            visuallySelectedButtonAmount = amount
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        OutlinedTextField(
            value = directInputAmountText,
            onValueChange = { newText ->
                val filteredText = newText.filter { char -> char.isDigit() }
                directInputAmountText = filteredText
                val longValue = filteredText.toLongOrNull()
                if (longValue != null && preSetAmounts.contains(longValue)) {
                    visuallySelectedButtonAmount = longValue
                } else {
                    visuallySelectedButtonAmount = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("직접 입력 (원)") },
            placeholder = { Text("금액을 숫자로 입력하세요") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onActionClick(currentAmountForAction) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = currentAmountForAction > 0
        ) {
            Text(actionButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AmountButton(
    amount: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFEEEEEE) else Color.Transparent
    // 텍스트 색상을 선택 여부와 관계없이 onSurface (보통 검은색 계열)로 변경
    val textColor = MaterialTheme.colorScheme.onSurface 

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor // Text에 적용될 색상
        ),
        border = null, // 테두리 없음
        contentPadding = PaddingValues(horizontal = 16.dp) 
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(
                text = formatAmountToManwon(amount), 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium
                // textColor는 Button의 contentColor에 의해 설정됨
            )
        }
    }
}
