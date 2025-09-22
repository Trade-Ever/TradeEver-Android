package com.trever.android

import android.app.Application
import com.google.gson.Gson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.network.AuthInterceptor
import com.trever.android.data.network.TokenAuthenticator
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.remote.MyPageApi
import com.trever.android.data.remote.VehicleApi
import com.trever.android.data.repository.AuthRepository
import com.trever.android.data.repository.MyPageRepository
import com.trever.android.data.repository.VehicleRepository
import com.trever.android.ui.auth.AuthViewModel
import com.trever.android.ui.myPage.MyPageViewModel
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

    single {
        // TokenAuthenticator가 AuthApi를, AuthApi는 Retrofit->OkHttpClient를 필요로 하므로
        // 순환 참조가 발생합니다. 이 문제를 해결하기 위해, 토큰 갱신 전용 AuthApi를
        // 별도의 간단한 OkHttpClient로 여기서 직접 생성합니다.
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl("http://54.180.107.111:8080/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(OkHttpClient()) // 인증 인터셉터가 없는 순수한 클라이언트
            .build()
        val refreshAuthApi = refreshRetrofit.create(AuthApi::class.java)

        // 이제 주입받을 나머지 의존성과 함께 메인 OkHttpClient를 생성합니다.
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>()) // 로그 확인용 인터셉터
            .addInterceptor(AuthInterceptor(get())) // 매 요청에 토큰을 추가하는 인터셉터
            .authenticator(TokenAuthenticator(get(), refreshAuthApi)) // 401 발생 시 토큰을 갱신하는 Authenticator
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("http://54.180.107.111:8080/")
            .client(get<OkHttpClient>()) // 위에서 설정한 메인 OkHttpClient를 주입받습니다.
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // --- API Interfaces (이제 메인 Retrofit을 사용합니다) ---
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
    single<VehicleApi> { get<Retrofit>().create(VehicleApi::class.java) }
    single<MyPageApi> { get<Retrofit>().create(MyPageApi::class.java) }

    // --- Data Layer ---
    single { Gson() }
    single { TokenStore(androidContext()) } // Context 주입 명시
    single { AuthRepository(get(), get(), get()) }
    single { MyPageRepository(get()) }
    // VehicleRepository는 Context가 필요할 수 있으므로 androidContext()를 명시적으로 주입합니다.
    single { VehicleRepository(get(), androidContext(), get()) }

    // --- UI Layer (ViewModels) ---
    viewModel { AuthViewModel(get()) }
    viewModel { MyPageViewModel(get(), get()) }
}
