package com.trever.android.ui.sellcar

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
// import androidx.lifecycle.viewmodel.compose.viewModel // 이제 파라미터로 받으므로 이 임포트 삭제
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp // 주석 처리된 InfoCheck에서만 사용
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import java.util.Calendar // ViewModel에서 연식 초기값 비교용
//import com.trever.android.ui.sellcar.viewmodel.SellCarViewModelFactory

// 화면 상태를 정의하는 enum (이전과 동일)
enum class CurrentScreen {
    PlateNumber,
//    Entry,
//    InfoCheck,
    ModelPrompt,
    SelectManufacturer,
    SelectModel,
    SelectYear,
    MileageAndType,
    Details,
    Visuals,
    Options,
    AccidentHistory,
    Price
}

@Composable
fun SellListingScreen(
    appNavController: NavHostController? = null,
    sellCarViewModel: SellCarViewModel // ViewModel을 파라미터로 받음
) {
    // val sellCarViewModel: SellCarViewModel = viewModel() // 이 줄 삭제
    // 초기 화면
    val context = LocalContext.current
//    val sellCarViewModel: SellCarViewModel = viewModel(factory = SellCarViewModelFactory(context))
    var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) } // 초기 화면을 다시 PlateNumber로 변경

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                CurrentScreen.PlateNumber -> {
                    SellCarPlateNumberScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = { appNavController?.popBackStack() },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.ModelPrompt
                        }
                    )
                }
                CurrentScreen.ModelPrompt -> {
                    SellCarModelPromptScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(1)
                            currentScreen = CurrentScreen.PlateNumber
                        },
                        onSelectModelPathClicked = {
                            currentScreen = CurrentScreen.SelectManufacturer
                        },
                        onConfirmAndProceedClicked = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.MileageAndType
                        },
                    )
                }
                CurrentScreen.SelectManufacturer -> {
                    SelectManufacturerScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.ModelPrompt },
                        onManufacturerSelected = {
                            currentScreen = CurrentScreen.SelectModel
                        }
                    )
                }
                CurrentScreen.SelectModel -> {
                    SelectModelScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.SelectManufacturer },
                        onModelSelected = {
                            currentScreen = CurrentScreen.SelectYear
                        }
                    )
                }
                CurrentScreen.SelectYear -> {
                    SelectYearScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.SelectModel },
                        onYearSelected = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.MileageAndType
                        }
                    )
                }
                CurrentScreen.MileageAndType -> {
                    SellCarMileageAndTypeScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = {
                            val uiState = sellCarViewModel.uiState.value
                            if (uiState.selectedManufacturer.isNotBlank() ||
                                uiState.selectedModel.isNotBlank() ||
                                uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR)) {
                                currentScreen = CurrentScreen.SelectYear
                            } else {
                                currentScreen = CurrentScreen.ModelPrompt
                            }
                            sellCarViewModel.updateCurrentStep(2)
                        },
                        onStepBack = {
                            val uiState = sellCarViewModel.uiState.value
                            if (uiState.selectedManufacturer.isNotBlank() ||
                                uiState.selectedModel.isNotBlank() ||
                                uiState.selectedYear != Calendar.getInstance().get(Calendar.YEAR)) {
                                currentScreen = CurrentScreen.SelectYear
                            } else {
                                currentScreen = CurrentScreen.ModelPrompt
                            }
                            sellCarViewModel.updateCurrentStep(2)
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(3)
                            currentScreen = CurrentScreen.Details
                        }
                    )
                }
                CurrentScreen.Details -> {
                    SellCarDetailsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(2)
                            currentScreen = CurrentScreen.MileageAndType
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(4)
                            currentScreen = CurrentScreen.Visuals
                        }
                    )
                }
                CurrentScreen.Visuals -> {
                    SellCarVisualsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(3)
                            currentScreen = CurrentScreen.Details
                         },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(5)
                            currentScreen = CurrentScreen.Options
                        }
                    )
                }
                CurrentScreen.Options -> {
                    SellCarOptionsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(4)
                            currentScreen = CurrentScreen.Visuals
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(6)
                            currentScreen = CurrentScreen.AccidentHistory
                        }
                    )
                }
                CurrentScreen.AccidentHistory -> {
                    SellCarAccidentHistoryScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(5)
                            currentScreen = CurrentScreen.Options
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(7)
                            currentScreen = CurrentScreen.Price
                        }
                    )
                }
                CurrentScreen.Price -> {
                    SellCarPriceScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(6)
                            currentScreen = CurrentScreen.AccidentHistory
                        },
                        onRegisterClicked = {
                            Log.d("SellListingScreen", "Register button clicked. Final data: ${sellCarViewModel.uiState.value}")
                            sellCarViewModel.completeRegistrationAndAddCar() // 차량 등록 로직 호출
                            appNavController?.popBackStack() // 이전 화면으로 돌아감 (SellEntryScreen이 있는 MainScreen으로)
                        }
                    )
                }
            }
        }
    }
}
