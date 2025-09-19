package com.trever.android.ui.myPage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.R
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
import com.trever.android.ui.theme.primaryLight

// 마이페이지 전체 화면 프리뷰에서 확인할 수 있도록 프리뷰를 제일 위에 올려놨습니다.
@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    AppTheme {
        MyPageScreen()
    }
}

@Composable
fun MyPageScreen() {
    // 앱 바 영역이 있기 때문에 전체 영역은 일단 컬럼으로 감싼다.
    // - 앱바
    // - 스크롤뷰
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White) // 일단은 테마 적용 안하고 흰색 배경으로
    ) {
        /// 앱바 영역
        MyPageAppBar()

        /// 스크롤뷰 영역
        Column(
            modifier = Modifier
                .fillMaxWidth() // 컬럼 영역 전체 세로 폭으로 확장. 컨텐트 없는 부분도 제스쳐 인식되서 스크롤 되도록
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            /// 상단 프로필 카드
            ProfileCard()

            Spacer(Modifier.height(25.dp))

            /// 내 계좌 카드
            AccountCard()

            Spacer(Modifier.height(25.dp))

            /// 최근 본 차
            MyRecentCars()

            Spacer(Modifier.height(25.dp))

            /// 찜한 차
            MyRecentCars()

            Spacer(Modifier.height(25.dp))
        }
    }
}

// 피그마 마이페이지에 있는 앱바가 다른 곳에는 일단 적용이 안 되어 있어서 이름을 MyPageAppBar로 지었습니다.
// 나중에 공통 영역에서 앱바로 쓸 필요가 생기면 이름만 수정해서 사용할 수 있도록 컴포저블로 빼놨습니다.
@Composable
fun MyPageAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.trever_logo),
            contentDescription = "마이페이지 앱바 트레버 로고",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(52.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.gear_setting),
            contentDescription = "셋팅 기어 아이콘",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(end = 14.dp)
                .size(30.dp)
        )
    }
}

@Composable
fun ProfileCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = primaryLight,
                shape = RoundedCornerShape(16.dp)  // 모서리 반경
            )
            .padding(horizontal = 16.dp)
            .padding(vertical = 29.dp)
    ) {
        // 상단 Row: 프로필 이미지 + 이름/이메일 + 알림 아이콘
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 프로필 이미지
            // 기본 아이콘 사용 (Material Icons)
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 이름/이메일
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("닉네임", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("nick@gmail.com", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            // 알림 아이콘
            Icon(
                painter = painterResource(R.drawable.ic_notification), // 벨 아이콘
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 하단 4개의 통계 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(icon = R.drawable.ic_patients, number = "120+", label = "Patients")
            StatItem(icon = R.drawable.ic_exp, number = "7+", label = "Years Exp")
            StatItem(icon = R.drawable.ic_rating, number = "4.9", label = "Rating")
            StatItem(icon = R.drawable.ic_reviews, number = "100+", label = "Reviews")
        }
    }
}

// 프로필 카드에 들어가는 4개의 통계 버튼
@Composable
fun StatItem(icon: Int, number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(number, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

// 내 계좌 카드뷰
@Composable
fun AccountCard(
    balance: String = "10,000원",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF6C4EFF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 좌측 계좌 정보
        Text(
            text = "내 계좌",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // 우측 잔액 + 버튼
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = balance,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "충전",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // 충전 클릭 시 처리
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "출금",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // 출금 클릭 시 처리
                }
            )
        }
    }
}

// 임시 Car 데이터 클래스
data class Car(
    val name: String,
    val type: String,
    val price: String,
    val year: String,
    val km: String
)

// 최근 본 차
@Composable
fun MyRecentCars() {
    val cars = listOf(
        Car("GV80", "SUV", "2,300만원", "2024년", "15,000km"),
        Car("GV80", "SUV", "2,300만원", "2024년", "15,000km"),
        Car("GV80", "SUV", "2,300만원", "2024년", "15,000km")
    )

    Column {
        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("최근 본 차")
            Image(
                painter = painterResource(id = R.drawable.arrow_right_round),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cars) { car ->
                MyPageCarCard(car)
            }
        }
    }
}


@Composable
fun MyPageCarCard(car: Car) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent, // 배경을 투명하게 설정
            contentColor = MaterialTheme.colorScheme.onSurface // 콘텐츠 색상 유지
        ),
        border = BorderStroke(1.dp, Grey_100) // 테두리 추가 (1.dp 두께, 회색)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = car.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = car.type, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            // 이미지 더미
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Car Image")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = car.price, fontWeight = FontWeight.Bold)
            Text(text = car.year, color = Color.Gray, fontSize = 12.sp)
            Text(text = car.km, color = Color.Gray, fontSize = 12.sp)
        }
    }
}





















