package com.trever.android

import android.app.Application
import com.google.gson.Gson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.network.AuthInterceptor
import com.trever.android.data.network.TokenAuthenticator
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.remote.MyPageApi
import com.trever.android.data.remote.TransactionApi
import com.trever.android.data.remote.VehicleApi
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.data.repository.TransactionRepository
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.data.repository.AuctionRepository // AuctionRepository 임포트 추가
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.myPage.MyPageViewModel
import com.trever.android.ui.myPage.TransactionViewModel
import com.trever.android.ui.search.SearchViewModel
import com.trever.android.ui.sellcar.viewmodel.SellEntryViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class TreverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TreverApplication)
            modules(appModule)
        }
    }
}

val appModule = module {

    // --- Network Layer ---
    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }

    // Json 객체 설정 (ignoreUnknownKeys = true)
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            // explicitNulls = false // 필요에 따라 추가
            // coerceInputValues = true // 필요에 따라 추가
        }
    }

    single<OkHttpClient> {
        val tokenStore = get<TokenStore>()
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl("http://54.180.107.111:8080/") // 서버 URL은 환경에 맞게 관리하는 것이 좋습니다.
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .client(OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>()).build()) // 로깅 인터셉터만 가지는 클라이언트
            .build()
        val refreshAuthApi = refreshRetrofit.create(AuthApi::class.java)

        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenAuthenticator(tokenStore, refreshAuthApi))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("http://54.180.107.111:8080/") // 서버 URL은 환경에 맞게 관리하는 것이 좋습니다.
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // --- API Interfaces ---
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
    single<VehicleApi> { get<Retrofit>().create(VehicleApi::class.java) }
    single<MyPageApi> { get<Retrofit>().create(MyPageApi::class.java) }
    single<TransactionApi> { get<Retrofit>().create(TransactionApi::class.java) }

    // --- Data Layer ---
    single { Gson() } // Gson은 kotlinx.serialization과 별개
    single { TokenStore(androidContext()) } // TokenStore 정의
    // AuthRepository는 TokenStore, AuthApi, Context(AuthManager 통해)를 주입받도록 수정될 수 있음 (현재는 TokenStore, AuthApi, Context 직접)
    // 만약 AuthManager를 통해 Context를 주입받는다면, AuthRepository(get(), get(), get<AuthManager>().context) 와 같이 사용
    // 현재 코드에서는 AuthManager가 직접 사용되지 않고, TokenStore가 Context를 직접 받으므로 아래와 같이 유지
    single { AuthRepository(get(), get(), androidContext()) } // AuthApi, TokenStore, Context
    single { MyPageRepository(get()) } // MyPageRepository는 MyPageApi를 주입받음
    single { VehicleRepository(get(), androidContext(), get()) } // VehicleApi, Context, Gson
    single { TransactionRepository(get()) } // TransactionApi
    single { AuctionRepository(get()) } // VehicleApi (ApiClient.vehicleApi와 동일) 주입

    // --- UI Layer (ViewModels) ---
    viewModel { AuthViewModel(get()) } // AuthRepository
    // MyPageViewModel에 MyPageRepository, AuthRepository, TokenStore, AuctionRepository를 주입
    viewModel { MyPageViewModel(get(), get(), get(), get()) } 
    viewModel { TransactionViewModel(get()) } // TransactionRepository
    viewModel { SellEntryViewModel(get()) } // SellRepository 또는 VehicleRepository (기존 코드에서는 VehicleRepository 사용)
    viewModel { SearchViewModel() } // 의존성 없음
}
