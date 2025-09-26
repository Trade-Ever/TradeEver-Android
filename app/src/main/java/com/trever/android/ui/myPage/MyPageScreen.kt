package com.trever.android.ui.myPage

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.trever.android.R
import com.trever.android.data.remote.UserInfo
import com.trever.android.ui.myPage.components.ProfileEditSheetContent
import com.trever.android.ui.myPage.components.TransactionSheetContent
import com.trever.android.ui.myPage.components.formatAmountToManwon
import com.trever.android.ui.navigation.ROUTE_LOGIN
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PRIVACY_POLICY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_PURCHASE_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_SALES_HISTORY
import com.trever.android.ui.navigation.ROUTE_MYPAGE_TERMS
import com.trever.android.ui.theme.AppTheme
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.Grey_100
import kotlinx.coroutines.flow.collectLatest
import com.trever.android.ui.theme.backgroundColor
import com.trever.android.ui.theme.cardBackgroundColor
import com.trever.android.ui.theme.textPrimaryColor
import com.trever.android.ui.theme.textSecondaryColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavController,
    viewModel: MyPageViewModel = koinViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showProfileBottomSheet by remember { mutableStateOf(false) }

    val chargeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showChargeBottomSheet by remember { mutableStateOf(false) }

    val withdrawSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWithdrawBottomSheet by remember { mutableStateOf(false) }

    val logoutState by viewModel.logoutProcessState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigateToLogin.collectLatest {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo("main") {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    if (logoutState == LogoutProcessState.CompletedShowDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onLogoutDialogConfirmed()
            },
            title = {
                Text(
                    text = "로그아웃",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "로그아웃 되었습니다!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onLogoutDialogConfirmed() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("확인")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (showProfileBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileBottomSheet = false },
            sheetState = profileSheetState,
            containerColor = MaterialTheme.colorScheme.backgroundColor
        ) {
            ProfileEditSheetContent(
                initialName = userProfile?.name ?: "",
                initialEmail = userProfile?.email ?: "",
                initialAddress = userProfile?.locationCity ?: "",
                initialBirthday = userProfile?.birthDate ?: "",
                initialProfileImageUri = userProfile?.profileImageUrl?.let { Uri.parse(it) },
                onSaveClicked = { name, email, address, birthday, imageUri -> 
                    val userInfo = UserInfo(
                        name = name,
                        phone = email.ifEmpty { null }, 
                        email = email.ifEmpty {null}, 
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

    if (showChargeBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChargeBottomSheet = false },
            sheetState = chargeSheetState,
            containerColor = MaterialTheme.colorScheme.backgroundColor
        ) {
            TransactionSheetContent(
                title = "얼마나 충전할까요?",
                bankName = "내 은행",
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

    if (showWithdrawBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWithdrawBottomSheet = false },
            sheetState = withdrawSheetState,
            containerColor = MaterialTheme.colorScheme.backgroundColor
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.backgroundColor)
    ) {
        MyPageTopAppBar(navController = navController)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()

                .padding(horizontal = 16.dp)
                .padding(top = 0.dp, bottom = 0.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = 0.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(0.dp)) }

            item {
                ProfileSection(
                    nickname = userProfile?.name ?: "닉네임",
                    email = userProfile?.email ?: "",
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
                        MyPageActionItem("최근 본 차") { navController.navigate("myPage/recentlyViewed/0") },
                        MyPageActionItem("찜한 차") { navController.navigate("myPage/recentlyViewed/1") }
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
fun MyPageTopAppBar(navController: NavController) { 
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_trever_logo), 
            contentDescription = "Trever 로고",
            modifier = Modifier
                .size(120.dp)  
                .clickable { navController.navigate("main") } 
        )
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween 
        ) {
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
                    color = MaterialTheme.colorScheme.textSecondaryColor
                )
            }

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
        shape = RoundedCornerShape(32.dp),
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
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardBackgroundColor),
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
    }
}
