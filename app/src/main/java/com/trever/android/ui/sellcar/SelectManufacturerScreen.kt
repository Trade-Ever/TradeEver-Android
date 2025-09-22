package com.trever.android.ui.sellcar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// NavController import 제거
import com.trever.android.R
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
//import com.trever.android.ui.theme.Grey_500

// 더미 데이터 및 플레이스홀더 로고 정의는 이전과 동일하게 유지
data class ManufacturerItem(val logoResId: Int, val name: String, val count: String, val isDomestic: Boolean)
val placeholderManufacturerLogo = R.drawable.hyundai_logo
val dummyManufacturers = listOf(
    ManufacturerItem(placeholderManufacturerLogo, "현대", "44,661", true),
    ManufacturerItem(placeholderManufacturerLogo, "제네시스", "11,696", true),
    ManufacturerItem(placeholderManufacturerLogo, "기아", "11,696", true),
    ManufacturerItem(placeholderManufacturerLogo, "쉐보레(GM대우)", "11,696", true),
    ManufacturerItem(placeholderManufacturerLogo, "르노코리아(삼성)", "11,696", true),
    ManufacturerItem(placeholderManufacturerLogo, "BMW", "11,696", false),
    ManufacturerItem(placeholderManufacturerLogo, "벤츠", "11,696", false),
    ManufacturerItem(placeholderManufacturerLogo, "아우디", "11,696", false),
    ManufacturerItem(placeholderManufacturerLogo, "포르쉐", "11,696", false),
    ManufacturerItem(placeholderManufacturerLogo, "미니", "11,696", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectManufacturerScreen(
    viewModel: SellCarViewModel,
    onSystemBack: () -> Unit, // 시스템 뒤로가기 콜백
    onManufacturerSelected: () -> Unit, // 제조사 선택 완료 콜백
    // onStepBack: (() -> Unit)? = null // 화면 내 이전 버튼용 (필요시 추가)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("제조사", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) { // onSystemBack 콜백 사용
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "국산차",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            items(dummyManufacturers.filter { it.isDomestic }) { manufacturer ->
                ManufacturerRow(manufacturer = manufacturer) {
                    viewModel.updateSelectedManufacturer(manufacturer.name)
                    onManufacturerSelected() // 다음 화면으로 전환 콜백 호출
                }
                HorizontalDivider(color = Grey_100)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "수입차",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            items(dummyManufacturers.filter { !it.isDomestic }) { manufacturer ->
                ManufacturerRow(manufacturer = manufacturer) {
                    viewModel.updateSelectedManufacturer(manufacturer.name)
                    onManufacturerSelected() // 다음 화면으로 전환 콜백 호출
                }
                HorizontalDivider(color = Grey_100)
            }
        }
    }
}

@Composable
fun ManufacturerRow(manufacturer: ManufacturerItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = manufacturer.logoResId),
            contentDescription = "${manufacturer.name} 로고",
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = manufacturer.name, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = manufacturer.count, fontSize = 14.sp, color = Color.Black) // Grey_500 사용 확인
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SelectManufacturerScreenPreview() {
//    AppTheme {
//        SelectManufacturerScreen(
//            viewModel = SellCarViewModel(), // Preview용 ViewModel
//            onSystemBack = {},
//            onManufacturerSelected = {}
//        )
//    }
//}
