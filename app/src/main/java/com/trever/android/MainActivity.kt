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
import com.trever.android.ui.theme.TreverTheme

// 화면 상태를 정의하는 enum
enum class CurrentScreen {
    PlateNumber,
    InfoCheck,
    ModelPrompt,
    YearInput,
    MileageAndType
    // TODO: 이후 화면들도 추가 예정
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreverTheme {
                val sellCarViewModel: SellCarViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        CurrentScreen.PlateNumber -> {
                            SellCarPlateNumberScreen(
                                sellCarViewModel = sellCarViewModel,
                                onNavigateBack = {
                                    Log.d("MainActivity", "Back from PlateNumberScreen")
                                    finish()
                                },
                                onNextClicked = {
                                    sellCarViewModel.updateCurrentStep(2)
                                    currentScreen = CurrentScreen.InfoCheck
                                }
                            )
                        }
                        CurrentScreen.InfoCheck -> {
                            // SellCarInfoCheckScreen 호출 임시 주석 처리
                            // SellCarInfoCheckScreen(
                            //     sellCarViewModel = sellCarViewModel,
                            //     onNavigateBack = { 
                            //         sellCarViewModel.updateCurrentStep(1)
                            //         currentScreen = CurrentScreen.PlateNumber
                            //     },
                            //     onNextClicked = {
                            //         sellCarViewModel.updateCurrentStep(3)
                            //         currentScreen = CurrentScreen.ModelPrompt
                            //     }
                            // )

                            // 임시 화면 구성
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("임시 InfoCheck 화면입니다.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    sellCarViewModel.updateCurrentStep(1)
                                    currentScreen = CurrentScreen.PlateNumber
                                }) {
                                    Text("이전 화면으로 (PlateNumber)")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    sellCarViewModel.updateCurrentStep(3)
                                    currentScreen = CurrentScreen.ModelPrompt
                                }) {
                                    Text("다음 화면으로 (ModelPrompt)")
                                }
                            }
                        }
                        CurrentScreen.ModelPrompt -> {
                            SellCarModelPromptScreen(
                                sellCarViewModel = sellCarViewModel,
                                onNavigateBack = {
                                    sellCarViewModel.updateCurrentStep(2)
                                    currentScreen = CurrentScreen.InfoCheck // 임시 화면으로 돌아감
                                },
                                onPromptClicked = { 
                                    sellCarViewModel.updateSelectedModel("현대 아반떼 SN7 (더미)")
                                    sellCarViewModel.updateCurrentStep(5) 
                                    currentScreen = CurrentScreen.YearInput
                                }
                            )
                        }
                        CurrentScreen.YearInput -> {
                            SellCarYearScreen(
                                sellCarViewModel = sellCarViewModel,
                                onNavigateBack = {
                                    sellCarViewModel.updateCurrentStep(3)
                                    currentScreen = CurrentScreen.ModelPrompt
                                },
                                onNextClicked = {
                                    sellCarViewModel.updateCurrentStep(6)
                                    currentScreen = CurrentScreen.MileageAndType
                                }
                            )
                        }
                        CurrentScreen.MileageAndType -> {
                            SellCarMileageAndTypeScreen(
                                sellCarViewModel = sellCarViewModel,
                                onNavigateBack = {
                                    sellCarViewModel.updateCurrentStep(5)
                                    currentScreen = CurrentScreen.YearInput
                                },
                                onNextClicked = {
                                    sellCarViewModel.updateCurrentStep(7)
                                    Log.d("MainActivity", "Next from MileageAndType. Mileage: ${sellCarViewModel.uiState.value.mileage}, Type: ${sellCarViewModel.uiState.value.selectedCarType}")
                                    // TODO: 다음 화면 로직 (예: AccidentHistory)
                                    // currentScreen = CurrentScreen.AccidentHistory // 예시
                                }
                            )
                        }
                        // TODO: 다른 화면 케이스 추가
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TreverTheme {
        Greeting("Android")
    }
}
