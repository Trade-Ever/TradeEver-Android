package com.trever.android.ui.buy

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trever.android.data.network.ApiClient

import com.trever.android.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface ContractUiState {
    data object Loading : ContractUiState
    data class Ready(val pageCount: Int) : ContractUiState
    data class Error(val message: String) : ContractUiState
}

class ContractViewModel(app: Application) : AndroidViewModel(app) {


    private val repo = VehicleRepository(ApiClient.vehicleApi,app)

    private val _ui = MutableStateFlow<ContractUiState>(ContractUiState.Loading)
    val ui = _ui.asStateFlow()

    private var pdfFile: File? = null
    private var renderer: PdfRenderer? = null

    // 간단 LRU 캐시(페이지 -> Bitmap)
    private val cache = object : LruCache<Int, Bitmap>(8) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount / 1024
    }

    fun load(contractId: Long) = viewModelScope.launch {
        try {
            _ui.value = ContractUiState.Loading
            val file = repo.downloadToCache(contractId)
            android.util.Log.d("ContractViewModel", "PDF file path: ${file.absolutePath}")
            pdfFile = file
            val r = repo.openRenderer(file)
            renderer = r
            _ui.value = ContractUiState.Ready(r.pageCount)
        } catch (e: Exception) {
            android.util.Log.e("ContractViewModel", "계약서 로드 실패", e)
            _ui.value = ContractUiState.Error(e.message ?: "알 수 없는 오류")
        }
    }

    fun getOrRenderPage(index: Int, widthPx: Int, onReady: (Bitmap) -> Unit) {
        val cached = cache.get(index)
        if (cached != null) {
            onReady(cached)
            return
        }
        viewModelScope.launch {
            val r = renderer ?: return@launch
            val bmp = repo.renderPage(r, index, widthPx)
            cache.put(index, bmp)
            onReady(bmp)
        }
    }

    fun downloadToDownloads(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        try {
            val file = pdfFile ?: error("파일 없음")
            repo.saveToDownloads(file, displayName = "contract_${file.name}")
            onDone(true)
        } catch (e: Exception) {
            onDone(false)
        }
    }

    fun confirmContract(contractId: Long, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        // TODO: api 호출(예: POST api/v1/contracts/{id}/confirm)
        // val ok = runCatching { api.confirm(contractId) }.isSuccess
        val ok = true // 데모
        onResult(ok)
    }

    override fun onCleared() {
        renderer?.close()
        pdfFile?.delete()
        super.onCleared()
    }
}