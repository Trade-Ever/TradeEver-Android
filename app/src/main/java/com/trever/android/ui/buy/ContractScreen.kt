package com.trever.android.ui.buy

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractScreen(
    navController: NavHostController,
    contractId: Long,
    vm: ContractViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(contractId) {
        vm.load(contractId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("계약서 확인", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        vm.downloadToDownloads { ok ->
                            Toast.makeText(
                                ctx,
                                if (ok) "다운로드 완료 (Downloads 폴더)" else "다운로드 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) { Text("다운로드") }

                    TextButton(onClick = {
                        vm.confirmContract(contractId) { ok ->
                            Toast.makeText(
                                ctx,
                                if (ok) "확인 완료" else "확인 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (ok) navController.popBackStack()
                        }
                    }) { Text("확인") }
                }
            )
        }
    ) { padding ->
        when (val s = ui) {
            is ContractUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is ContractUiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("오류: ${s.message}") }

            is ContractUiState.Ready -> {
                val pageCount = s.pageCount
                // 화면 폭 px 계산
                val widthPx = with(density) { (LocalContext.current.resources.displayMetrics.widthPixels) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items((0 until pageCount).toList(), key = { it }) { index ->
                        var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                        LaunchedEffect(index, widthPx) {
                            vm.getOrRenderPage(index, widthPx) { rendered ->
                                bmp = rendered
                            }
                        }

                        if (bmp == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                                contentAlignment = Alignment.Center
                            ) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f)) }
                        } else {
                            Image(
                                bitmap = bmp!!.asImageBitmap(),
                                contentDescription = "page ${index + 1}",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}