package com.trever.android.ui.myPage

import android.net.Uri
import android.widget.Toast // 토스트 메시지용 임포트
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter // Coil 임포트 추가
import com.trever.android.R
import com.trever.android.ui.myPage.components.ProfileEditSheetContent // 바텀시트 컨텐츠 임포트
import com.trever.android.ui.navigation.ROUTE_MYPAGE_LIKED_CARS
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PRIVACY_POLICY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PURCHASE_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_RECENTLY_VIEWED
import com.trever.android.ui.navigation.ROUTE_MYPAGE_SALES_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_TERMS
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
import kotlinx.coroutines.launch // 코루틴 스코프용 임포트

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavController,
    viewModel: MyPageViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val accountInfo by viewModel.accountInfo.collectAsState()
    val context = LocalContext.current // 토스트 메시지용

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // 바텀시트가 완전히 확장되거나 숨겨지도록 설정
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            ProfileEditSheetContent(
                initialName = userProfile.nickname,
                initialPhoneNumber = userProfile.phoneNumber ?: "",
                initialAddress = userProfile.address ?: "",
                initialBirthday = userProfile.birthday ?: "",
                initialProfileImageUri = userProfile.profileImageUrl?.let { Uri.parse(it) },
                onSaveClicked = {
                    name, phone, address, birthday, imageUri ->
                    // ViewModel의 프로필 업데이트 함수 호출
                    viewModel.updateUserProfile(
                        newName = name,
                        newPhoneNumber = phone.ifEmpty { null }, // 빈 문자열이면 null로 전달
                        newAddress = address.ifEmpty { null },   // 빈 문자열이면 null로 전달
                        newBirthday = birthday.ifEmpty { null }, // 빈 문자열이면 null로 전달
                        newProfileImageUrl = imageUri?.toString()
                    )
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                    Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            MyPageTopAppBar()
        },
        containerColor = Color(0xFFF4F4F4)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ProfileSection(
                    nickname = userProfile.nickname,
                    email = userProfile.email,
                    profileImageUrl = userProfile.profileImageUrl,
                    onProfileClick = { 
                        showBottomSheet = true // 프로필 클릭 시 바텀시트 표시
                    }
                )
            }

            item {
                AccountSection(
                    balance = accountInfo.balance,
                    onChargeClick = { viewModel.onChargeClicked() },
                    onWithdrawClick = { viewModel.onWithdrawClicked() }
                )
            }

            item {
                MyPageMenuGroup(
                    title = "나의 활동",
                    items = listOf(
                        MyPageActionItem("최근 본 차") { navController.navigate(ROUTE_MYPAGE_RECENTLY_VIEWED) },
                        MyPageActionItem("찜한 차") { navController.navigate(ROUTE_MYPAGE_LIKED_CARS) }
                    )
                )
            }

            item {
                MyPageMenuGroup(
                    title = "거래 내역",
                    items = listOf(
                        MyPageActionItem("판매 내역") { navController.navigate(ROUTE_MYPAGE_SALES_HISTORY) },
                        MyPageActionItem("구매 내역") { navController.navigate(ROUTE_MYPAGE_PURCHASE_HISTORY) }
                    )
                )
            }

            item {
                MyPageMenuGroup(
                    title = "고객지원",
                    items = listOf(
                        MyPageActionItem("약관 및 정책") { navController.navigate(ROUTE_MYPAGE_TERMS) },
                        MyPageActionItem("개인정보 처리방침") { navController.navigate(ROUTE_MYPAGE_PRIVACY_POLICY) },
                        MyPageActionItem("로그아웃", true) { viewModel.onLogoutClicked() }
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MyPageTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("마이페이지", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileSection(
    nickname: String,
    email: String,
    profileImageUrl: String?,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (profileImageUrl != null) {
                    rememberAsyncImagePainter(model = profileImageUrl)
                } else {
                    painterResource(id = R.drawable.profile_placeholder) 
                },
                contentDescription = "프로필 사진",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Grey_100),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun AccountSection(
    balance: String,
    onChargeClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5222D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "내 계좌",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = balance,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )
            TextButton(
                onClick = onChargeClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text("충전", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "|",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal=2.dp)
            )
            TextButton(
                onClick = onWithdrawClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text("출금", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

data class MyPageActionItem(val title: String, val isLogout: Boolean = false, val action: () -> Unit)

@Composable
fun MyPageMenuGroup(title: String, items: List<MyPageActionItem>) {
    Column {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    MyPageMenuListItem(
                        title = item.title,
                        isLogout = item.isLogout,
                        onClick = item.action
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = Color(0xFFF0F0F0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyPageMenuListItem(title: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = if (isLogout) Color.Red else Color.Black,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!isLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "이동",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    AppTheme {
        val navController = NavController(LocalContext.current)
        MyPageScreen(navController = navController)
    }
}
