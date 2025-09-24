package com.trever.android.ui.sellcar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.textPrimaryColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SelectManufacturerScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit,
    onManufacturerSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val manufacturerData = uiState.manufacturerDataMap

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("제조사 선택", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.textPrimaryColor) },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor)
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor // 전체 배경 흰색으로 설정
    ) { paddingValues ->
        if (uiState.isLoadingManufacturers) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (manufacturerData.isEmpty() && !uiState.isLoadingManufacturers) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "제조사 정보를 불러올 수 없습니다.\\n네트워크 연결을 확인 후 다시 시도해주세요.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.textPrimaryColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                manufacturerData.keys.sorted().forEach { category ->
                    stickyHeader {
                        Text(
                            text = category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.textPrimaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.backgroundColor) // 헤더 배경도 다크모드 대응
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                    val manufacturers = manufacturerData[category] ?: emptyList()
                    items(manufacturers) { manufacturerName ->
                        ManufacturerRow(manufacturerName = manufacturerName) {
                            viewModel.updateSelectedManufacturer(category, manufacturerName)
                            onManufacturerSelected()
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.G_100,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManufacturerRow(manufacturerName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp), // 패딩 조정
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: 각 제조사에 맞는 실제 로고 이미지(Painter)로 교체해야 합니다.
        Image(
            imageVector = Icons.Default.Image, // 임시 로고 아이콘
            contentDescription = "$manufacturerName 로고",
            modifier = Modifier.size(36.dp), // 로고 사이즈
            colorFilter = ColorFilter.tint(Color.LightGray) // 임시 아이콘이므로 약간 옅게 표시
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = manufacturerName,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.textPrimaryColor
        )
    }
}