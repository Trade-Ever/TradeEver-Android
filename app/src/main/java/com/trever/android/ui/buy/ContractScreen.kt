package com.trever.android.ui.buy

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL


//@Composable
//fun ContractScreen(
//    contractPdfUrl: String,
//    onDownload: () -> Unit,
//    onComplete: () -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp)
//    ) {
//        Text("계약서", style = MaterialTheme.typography.titleLarge)
//        Spacer(Modifier.height(16.dp))
//        // PDF 뷰어 표시
//        AndroidView(
//            factory = { context ->
//                val pdfView = PDFView(context, null)
//                // PDF 파일을 임시로 다운로드 후 표시
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val url = URL(contractPdfUrl)
//                        val connection = url.openConnection()
//                        connection.connect()
//                        val input = connection.getInputStream()
//                        val file = File.createTempFile("contract", ".pdf", context.cacheDir)
//                        val output = FileOutputStream(file)
//                        input.copyTo(output)
//                        input.close()
//                        output.close()
//                        withContext(Dispatchers.Main) {
//                            pdfView.fromFile(file).load()
//                        }
//                    } catch (e: Exception) {
//                        // 에러 처리
//                    }
//                }
//                FrameLayout(context).apply { addView(pdfView) }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//        )
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            Button(onClick = onDownload) { Text("계약서 다운로드") }
//            Button(onClick = onComplete) { Text("완료") }
//        }
//    }
//}