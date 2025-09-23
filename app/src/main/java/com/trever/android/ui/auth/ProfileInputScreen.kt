package com.trever.android.ui.auth

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.components.AppFilledButton
import com.trever.android.ui.theme.G_200
import java.util.Calendar

@Composable
fun ProfileInputScreen(viewModel: AuthViewModel, onComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val cs = MaterialTheme.colorScheme

    val textFieldShape = RoundedCornerShape(14.dp)
    val borderColor = cs.G_200
    val activeBorderColor = cs.primary // 값이 있을 때 테두리 색
    val placeholderColor = cs.G_200
    val isButtonEnabled = name.isNotBlank() && phone.isNotBlank() && birth.isNotBlank() && region.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "추가정보 입력",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222),
            modifier = Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.Start)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("이름", color = placeholderColor) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
             shape = textFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (name.isNotBlank()) activeBorderColor else borderColor,
                unfocusedBorderColor = if (name.isNotBlank()) activeBorderColor else borderColor
            )
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("전화번호", color = placeholderColor) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = textFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (phone.isNotBlank()) activeBorderColor else borderColor,
                unfocusedBorderColor = if (phone.isNotBlank()) activeBorderColor else borderColor
            )
        )

        // 생년월일: 텍스트필드 + 투명 오버레이 박스(탭 가로채기)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = birth,
                onValueChange = { /* readOnly */ },
                placeholder = { Text("생년월일", color = placeholderColor) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true, // 키보드 방지
                shape = textFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (birth.isNotBlank()) activeBorderColor else borderColor,
                    unfocusedBorderColor = if (birth.isNotBlank()) activeBorderColor else borderColor
                )
            )

            // 👇 이 레이어가 모든 탭을 받아서 달력 띄움
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                birth = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            )
        }

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            placeholder = { Text("지역", color = placeholderColor) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true,
            shape = textFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (region.isNotBlank()) activeBorderColor else borderColor,
                unfocusedBorderColor = if (region.isNotBlank()) activeBorderColor else borderColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppFilledButton(
            onClick = {
                viewModel.saveProfile(
                    name = name,
                    phone = phone,
                    birth = birth,
                    region = region
                ) { onComplete() }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            text = "입력 완료",
            enabled = isButtonEnabled
        )
    }
}
