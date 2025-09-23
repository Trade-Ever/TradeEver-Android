package com.trever.android


import android.app.Application
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.repository.AuthRepository
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.search.SearchViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit

class TreverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TreverApplication)
            modules(authModule,viewModelModule)
        }
    }
}

val authModule = module {
    single { TokenStore(get()) }
    single<AuthApi> {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("http://54.180.107.111:8080/") // 실제 API 주소로 변경

            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
            .create(AuthApi::class.java)
    }

    single { AuthRepository(get(), get(), get()) }
    viewModel { AuthViewModel(get()) }
}

val viewModelModule = module {
    viewModel { SearchViewModel() }
}