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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.trever.android.R
import com.trever.android.ui.myPage.components.ProfileEditSheetContent
import com.trever.android.ui.myPage.components.TransactionSheetContent
import com.trever.android.ui.myPage.components.formatAmountToManwon
import com.trever.android.ui.navigation.* 
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.Grey_100
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * 데이터를 관리하고 상태를 수집하는 'Stateful' Composable 입니다.
 */
@Composable
fun MyPageScreen(
    navController: NavController,
    viewModel: MyPageViewModel = koinViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val accountInfo by viewModel.accountInfo.collectAsState()

    MyPageContent(
        navController = navController,
        userProfile = userProfile,
        accountInfo = accountInfo,
        onUpdateUserProfile = viewModel::updateUserProfile,
        onCharge = viewModel::charge,
        onWithdraw = viewModel::withdraw,
        onLogoutClicked = viewModel::onLogoutClicked
    )
}

/**
 * 데이터를 받아서 UI를 그리기만 하는 'Stateless' Composable 입니다.
 * 이 함수는 ViewModel의 존재를 알지 못하므로 Preview가 가능합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPageContent(
    navController: NavController,
    userProfile: UserProfile,
    accountInfo: AccountInfo,
    onUpdateUserProfile: (String, String?, String?, String?, String?) -> Unit,
    onCharge: (Long) -> Unit,
    onWithdraw: (Long) -> Unit,
    onLogoutClicked: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 바텀시트 상태 관리
    var showProfileBottomSheet by remember { mutableStateOf(false) }
    val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showChargeBottomSheet by remember { mutableStateOf(false) }
    val chargeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWithdrawBottomSheet by remember { mutableStateOf(false) }
    val withdrawSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- 바텀시트 정의 --- //
    if (showProfileBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showProfileBottomSheet = false }, sheetState = profileSheetState, containerColor = Color.White) {
            ProfileEditSheetContent(
                initialName = userProfile.nickname,
                initialPhoneNumber = userProfile.phoneNumber ?: "",
                initialAddress = userProfile.address ?: "",
                initialBirthday = userProfile.birthday ?: "",
                initialProfileImageUri = userProfile.profileImageUrl?.let { Uri.parse(it) },
                onSaveClicked = { name, phone, address, birthday, imageUri ->
                    onUpdateUserProfile(name, phone.ifEmpty { null }, address.ifEmpty { null }, birthday.ifEmpty { null }, imageUri?.toString())
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

    if (showChargeBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showChargeBottomSheet = false }, sheetState = chargeSheetState, containerColor = Color.White) {
            TransactionSheetContent(
                title = "얼마나 충전할까요?",
                bankName = accountInfo.bankName ?: "알 수 없는 은행",
                accountNumber = accountInfo.accountNumber,
                bankLogoResId = R.drawable.ic_bank_placeholder,
                preSetAmounts = listOf(10000L, 50000L, 100000L, 500000L, 1000000L),
                actionButtonText = "충전하기",
                onActionClick = { amount ->
                    onCharge(amount)
                    scope.launch { chargeSheetState.hide() }.invokeOnCompletion {
                        if (!chargeSheetState.isVisible) showChargeBottomSheet = false
                    }
                    Toast.makeText(context, "${formatAmountToManwon(amount)} 충전 완료", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showWithdrawBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showWithdrawBottomSheet = false }, sheetState = withdrawSheetState, containerColor = Color.White) {
            TransactionSheetContent(
                title = "얼마나 출금할까요?",
                bankName = accountInfo.bankName ?: "알 수 없는 은행",
                accountNumber = accountInfo.accountNumber,
                bankLogoResId = R.drawable.ic_bank_placeholder,
                preSetAmounts = listOf(10000L, 50000L, 100000L, 500000L, 1000000L),
                actionButtonText = "출금하기",
                onActionClick = { amount ->
                    onWithdraw(amount)
                    scope.launch { withdrawSheetState.hide() }.invokeOnCompletion {
                        if (!withdrawSheetState.isVisible) showWithdrawBottomSheet = false
                    }
                    Toast.makeText(context, "${formatAmountToManwon(amount)} 출금 요청됨", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // --- 본 화면 UI --- //
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

            item { // 프로필 섹션
                ProfileSection(
                    nickname = userProfile.nickname,
                    email = userProfile.email,
                    profileImageUrl = userProfile.profileImageUrl,
                    onProfileClick = { showProfileBottomSheet = true }
                )
            }

            item { // 예치금 섹션
                AccountSection(
                    accountTitle = accountInfo.accountName ?: "내 계좌",
                    balance = accountInfo.balance,
                    onChargeClick = { showChargeBottomSheet = true },
                    onWithdrawClick = { showWithdrawBottomSheet = true }
                )
            }

            item { // 나의 활동 메뉴
                MyPageMenuGroup(
                    title = "나의 활동",
                    items = listOf(
                        MyPageActionItem("최근 본 차") { navController.navigate(ROUTE_MYPAGE_RECENTLY_VIEWED) },
                        MyPageActionItem("찜한 차") { navController.navigate(ROUTE_MYPAGE_LIKED_CARS) }
                    )
                )
            }

            item { // 거래 내역 메뉴
                MyPageMenuGroup(
                    title = "거래 내역",
                    items = listOf(
                        MyPageActionItem("판매 내역") { navController.navigate(ROUTE_MYPAGE_SALES_HISTORY) },
                        MyPageActionItem("구매 내역") { navController.navigate(ROUTE_MYPAGE_PURCHASE_HISTORY) }
                    )
                )
            }

            item { // 고객지원 메뉴
                MyPageMenuGroup(
                    title = "고객지원",
                    items = listOf(
                        MyPageActionItem("약관 및 정책") { navController.navigate(ROUTE_MYPAGE_TERMS) },
                        MyPageActionItem("개인정보 처리방침") { navController.navigate(ROUTE_MYPAGE_PRIVACY_POLICY) },
                        MyPageActionItem("로그아웃", true, onLogoutClicked)
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- 이하 UI 구성 요소들 (변경 없음) --- //

@Composable
fun MyPageTopAppBar() {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F4F4)).padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("마이페이지", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileSection(nickname: String, email: String, profileImageUrl: String?, onProfileClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onProfileClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (profileImageUrl != null) rememberAsyncImagePainter(model = profileImageUrl) else painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "프로필 사진",
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Grey_100),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nickname, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AccountSection(accountTitle: String, balance: Long, onChargeClick: () -> Unit, onWithdrawClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5222D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = accountTitle, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(balance)}원",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )
            TextButton(onClick = onChargeClick, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("충전", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(text = "|", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal=2.dp))
            TextButton(onClick = onWithdrawClick, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
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
                    MyPageMenuListItem(title = item.title, isLogout = item.isLogout, onClick = item.action)
                    if (index < items.size - 1) {
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MyPageMenuListItem(title: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!isLogout) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "이동", tint = Color.Gray)
        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun MyPageScreenPreview() {
//    AppTheme {
//        // 이제 Preview는 ViewModel 없이 가짜 데이터로 'Stateless' Composable을 직접 렌더링합니다.
//        MyPageContent(
//            navController = rememberNavController(),
//            userProfile = UserProfile(nickname = "홍길동", email = "test@trever.com"),
//            accountInfo = AccountInfo(balance = 1_500_000, accountName = "내 예치금"),
//            onUpdateUserProfile = { _, _, _, _, _ -> },
//            onCharge = {},
//            onWithdraw = {},
//            onLogoutClicked = {}
//        )
//    }
//}
