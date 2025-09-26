package com.trever.android

import android.os.Bundle
// import android.util.Log // Log 임포트는 현재 필요 없어 보입니다.
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// 아래 Composable 관련 임포트는 Greeting 함수용으로 남겨두거나, Greeting 함수를 사용하지 않는다면 제거 가능
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.Spacer
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Button
// import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text // Greeting에서 사용
import androidx.compose.runtime.* // Greeting에서 사용 (Composable 어노테이션 등)
// import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // Greeting에서 사용
// import androidx.compose.ui.tooling.preview.Preview // Greeting에서 사용
// import androidx.compose.ui.unit.dp
// import androidx.lifecycle.viewmodel.compose.viewModel // 직접 ViewModelProvider를 사용하므로 이 임포트는 불필요
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider // ViewModelProvider 임포트 추가
import androidx.navigation.compose.rememberNavController
import com.trever.android.data.network.ApiClient
import com.trever.android.ui.navigation.TreverApp
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel // SellCarViewModel 임포트 추가
import com.trever.android.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sellCarViewModel: SellCarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()


        setContent {
            val navController = rememberNavController() // NavHostController 생성
            ApiClient.init(applicationContext, navController) // navController 전달

            // Koin으로 ViewModel을 가져옴
            val sellCarViewModel: SellCarViewModel = koinViewModel()

            AppTheme(dynamicColor = false) {
                TreverApp(
                    sellCarViewModel = sellCarViewModel,
                )
            }
        }
    }
}

