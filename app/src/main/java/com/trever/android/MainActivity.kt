package com.trever.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// SellCarInfoCheckScreen은 주석 처리 또는 실제 파일이 있다면 그대로 둡니다.
// import com.trever.android.ui.sellcar.SellCarInfoCheckScreen
import com.trever.android.ui.sellcar.SellCarMileageAndTypeScreen
import com.trever.android.ui.sellcar.SellCarModelPromptScreen
import com.trever.android.ui.sellcar.SellCarPlateNumberScreen
import com.trever.android.ui.sellcar.SellCarYearScreen
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import androidx.core.view.WindowCompat
import com.trever.android.ui.navigation.TreverApp
import com.trever.android.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { AppTheme(dynamicColor = false) {
            TreverApp()
        } }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
