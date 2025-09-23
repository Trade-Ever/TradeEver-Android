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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.trever.android.R
import com.trever.android.domain.model.AuctionCar
import com.trever.android.ui.components.ListingItem
import com.trever.android.ui.navigation.ROUTE_SELL_FLOW
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Red_1

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
    sellCarViewModel: SellCarViewModel
) {
    val registeredCars by sellCarViewModel.registeredCars.collectAsState()

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. 상단 UI: 차량 이미지 및 등록 버튼
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp), // 이미지 컨테이너 높이
                    contentAlignment = Alignment.Center
                ) {
                    // 배경 차량 이미지
                    Image(
                        painter = painterResource(id = R.drawable.purple_car_22),
                        contentDescription = "차량 등록 배경",
                        modifier = Modifier
                            .matchParentSize(), // 부모 Box 크기에 이미지를 맞춤
                        // Crop -> Fit 으로 변경하여 이미지 잘림 없이 전체가 보이도록 수정
                        contentScale = ContentScale.Fit
                    )

                    // 번호판 모양 버튼
                    Button(
                        onClick = { parentNavController.navigate(ROUTE_SELL_FLOW) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            // offset 값을 조절해 버튼을 번호판 위치로 이동
                            .offset(y = (-50).dp),
                        shape = RoundedCornerShape(4.dp), // 번호판 모양
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "차량 등록하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 2. "내가 등록한 차량" 타이틀 또는 목록이 없을 때의 메시지
            item {
                if (registeredCars.isEmpty()) {
                    Text(
                        text = "아직 등록된 차량이 없어요.\n지금 바로 내 차 정보를 등록해보세요!",
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                    )
                }
            }

            // 3. 등록된 차량 목록
            if (registeredCars.isNotEmpty()) {
                items(registeredCars, key = { it.id }) { car ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        val isAuction = car.transactionType == "경매"
                        ListingItem(
                            car = car,
                            onClick = { /* TODO: 등록된 차량 상세 화면으로 이동 */ },
                            onToggleLike = { /* TODO: 찜하기 로직 */ },
                            // transactionType에 따라 경매/일반 매물 UI 분기
                            showBadge = isAuction,
                            showAuctionMeta = isAuction,
                            // 일반 매물일 경우 "판매 가격"으로 표시 (예시)
                            priceLabel = if (isAuction) "최고 입찰가" else "판매 가격"
                        )
                    }
                }
            }

            // 바텀 네비게이션과 겹치지 않도록 충분한 하단 공간 확보
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}