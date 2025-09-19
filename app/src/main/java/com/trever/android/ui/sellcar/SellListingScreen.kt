package com.trever.android.ui.sellcar

import android.util.Log
import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Arrangement // 주석 처리된 InfoCheck에서만 사용
//import androidx.compose.foundation.layout.Column // 주석 처리된 InfoCheck에서만 사용
//import androidx.compose.foundation.layout.Spacer // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
//import androidx.compose.material3.Button // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
//import androidx.compose.material3.Text // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
//import androidx.compose.ui.Alignment // 주석 처리된 InfoCheck에서만 사용
import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp // 주석 처리된 InfoCheck에서만 사용
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel


// 화면 상태를 정의하는 enum
enum class CurrentScreen {
    PlateNumber,
//    Entry,
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
fun SellListingScreen(    appNavController: NavHostController? = null // AppNavHost로부터 NavController를 받음)
)
{
    val sellCarViewModel: SellCarViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) } // 초기 화면을 다시 PlateNumber로 변경

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) { // innerPadding 적용
            when (currentScreen) {
//                CurrentScreen.Entry -> {
//                    SellEntryScreen( // 실제 SellEntryScreen 컴포저블 사용
//                        onStartRegistrationClicked = {
//                            sellCarViewModel.updateCurrentStep(1) // PlateNumber가 1단계라고 가정
//                            currentScreen = CurrentScreen.PlateNumber
//                        }
//                    )
//                }
                CurrentScreen.PlateNumber -> {
                    SellCarPlateNumberScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },      // SellEntryScreen으로
                        onStepBack = { appNavController?.popBackStack() },        // 첫 단계이므로 동일하게 처리 또는 다른 로직
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.ModelPrompt
                        }
                    )
                }
//                CurrentScreen.PlateNumber -> {
//                    SellCarPlateNumberScreen(
//                        sellCarViewModel = sellCarViewModel,
//                        onNavigateBack = {
//                            currentScreen = CurrentScreen.Entry
//                            // 첫 화면이므로 뒤로가기 시 아무것도 안 함
//                            Log.d("SellListingScreen", "Back from PlateNumberScreen")
//                        },
//                        onNextClicked = {
//                            sellCarViewModel.updateCurrentStep(2)
//                            currentScreen = CurrentScreen.ModelPrompt
//                        }
//                    )
//                }
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
                        onSystemBack = {
                            // Visuals 화면에서 뒤로가기 시 AppNavHost의 이전 화면으로
                            // (MainScreen의 SellEntryScreen)
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(1)
                            currentScreen = CurrentScreen.PlateNumber // 다음 단계로
                        } ,
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(3) // 다음 단계는 AccidentHistory
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
                        onSystemBack = {
                            // Visuals 화면에서 뒤로가기 시 AppNavHost의 이전 화면으로
                            // (MainScreen의 SellEntryScreen)
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(1)
                            currentScreen = CurrentScreen.ModelPrompt // 다음 단계로
                        } ,
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(3) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.Details
                        }
                    )
                }
                CurrentScreen.Details -> {
                    SellCarDetailsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            // Visuals 화면에서 뒤로가기 시 AppNavHost의 이전 화면으로
                            // (MainScreen의 SellEntryScreen)
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.MileageAndType // 다음 단계로
                        } ,
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(4) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.Visuals
                        }
                    )
                }
                CurrentScreen.Visuals -> {
                    SellCarVisualsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            // Visuals 화면에서 뒤로가기 시 AppNavHost의 이전 화면으로
                            // (MainScreen의 SellEntryScreen)
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(3)
                            currentScreen = CurrentScreen.Details // 다음 단계로
                        } ,
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(5) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.Options
                        }
                    )
                }
                CurrentScreen.Options -> {
                    SellCarOptionsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            appNavController?.popBackStack()
                        },
                        onStepBack = {                                         // Options의 이전 단계는 Visuals
                            sellCarViewModel.updateCurrentStep(4) // Visuals가 6단계라고 가정
                            currentScreen = CurrentScreen.Visuals
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(6) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.AccidentHistory
                        }
                    )
                }
                CurrentScreen.AccidentHistory -> {
                    SellCarAccidentHistoryScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            // Visuals 화면에서 뒤로가기 시 AppNavHost의 이전 화면으로
                            // (MainScreen의 SellEntryScreen)
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(5)
                            currentScreen = CurrentScreen.Options // 다음 단계로
                        } ,
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(7) // 다음 단계는 AccidentHistory
                            currentScreen = CurrentScreen.Price
                        }
                    )
                }
                CurrentScreen.Price -> {
                    SellCarPriceScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            appNavController?.popBackStack()
                        },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(6)
                            currentScreen = CurrentScreen.AccidentHistory // 다음 단계로
                        } ,
                        onRegisterClicked = {
                            Log.d("SellListingScreen", "Register button clicked. Final data: ${sellCarViewModel.uiState.value}")
//                            sellCarViewModel.updateCurrentStep(1) // 등록 완료 후 첫 단계로
//                            currentScreen = CurrentScreen.PlateNumber // 등록 완료 후 PlateNumber 화면으로
                            appNavController?.popBackStack()
                        }
                    )
                }

                CurrentScreen.YearInput -> TODO() // YearInput 화면 정의 필요
            }
        }
    }
}
