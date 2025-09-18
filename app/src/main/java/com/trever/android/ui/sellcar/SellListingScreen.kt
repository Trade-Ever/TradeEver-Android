package com.trever.android.ui.sellcar

import android.util.Log
import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Arrangement // 주석 처리된 InfoCheck에서만 사용
//import androidx.compose.foundation.layout.Column // 주석 처리된 InfoCheck에서만 사용
//import androidx.compose.foundation.layout.Spacer // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp // 주석 처리된 InfoCheck에서만 사용
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel


// 화면 상태를 정의하는 enum
enum class CurrentScreen {
    PlateNumber,
//    InfoCheck,
    ModelPrompt,
    YearInput,
    MileageAndType,
    Details,
    Visuals,
    Options,
    AccidentHistory,
    Price
}

@Composable
fun SellListingScreen() {
    val sellCarViewModel: SellCarViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) } // 초기 화면을 다시 PlateNumber로 변경

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) { // innerPadding 적용
            when (currentScreen) {
                CurrentScreen.PlateNumber -> {
                    SellCarPlateNumberScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            // 첫 화면이므로 뒤로가기 시 아무것도 안 함
                            Log.d("SellListingScreen", "Back from PlateNumberScreen")
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.ModelPrompt
                        }
                    )
                }
//            CurrentScreen.InfoCheck -> {
//                // 임시 화면 구성
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding) // 이 부분은 Box로 옮겨졌으므로 중복 패딩 제거 또는 수정 필요
//                        .padding(16.dp),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("임시 InfoCheck 화면입니다.")
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(onClick = {
//                        sellCarViewModel.updateCurrentStep(1)
//                        currentScreen = CurrentScreen.PlateNumber
//                    }) {
//                        Text("이전 화면으로 (PlateNumber)")
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Button(onClick = {
//                        sellCarViewModel.updateCurrentStep(2)
//                        currentScreen = CurrentScreen.ModelPrompt
//                    }) {
//                        Text("다음 화면으로 (ModelPrompt)")
//                    }
//                }
//            }
                CurrentScreen.ModelPrompt -> {
                    SellCarModelPromptScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(2) // 이전 단계는 1
                            currentScreen = CurrentScreen.PlateNumber
                        },
                        onPromptClicked = {
                            sellCarViewModel.updateSelectedModel("현대 아반떼 SN7 (더미)")
                            sellCarViewModel.updateCurrentStep(3) // 다음 단계는 3
                            currentScreen = CurrentScreen.MileageAndType
                        }
                    )
                }
//            CurrentScreen.YearInput -> {
//                SellCarYearScreen(
//                    sellCarViewModel = sellCarViewModel,
//                    onNavigateBack = {
//                        sellCarViewModel.updateCurrentStep(3)
//                        currentScreen = CurrentScreen.ModelPrompt
//                    },
//                    onNextClicked = {
//                        sellCarViewModel.updateCurrentStep(6)
//                        currentScreen = CurrentScreen.MileageAndType
//                    }
//                )
//            }
                CurrentScreen.MileageAndType -> {
                    SellCarMileageAndTypeScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(2) // 이전 단계는 ModelPrompt
                            currentScreen = CurrentScreen.ModelPrompt
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(4) // 다음 단계는 Details
                            currentScreen = CurrentScreen.Details
                        }
                    )
                }
                CurrentScreen.Details -> {
                    SellCarDetailsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(3) // 이전 단계는 MileageAndType
                            currentScreen = CurrentScreen.MileageAndType
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(5) // 다음 단계는 Visuals
                            currentScreen = CurrentScreen.Visuals
                        }
                    )
                }
                CurrentScreen.Visuals -> {
                    SellCarVisualsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(4) // 이전 단계는 Details
                            currentScreen = CurrentScreen.Details
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(6) // 다음 단계는 Options
                            currentScreen = CurrentScreen.Options
                        }
                    )
                }
                CurrentScreen.Options -> {
                    SellCarOptionsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(5) // 이전 단계는 Visuals
                            currentScreen = CurrentScreen.Visuals
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(7) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.AccidentHistory
                        }
                    )
                }
                CurrentScreen.AccidentHistory -> {
                    SellCarAccidentHistoryScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(6) // 이전 단계는 Options
                            currentScreen = CurrentScreen.Options
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(7) // 실제로는 마지막 단계이므로 다음으로 갈 때 7유지 또는 다른 값으로 설정
                            currentScreen = CurrentScreen.Price
                        }
                    )
                }
                CurrentScreen.Price -> {
                    SellCarPriceScreen(
                        sellCarViewModel = sellCarViewModel,
                        onNavigateBack = {
                            sellCarViewModel.updateCurrentStep(6) // 이전 단계는 AccidentHistory
                            currentScreen = CurrentScreen.AccidentHistory
                        },
                        onRegisterClicked = {
                            Log.d("SellListingScreen", "Register button clicked. Final data: ${sellCarViewModel.uiState.value}")
                            sellCarViewModel.updateCurrentStep(1) // 등록 완료 후 첫 단계로
                            currentScreen = CurrentScreen.PlateNumber // 등록 완료 후 PlateNumber 화면으로
                        }
                    )
                }

                CurrentScreen.YearInput -> TODO() // YearInput 화면 정의 필요
            }
        }
    }
}
