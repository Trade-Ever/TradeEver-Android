package com.trever.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CarNumberScreen() {
    var carNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ProgressBar(steps = 5, currentStep = 1)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "등록할 차량 번호를 입력해주세요",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = carNumber,
            onValueChange = { carNumber = it },
            placeholder = {
                Text(
                    text = "237가 4821",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* TODO: 다음 화면으로 이동 로직 */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            enabled = carNumber.isNotBlank()
        ) {
            Text(text = "다음", color = Color.White, fontSize = 18.sp)
        }
    }
}

//@Composable
//fun ProgressBar(steps: Int, currentStep: Int) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        repeat(steps) { index ->
//            val color = if (index + 1 == currentStep) Color(0xFF6200EE) else Color.LightGray
//            Box(
//                modifier = Modifier
//                    .width(if (index + 1 == currentStep) 24.dp else 8.dp)
//                    .height(4.dp)
//                    .background(color, RoundedCornerShape(2.dp))
//                    .padding(horizontal = 4.dp)
//            )
//        }
//    }
//}

@Preview(showBackground = true)
@Composable
fun CarNumberScreenPreview() {
    CarNumberScreen()
}