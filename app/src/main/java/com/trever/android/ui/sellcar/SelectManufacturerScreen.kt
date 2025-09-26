package com.trever.android.ui.sellcar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.filled.Image // painterResource 사용으로 대체
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color // 직접 사용하지 않으면 제거 가능
// import androidx.compose.ui.graphics.ColorFilter // 실제 컬러 로고 사용 시 제거 또는 주석
import androidx.compose.ui.layout.ContentScale // 추가
import androidx.compose.ui.res.painterResource // 추가
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.tooling.preview.Preview // Preview 관련 코드가 없다면 제거 가능
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.R
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
// import com.trever.android.ui.theme.AppTheme // Preview 관련 코드가 없다면 제거 가능
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.backgroundColor // 이전 스타일로 복원하기 위해 추가
import com.trever.android.ui.theme.textPrimaryColor // 이전 스타일로 복원하기 위해 추가

// 제조사 이름에 따라 Drawable 리소스 ID를 반환하는 헬퍼 함수
@Composable
@DrawableRes
// @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // 이 함수 자체에는 필요 없음
fun getManufacturerLogoResId(manufacturerName: String): Int {
    // ViewModel에서 전달받는 manufacturerName (한글 이름)을 기준으로 매핑
    return when (manufacturerName) {
        // --- 국내차 ---
        "기아" -> R.drawable.kia
        "쉐보레" -> R.drawable.chevrolet
        "쌍용" -> R.drawable.kg // (현재 KG 모빌리티)
        "제네시스" -> R.drawable.genesis
        "현대" -> R.drawable.hyundai

        // --- 수입차 ---
        "랜드로버" -> R.drawable.landrover
        "렉서스" -> R.drawable.lexus
        "로터스" -> R.drawable.lotus
        "롤스로이스" -> R.drawable.rolls_royce
        "르노" -> R.drawable.renault
        "링컨" -> R.drawable.lincoln
        "마세라티" -> R.drawable.maserati
        "마이바흐" -> R.drawable.maybach
        "마쯔다" -> R.drawable.mazda
        "맥라렌" -> R.drawable.mclaren
        "미니" -> R.drawable.mini
        "미쯔비시" -> R.drawable.mitsubishi
        "미쯔오까" -> R.drawable.mitsuoka
        "벤츠", "메르세데스-벤츠" -> R.drawable.mercedes_benz
        "벤틀리" -> R.drawable.bentley
        "볼보" -> R.drawable.volvo
        "부가티" -> R.drawable.bugatti
        "북기은상" -> R.drawable.baic_motor // baic_motor.png 또는 유사 파일 필요
        "사이언" -> R.drawable.scion
        "새턴" -> R.drawable.saturn
        "스마트" -> R.drawable.smart
        "스바루" -> R.drawable.subaru
        "스즈키" -> R.drawable.suzuki
        "시트로엥" -> R.drawable.citroen
        "아우디" -> R.drawable.audi
        "어큐라" -> R.drawable.acura
        "오펠" -> R.drawable.opel
        "인피니티" -> R.drawable.infiniti
        "재규어" -> R.drawable.jaguar
        "중한자동차" -> R.drawable.ck // ck.png 기반
        "지프" -> R.drawable.jeep
        "캐딜락" -> R.drawable.cadillac
        "크라이슬러" -> R.drawable.chrysler
        "테슬라" -> R.drawable.tesla
        "토요타", "도요타" -> R.drawable.toyota
        "페라리" -> R.drawable.ferrari
        "포드" -> R.drawable.ford
        "포르쉐" -> R.drawable.porsche
        "폭스바겐" -> R.drawable.volkswagen
        "푸조" -> R.drawable.peugeot
        "피아트" -> R.drawable.fiat
        "허머" -> R.drawable.hummer
        "혼다" -> R.drawable.honda
        "BMW" -> R.drawable.bmw // ViewModel에서 "BMW"로 오는 경우
        "GMC" -> R.drawable.gmc // ViewModel에서 "GMC"로 오는 경우
        "닛산" -> R.drawable.nissan
        "다이하쓰" -> R.drawable.daihatsu
        "닷지" -> R.drawable.dodge
        "람보르기니" -> R.drawable.lamborghini

        // --- 기타 제조사 ---
        "애스턴마틴" -> R.drawable.astonmartin
        "바이톤" -> R.drawable.baic_motor // baic_motor.png 또는 etc.png 등으로 대체 (baic_motor가 중복될 수 있음)

        else -> R.drawable.etc // ic_default_logo_placeholder.png 등의 기본 이미지 파일 필요
    }
}

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
                title = { Text("제조사 선택", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.textPrimaryColor) }, // 기존 스타일 적용
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor) // 기존 스타일 적용
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor // 기존 스타일 적용
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
                    "제조사 정보를 불러올 수 없습니다.\n네트워크 연결을 확인 후 다시 시도해주세요.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.textPrimaryColor // 기존 스타일 적용 (또는 onSurfaceVariant 등 고려)
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
                            color = MaterialTheme.colorScheme.textPrimaryColor, // 기존 스타일 적용 (또는 primary)
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.backgroundColor) // 기존 스타일 적용 (또는 surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp) // 패딩 원복 또는 유지
                        )
                    }
                    val manufacturers = manufacturerData[category] ?: emptyList()
                    items(manufacturers) { manufacturerName ->
                        ManufacturerRow(manufacturerName = manufacturerName) {
                            viewModel.updateSelectedManufacturer(category, manufacturerName)
                            onManufacturerSelected()
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.G_100, // 기존 스타일 적용 (G_100 직접 참조 또는 테마 확장)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val logoResId = getManufacturerLogoResId(manufacturerName)

        Image(
            painter = painterResource(id = logoResId),
            contentDescription = "$manufacturerName 로고",
            modifier = Modifier.size(36.dp),
            contentScale = ContentScale.Fit // 로고 비율에 맞게 조정
            // colorFilter = ColorFilter.tint(Color.LightGray) // 실제 컬러 로고 사용 시 제거
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = manufacturerName,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.textPrimaryColor // 기존 스타일 적용
        )
    }
}
