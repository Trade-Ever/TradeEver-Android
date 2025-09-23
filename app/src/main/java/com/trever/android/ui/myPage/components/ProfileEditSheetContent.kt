package com.trever.android.ui.myPage.components

import android.net.Uri // android.net.Uri 임포트 확인
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext // 현재 사용되지 않음
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.trever.android.R // Placeholder 이미지용

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheetContent(
    initialName: String = "채은정",
    initialPhoneNumber: String = "010-1234-5678",
    initialAddress: String = "",
    initialBirthday: String = "",
    initialProfileImageUri: Uri? = null,
    onSaveClicked: (name: String, phone: String, address: String, birthday: String, imageUri: Uri?) -> Unit,
) {
    var profileImageUri by remember { mutableStateOf(initialProfileImageUri) }
    var name by remember { mutableStateOf(initialName) }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var address by remember { mutableStateOf(initialAddress) }
    var birthday by remember { mutableStateOf(initialBirthday) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> profileImageUri = uri }
    )

    // 스크롤 상태 추가
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding() // ✅ 키보드 올라올 때 자동 패딩
            .verticalScroll(scrollState) // ✅ 스크롤 가능
            .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "프로필 수정",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.Start)
        )

        Image(
            painter = if (profileImageUri != null) {
                rememberAsyncImagePainter(model = profileImageUri)
            } else {
                painterResource(id = R.drawable.profile_placeholder)
            },
            contentDescription = "프로필 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileTextField(label = "이름", value = name, onValueChange = { name = it })
        ProfileTextField(label = "전화번호", value = phoneNumber, onValueChange = { phoneNumber = it })
        ProfileTextField(label = "주소", value = address, onValueChange = { address = it })
        ProfileTextField(label = "생일", value = birthday, onValueChange = { birthday = it }, placeholder = "YYYYMMDD")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSaveClicked(name, phoneNumber, address, birthday, profileImageUri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text("저장", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder != null) Text(placeholder) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            singleLine = true
        )
    }
}
