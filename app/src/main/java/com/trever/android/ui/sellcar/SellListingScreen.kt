package com.trever.android.ui.sellcar

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel


// 화면 상태를 정의하는 enum
enum class CurrentScreen {
    PlateNumber,
    InfoCheck,
    ModelPrompt,
    YearInput,
    MileageAndType,
    Details,
    Visuals,
    Options // 새 화면 상태 추가
}

@Composable
fun SellListingScreen() {
    val sellCarViewModel: SellCarViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            CurrentScreen.PlateNumber -> {
                SellCarPlateNumberScreen(
                    sellCarViewModel = sellCarViewModel,
                    onNavigateBack = {
                        Log.d("SellListingScreen", "Back from PlateNumberScreen")
                    },
                    onNextClicked = {
                        sellCarViewModel.updateCurrentStep(2)
                        currentScreen = CurrentScreen.InfoCheck
                    }
                )
            }
            CurrentScreen.InfoCheck -> {
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
                        currentScreen = CurrentScreen.InfoCheck
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
                        currentScreen = CurrentScreen.Details
                    }
                )
            }
            CurrentScreen.Details -> {
                SellCarDetailsScreen(
                    sellCarViewModel = sellCarViewModel,
                    onNavigateBack = {
                        sellCarViewModel.updateCurrentStep(6)
                        currentScreen = CurrentScreen.MileageAndType
                    },
                    onNextClicked = {
                        sellCarViewModel.updateCurrentStep(8)
                        currentScreen = CurrentScreen.Visuals
                    }
                )
            }
            CurrentScreen.Visuals -> {
                SellCarVisualsScreen(
                    sellCarViewModel = sellCarViewModel,
                    onNavigateBack = {
                        sellCarViewModel.updateCurrentStep(7)
                        currentScreen = CurrentScreen.Details
                    },
                    onNextClicked = {
                        sellCarViewModel.updateCurrentStep(9) // Options 화면은 9단계
                        currentScreen = CurrentScreen.Options
                    }
                )
            }
            CurrentScreen.Options -> {
                SellCarOptionsScreen(
                    sellCarViewModel = sellCarViewModel,
                    onNavigateBack = {
                        sellCarViewModel.updateCurrentStep(8) // 이전 화면은 8단계
                        currentScreen = CurrentScreen.Visuals
                    },
                    onNextClicked = {
                        // TODO: 다음 화면 로직 (예: 최종 확인 페이지)
                        Log.d("SellListingScreen", "Next from OptionsScreen")
                    }
                )
            }
        }
    }
}
