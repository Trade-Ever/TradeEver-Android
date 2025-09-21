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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
import java.util.Calendar // ViewModel에서 연식 초기값 비교용

// 화면 상태를 정의하는 enum (이전과 동일)
enum class CurrentScreen {
    PlateNumber,
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
fun SellListingScreen(appNavController: NavHostController? = null) {
    val sellCarViewModel: SellCarViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(CurrentScreen.PlateNumber) } // 초기 화면

    // ViewModel의 currentStep을 초기 화면에 맞게 설정 (앱 실행 시 또는 이 화면 진입 시 한 번)
    // LaunchedEffect(Unit) { sellCarViewModel.updateCurrentStep(1) } // 필요시 사용

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                CurrentScreen.PlateNumber -> {
                    // PlateNumber가 첫 화면이므로, 진입 시 currentStep = 1이라고 가정.
                    // sellCarViewModel.updateCurrentStep(1) // 필요하면 여기서 명시적 호출
                    SellCarPlateNumberScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = { appNavController?.popBackStack() },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(2) // ModelPrompt는 2단계
                            currentScreen = CurrentScreen.ModelPrompt
                        }
                    )
                }
                CurrentScreen.ModelPrompt -> {
                    // ModelPrompt 관련 화면들은 모두 논리적으로 2단계에 해당
                    // sellCarViewModel.updateCurrentStep(2) // 이미 이전 단계에서 설정되었을 것
                    SellCarModelPromptScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = { 
                            sellCarViewModel.updateCurrentStep(1) // PlateNumber로 돌아가므로 1단계
                            currentScreen = CurrentScreen.PlateNumber
                        },
                        onSelectModelPathClicked = { 
                            // SelectManufacturer는 ModelPrompt(2단계)의 하위 흐름, currentStep 변경 없음
                            currentScreen = CurrentScreen.SelectManufacturer
                        },
                        onConfirmAndProceedClicked = { 
                            sellCarViewModel.updateCurrentStep(3) // MileageAndType은 3단계
                            currentScreen = CurrentScreen.MileageAndType
                        },
//                        onSkipAndProceedClicked = {
//                            sellCarViewModel.updateSelectedManufacturer("")
//                            sellCarViewModel.updateSelectedModel("")
//                            sellCarViewModel.updateSelectedYear(Calendar.getInstance().get(Calendar.YEAR))
//                            sellCarViewModel.updateCurrentStep(3) // MileageAndType은 3단계
//                            currentScreen = CurrentScreen.MileageAndType
//                        }
                    )
                }
                CurrentScreen.SelectManufacturer -> {
                    // ModelPrompt(2단계)의 하위 흐름, currentStep 변경 없음
                    SelectManufacturerScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.ModelPrompt },
                        onManufacturerSelected = {
                            currentScreen = CurrentScreen.SelectModel
                        }
                    )
                }
                CurrentScreen.SelectModel -> {
                    // ModelPrompt(2단계)의 하위 흐름, currentStep 변경 없음
                    SelectModelScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.SelectManufacturer },
                        onModelSelected = {
                            currentScreen = CurrentScreen.SelectYear
                        }
                    )
                }
                CurrentScreen.SelectYear -> {
                    // ModelPrompt(2단계)의 하위 흐름, currentStep 변경 없음 (연식 선택 후 MileageAndType으로 갈 때 변경)
                    SelectYearScreen(
                        viewModel = sellCarViewModel,
                        onSystemBack = { currentScreen = CurrentScreen.SelectModel },
                        onYearSelected = {
                            sellCarViewModel.updateCurrentStep(3) // MileageAndType은 3단계
                            currentScreen = CurrentScreen.MileageAndType
                        }
                    )
                }
                CurrentScreen.MileageAndType -> {
                    // MileageAndType은 3단계
                    // sellCarViewModel.updateCurrentStep(3) // 이전 단계들에서 설정되었을 것
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
                            sellCarViewModel.updateCurrentStep(2) // ModelPrompt 관련 화면들은 2단계
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
                            sellCarViewModel.updateCurrentStep(2) // ModelPrompt 관련 화면들은 2단계
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(4) // Details는 4단계
                            currentScreen = CurrentScreen.Details
                        }
                    )
                }
                CurrentScreen.Details -> {
                    SellCarDetailsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() }, 
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(3) // MileageAndType은 3단계
                            currentScreen = CurrentScreen.MileageAndType
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(5) // Visuals는 5단계
                            currentScreen = CurrentScreen.Visuals
                        }
                    )
                }
                CurrentScreen.Visuals -> {
                    SellCarVisualsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(4) // Details는 4단계
                            currentScreen = CurrentScreen.Details
                         },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(6) // Options는 6단계
                            currentScreen = CurrentScreen.Options
                        }
                    )
                }
                CurrentScreen.Options -> {
                    SellCarOptionsScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(5) // Visuals는 5단계
                            currentScreen = CurrentScreen.Visuals
                        },
                        onNextClicked = {
                            sellCarViewModel.updateCurrentStep(7) // AccidentHistory는 7단계 (Price가 마지막 최종 확인이라면)
                            currentScreen = CurrentScreen.AccidentHistory
                        }
                    )
                }
                CurrentScreen.AccidentHistory -> {
                     // 만약 AccidentHistory가 7단계 중 마지막이면, 다음은 Price (별도 단계) 또는 요약일 수 있음
                     // 여기서는 Price를 7단계 중 마지막으로 보고, AccidentHistory를 6단계 다음으로 가정하여 수정.
                     // 또는 totalSteps를 8로 늘리고 Price를 8단계로 할 수도 있음.
                     // 우선, Options(6) -> AccidentHistory(7) -> Price(최종, currentStep 유지 또는 증가 안함)로 가정.
                    SellCarAccidentHistoryScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(6) // Options는 6단계
                            currentScreen = CurrentScreen.Options
                        },
                        onNextClicked = {
                            // Price가 최종 등록 화면이라면, currentStep은 7에서 유지하거나 더 이상 증가하지 않을 수 있음.
                            // 여기서는 Price 화면으로 가면서 7단계를 유지한다고 가정.
                            // sellCarViewModel.updateCurrentStep(7) // 이미 7단계일 것이므로, 또는 다음 단계가 있다면 증가
                            currentScreen = CurrentScreen.Price
                        }
                    )
                }
                CurrentScreen.Price -> {
                    // Price가 7단계의 마지막 내용.
                    // sellCarViewModel.updateCurrentStep(7) // 이전 화면에서 7로 설정됨
                    SellCarPriceScreen(
                        sellCarViewModel = sellCarViewModel,
                        onSystemBack = { appNavController?.popBackStack() },
                        onStepBack = {
                            sellCarViewModel.updateCurrentStep(7) // AccidentHistory도 7단계의 일부로 본다면 유지, 아니면 6으로.
                                                              // 여기서는 AccidentHistory에서 Price로 왔으므로 7단계로 유지.
                            currentScreen = CurrentScreen.AccidentHistory
                        },
                        onRegisterClicked = {
                            Log.d("SellListingScreen", "Register button clicked. Final data: ${sellCarViewModel.uiState.value}")
                            appNavController?.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
