package com.trever.android.ui.buy

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient

import com.trever.android.data.remote.ContractDetail

import com.trever.android.data.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class ContractSummaryUi(
    val contractId: Long,
    val transactionId: Long,
    val buyerName: String,
    val sellerName: String,
    val signedAtText: String
)

sealed interface ContractSheetState {
    data object Loading : ContractSheetState
    data class Ready(val summary: ContractSummaryUi, val pageCount: Int) : ContractSheetState
    data class Error(val message: String) : ContractSheetState
}

class ContractSheetViewModel(app: Application) : AndroidViewModel(app) {
    private val api = ApiClient.vehicleApi
    private val repo = VehicleRepository(api, app)

    private val _state = MutableStateFlow<ContractSheetState>(ContractSheetState.Loading)
    val state = _state.asStateFlow()

    private var pdfFile: File? = null
    private var renderer: PdfRenderer? = null

    private val cache = object : LruCache<Int, Bitmap>(8) {
        override fun sizeOf(key: Int, value: Bitmap) = value.byteCount / 1024
    }

    fun load(contractId: Long) = viewModelScope.launch {
        try {
            _state.value = ContractSheetState.Loading

            val res = api.getContract(contractId)
            val d: ContractDetail = res.data

            // 1) 요약 변환
            val signedAtText = runCatching {
                val odt = OffsetDateTime.parse(d.signedAt)
                odt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT).withLocale(Locale.KOREA))
            }.getOrElse { d.signedAt }

            val summary = ContractSummaryUi(
                contractId = d.contractId,
                transactionId = d.transactionId,
                buyerName = d.buyerName,
                sellerName = d.sellerName,
                signedAtText = signedAtText
            )

            // 2) PDF 다운로드 & 렌더러 준비
            val file = repo.downloadToCache(d.contractId)
            pdfFile = file
            val r = repo.openRenderer(file)
            renderer = r

            _state.value = ContractSheetState.Ready(summary, r.pageCount)
        } catch (e: Exception) {
            _state.value = ContractSheetState.Error(e.message ?: "로딩 실패")
        }
    }

    fun getOrRenderPage(index: Int, widthPx: Int, onReady: (Bitmap) -> Unit) {
        cache.get(index)?.let { onReady(it); return }
        viewModelScope.launch(Dispatchers.Default) {
            val r = renderer ?: return@launch
            // 확대 대비해 기본 해상도 1.5배로 렌더
            val bmp = repo.renderPage(r, index, (widthPx * 1.5f).toInt())
            cache.put(index, bmp)
            onReady(bmp)
        }
    }

    fun downloadToDownloads(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        runCatching {
            val f = pdfFile ?: error("파일 없음")
            repo.saveToDownloads(f, "contract_${f.nameWithoutExtension}.pdf")
        }.onSuccess { onDone(true) }.onFailure { onDone(false) }
    }

    fun confirm(contractId: Long, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        // TODO: 확인 API 연결
        onResult(true)
    }

    override fun onCleared() {
        renderer?.close()
        pdfFile?.delete()
        super.onCleared()
    }
}