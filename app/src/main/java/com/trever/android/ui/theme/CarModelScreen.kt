package com.trever.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CarModelScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 진행 상태 바: 이제 두 번째 단계
        Spacer(modifier = Modifier.height(16.dp))
        ProgressBar(steps = 5, currentStep = 2)

        Spacer(modifier = Modifier.weight(1f))

        // 안내 문구
        Text(
            text = "차량 모델을 입력해주세요",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 클릭 가능한 입력 필드 (Text Field가 아님)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // TextField와 비슷한 높이
                .background(
                    color = Color(0xFFF0F0F0), // 연한 회색 배경
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    // TODO: 모델 선택을 위한 다이얼로그나 바텀 시트 열기
                },
            contentAlignment = Alignment.CenterStart // 플레이스홀더 정렬
        ) {
            Text(
                text = "모델을 선택해주세요",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 16.dp) // 왼쪽 패딩
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        // '다음' 버튼은 필요 없음
    }
}

// 이전 파일의 ProgressBar 컴포넌트 재사용
@Composable
fun ProgressBar(steps: Int, currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(steps) { index ->
            val color = if (index + 1 == currentStep) Color(0xFF6200EE) else Color.LightGray
            Box(
                modifier = Modifier
                    .width(if (index + 1 == currentStep) 24.dp else 8.dp)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarModelScreenPreview() {
    CarModelScreen()
}