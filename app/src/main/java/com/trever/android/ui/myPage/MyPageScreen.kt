package com.trever.android.ui.myPage

import android.net.Uri
import android.widget.Toast 
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
import coil.compose.rememberAsyncImagePainter 
import com.trever.android.R
import com.trever.android.data.remote.UserInfo
import com.trever.android.ui.myPage.components.ProfileEditSheetContent 
import com.trever.android.ui.myPage.components.TransactionSheetContent // 바텀시트 임포트
import com.trever.android.ui.myPage.components.formatAmountToManwon // 금액 포맷팅 함수 임포트
import com.trever.android.ui.navigation.ROUTE_MYPAGE_LIKED_CARS
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PRIVACY_POLICY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PURCHASE_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_RECENTLY_VIEWED
import com.trever.android.ui.navigation.ROUTE_MYPAGE_SALES_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_TERMS
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
import kotlinx.coroutines.launch 
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavController,
    viewModel: MyPageViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 프로필 수정 바텀시트 상태
    val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showProfileBottomSheet by remember { mutableStateOf(false) }

    // 충전 바텀시트 상태
    val chargeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showChargeBottomSheet by remember { mutableStateOf(false) }

    // 출금 바텀시트 상태
    val withdrawSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWithdrawBottomSheet by remember { mutableStateOf(false) }

    // 프로필 수정 바텀시트
    if (showProfileBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileBottomSheet = false },
            sheetState = profileSheetState,
            containerColor = Color.White // 프로필 바텀시트 배경색도 흰색으로 통일 (선택 사항)
        ) {
            ProfileEditSheetContent(
                initialName = userProfile?.name ?: "",
                initialPhoneNumber = userProfile?.phone ?: "",
                initialAddress = userProfile?.locationCity ?: "",
                initialBirthday = userProfile?.birthDate ?: "",
                initialProfileImageUri = userProfile?.profileImageUrl?.let { Uri.parse(it) },
                onSaveClicked = { name, phone, address, birthday, imageUri ->
                    val userInfo = UserInfo(
                        name = name,
                        phone = phone.ifEmpty { null },
                        locationCity = address.ifEmpty { null },
                        birthDate = birthday.ifEmpty { null }
                    )
                    viewModel.updateProfile(userInfo, imageUri)
                    scope.launch {
                        profileSheetState.hide()
                    }.invokeOnCompletion {
                        if (!profileSheetState.isVisible) showProfileBottomSheet = false
                    }
                    Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // 충전 바텀시트
    if (showChargeBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChargeBottomSheet = false },
            sheetState = chargeSheetState,
            containerColor = Color.White // 바텀시트 컨테이너 색상을 흰색으로 설정
            // modifier = Modifier.fillMaxHeight(0.9f) // 높이 고정 제거
        ) {
            TransactionSheetContent(
                title = "얼마나 충전할까요?",
                bankName = "내 은행", // 서버에서 은행정보 아직 안받으니 임시 표시
                accountNumber = "계좌번호",
                bankLogoResId = R.drawable.ic_bank_placeholder,
                preSetAmounts = listOf(10000L, 50000L, 100000L, 500000L, 1000000L),
                actionButtonText = "충전하기",
                onActionClick = { amount ->
                    viewModel.deposit(amount)
                    scope.launch {
                        chargeSheetState.hide()
                    }.invokeOnCompletion {
                        if (!chargeSheetState.isVisible) showChargeBottomSheet = false
                    }
                    Toast.makeText(context, "${formatAmountToManwon(amount)} 충전 완료", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // 출금 바텀시트
    if (showWithdrawBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWithdrawBottomSheet = false },
            sheetState = withdrawSheetState,
            containerColor = Color.White // 바텀시트 컨테이너 색상을 흰색으로 설정
            // modifier = Modifier.fillMaxHeight(0.9f) // 높이 고정 제거
        ) {
            TransactionSheetContent(
                title = "얼마나 출금할까요?",
                bankName = "내 은행",
                accountNumber = "계좌번호",
                bankLogoResId = R.drawable.ic_bank_placeholder,
                preSetAmounts = listOf(10000L, 50000L, 100000L, 500000L, 1000000L),
                actionButtonText = "출금하기",
                onActionClick = { amount ->
                    viewModel.withdraw(amount)
                    scope.launch {
                        withdrawSheetState.hide()
                    }.invokeOnCompletion {
                        if (!withdrawSheetState.isVisible) showWithdrawBottomSheet = false
                    }
                    Toast.makeText(context, "${formatAmountToManwon(amount)} 출금 요청됨", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Scaffold(
        topBar = { MyPageTopAppBar() },
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
                    nickname = userProfile?.name ?: "닉네임",
                    email = userProfile?.phone ?: "-",
                    profileImageUrl = userProfile?.profileImageUrl,
                    onProfileClick = { showProfileBottomSheet = true }
                )
            }

            item {
                AccountSection(
                    accountTitle = "내 지갑",
                    balance = balance ?: 0L,
                    onChargeClick = { showChargeBottomSheet = true },
                    onWithdrawClick = { showWithdrawBottomSheet = true }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) 
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AccountSection(
    accountTitle: String,
    balance: Long, 
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
                text = accountTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(balance)}원",
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

/*
@Composable
fun AccountActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, 
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}
*/

data class MyPageActionItem(val title: String, val isLogout: Boolean = false, val action: () -> Unit)

@Composable
fun MyPageMenuGroup(title: String, items: List<MyPageActionItem>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            style = MaterialTheme.typography.bodyMedium, 
            color = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
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
