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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale 
import androidx.compose.ui.res.painterResource 
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.R
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.backgroundColor 
import com.trever.android.ui.theme.textPrimaryColor 

@Composable
@DrawableRes
fun getManufacturerLogoResId(manufacturerName: String): Int {
    return when (manufacturerName) {
        "기아" -> R.drawable.kia
        "쉐보레" -> R.drawable.chevrolet
        "쌍용" -> R.drawable.kg 
        "제네시스" -> R.drawable.genesis
        "현대" -> R.drawable.hyundai

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
        "북기은상" -> R.drawable.baic_motor 
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
        "중한자동차" -> R.drawable.ck 
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
        "BMW" -> R.drawable.bmw 
        "GMC" -> R.drawable.gmc 
        "닛산" -> R.drawable.nissan
        "다이하쓰" -> R.drawable.daihatsu
        "닷지" -> R.drawable.dodge
        "람보르기니" -> R.drawable.lamborghini

        "애스턴마틴" -> R.drawable.astonmartin
        "바이톤" -> R.drawable.baic_motor 

        else -> R.drawable.etc 
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
                title = { Text("제조사 선택", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.textPrimaryColor) }, 
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.backgroundColor) 
            )
        },
        containerColor = MaterialTheme.colorScheme.backgroundColor 
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
                                .background(MaterialTheme.colorScheme.backgroundColor) 
                                .padding(horizontal = 16.dp, vertical = 12.dp) 
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val logoResId = getManufacturerLogoResId(manufacturerName)

        Image(
            painter = painterResource(id = logoResId),
            contentDescription = "$manufacturerName 로고",
            modifier = Modifier.size(36.dp),
            contentScale = ContentScale.Fit 
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = manufacturerName,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.textPrimaryColor 
        )
    }
}
