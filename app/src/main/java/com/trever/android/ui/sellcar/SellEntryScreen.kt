package com.trever.android.ui.sellcar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.trever.android.R
import com.trever.android.domain.model.AuctionCar // AuctionCar 모델 임포트
import com.trever.android.ui.components.ListingItem // ListingItem 임포트
import com.trever.android.ui.navigation.ROUTE_SELL_FLOW
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
//import com.trever.android.ui.theme.Grey_5
import com.trever.android.ui.theme.Red_1

// ListingItem 내부에서 호출될 수 있는 AuctionBadge의 임시 더미 구현
@Composable
fun AuctionBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Red_1.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text("경매", color = Red_1, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellEntryScreen(
    parentNavController: NavHostController,
    sellCarViewModel: SellCarViewModel // ViewModel을 파라미터로 받음
) {
    val registeredCars by sellCarViewModel.registeredCars.collectAsState()

    Scaffold(
//        containerColor = Color.Gray,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold로부터 받은 padding 적용
        ) {
            // 1. 상단 배너 및 차량 등록 버튼
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // 이미지 높이 조절 가능
                        .background(Color.DarkGray), // Placeholder background
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sell_entry_banner_placeholder), // 실제 배너 이미지 리소스로 교체
                        contentDescription = "차량 등록 배너",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Button(
                        onClick = {
                            // 차량 등록 플로우 시작 전 ViewModel 상태 초기화 (필요한 경우)
                            // sellCarViewModel.resetUiStateForNewRegistration()
                            parentNavController.navigate(ROUTE_SELL_FLOW)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.9f),
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("차량 등록하기 →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. "내가 등록한 차량" 타이틀 또는 목록이 없을 때의 메시지
            item {
                if (registeredCars.isEmpty()) {
                    Text(
                        text = "아직 등록된 차량이 없어요.\n지금 바로 차량을 등록해보세요!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 48.dp)
                    )
                } else {
                    Text(
                        text = "내가 등록한 차량",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                    )
                }
            }

            // 3. 등록된 차량 목록
            if (registeredCars.isNotEmpty()) {
                items(registeredCars) { car ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ListingItem(
                            car = car,
                            onClick = { /* TODO: 등록된 차량 상세 화면으로 이동 */ },
                            onToggleLike = { /* TODO: 찜하기 로직 */ },
                            // ListingItem의 showBadge, showAuctionMeta 등 파라미터는
                            // ListingItem의 실제 정의에 맞게 설정해야 합니다.
                            showBadge = car.transactionType == "경매", // 예시 조건
                            showAuctionMeta = car.transactionType == "경매" // 예시 조건
                        )
                    }
                }
            }

            // 바텀 네비게이션과 겹치지 않도록 충분한 하단 공간 확보 (선택 사항)
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SellEntryScreenPreview() {
//    AppTheme {
//        val dummyNavController = NavHostController(LocalContext.current)
//        // Preview에서는 ViewModel 인스턴스를 직접 생성하여 전달
//        val previewViewModel = SellCarViewModel()
//        // 만약 Preview에서 더미 데이터를 보고 싶다면, ViewModel에 해당 데이터를 추가하는 로직 필요
//        // 예: previewViewModel.addDummyCarsForPreview()
//
//        SellEntryScreen(
//            parentNavController = dummyNavController,
//            sellCarViewModel = previewViewModel
//        )
//    }
//}
