package com.trever.android.ui.sellcar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellCarVisualsScreen(
    sellCarViewModel: SellCarViewModel,
    onSystemBack: () -> Unit, // 시스템 뒤로가기 (ArrowBack 아이콘용)
    onStepBack: () -> Unit,   // 단계별 이전 (하단 "이전" 버튼용)
    onNextClicked: () -> Unit
) {
    val uiState by sellCarViewModel.uiState.collectAsState()
    var color by remember { mutableStateOf(uiState.color) }

    // 이미지 선택 요청을 명시적으로 정의
    val pickImagesRequest = remember {
        PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
        onResult = { uris ->
            // 사용자가 이미지를 선택한 경우에만 상태 업데이트
            if (uris.isNotEmpty()) {
                sellCarViewModel.addImageUris(uris)
            }
        }
    )
    val purpleColor = Color(0xFF6A11CB)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { }, // 제목 추가
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomProgressBar(totalSteps = 7, currentStep = 4)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("실물 이미지를 업로드 해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // 이미지 업로드 영역
                ImageUploadBox {
                    imagePickerLauncher.launch(pickImagesRequest)
                }

                // 선택된 이미지 리스트
                if (uiState.imageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.imageUris) { uri ->
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected Image",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { sellCarViewModel.removeImageUri(uri) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Remove Image", tint = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }

                // 색상 입력 (애니메이션)
                AnimatedVisibility(
                    visible = uiState.imageUris.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { -it / 2 } + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("색상을 입력해주세요", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: 흰색") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purpleColor,
                                unfocusedBorderColor = if (color.isNotEmpty()) purpleColor else Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // 버튼 사이 간격
            ) {
                // 이전 버튼
                OutlinedButton(
                    onClick = onStepBack, // 파라미터로 받은 onNavigateBack 사용
                    modifier = Modifier.weight(1f).height(56.dp), // 높이 추가
                    shape = RoundedCornerShape(8.dp), // 기존 "다음" 버튼과 동일한 모양
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black // 텍스트 색상
                    ),
                    border = BorderStroke(1.dp, Color.LightGray), // 테두리
                    contentPadding = PaddingValues(vertical = 16.dp) // 패딩 일관성 유지
                ) {
                    Text(text = "이전", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // 다음 버튼
                Button(
                    onClick = {
                        sellCarViewModel.updateColor(color)
                        onNextClicked()
                    },
                    modifier = Modifier.weight(1f).height(56.dp), // 기존 modifier에서 fillMaxWidth() 제거, weight 사용
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    enabled = uiState.imageUris.isNotEmpty() && color.isNotBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp) // 패딩 일관성 유지
                ) {
                    Text("다음", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) // fontWeight 추가
                }
            }
        }
    }
}

@Composable
fun ImageUploadBox(onClick: () -> Unit) {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
    val purpleColor = Color(0xFF6A11CB)
    val lightPurpleColor = Color(0xFF9F72FF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = purpleColor,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Upload Icon",
                tint = purpleColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "사진 선택(최대 5장)",
                color = purpleColor,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightPurpleColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text("이미지 선택", fontSize = 16.sp)
            }
        }
    }
}
//
//@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=480")
//@Composable
//fun SellCarVisualsScreenPreview() {
//    MaterialTheme {
//        val previewViewModel = SellCarViewModel()
//        previewViewModel.updateCurrentStep(4)
//        SellCarVisualsScreen(
//            sellCarViewModel = previewViewModel,
//            onNavigateBack = {},
//            onNextClicked = {}
//        )
//    }
//}
