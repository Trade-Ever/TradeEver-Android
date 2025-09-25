package com.trever.android.ui.myPage.components

import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.trever.android.R
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheetContent(
    initialName: String = "채은정",
    initialEmail: String = "example@email.com",
    initialAddress: String = "",
    initialBirthday: String = "",
    initialProfileImageUri: Uri? = null,
    onSaveClicked: (name: String, email: String, address: String, birthday: String, imageUri: Uri?) -> Unit,
) {
    var profileImageUri by remember { mutableStateOf(initialProfileImageUri) }
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var address by remember { mutableStateOf(initialAddress) }
    var birthday by remember { mutableStateOf(initialBirthday) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> profileImageUri = uri }
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "프로필 수정",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.textPrimaryColor,
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
        ProfileTextField(label = "이메일", value = email, onValueChange = { email = it })
        ProfileTextField(label = "주소", value = address, onValueChange = { address = it })
        ProfileTextField(label = "생일", value = birthday, onValueChange = { birthday = it }, placeholder = "YYYYMMDD")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSaveClicked(name, email, address, birthday, profileImageUri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
            color = MaterialTheme.colorScheme.textPrimaryColor,
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
