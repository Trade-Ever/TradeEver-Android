package com.trever.android.data.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trever.android.data.auth.TokenStore
import com.trever.android.data.remote.AuctionApi
import com.trever.android.data.remote.AuthApi
import com.trever.android.data.remote.ProfileApi
import com.trever.android.data.remote.SearchApi
import com.trever.android.data.remote.VehicleApi
import com.trever.android.data.remote.WalletApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import okhttp3.MediaType.Companion.toMediaType

object ApiClient {

    private const val BASE_URL = "http://54.180.107.111:8080/"

    lateinit var tokenStore: TokenStore
        private set

    lateinit var authApi: AuthApi
        private set

    lateinit var vehicleApi: VehicleApi
        private set

    lateinit var auctionApi: AuctionApi
        private set

    lateinit var walletApi: WalletApi
        private set

    lateinit var profileApi: ProfileApi
        private set

    lateinit var searchApi: SearchApi
        private set

    lateinit var profileApi: ProfileApi
        private set

    fun init(context: Context, baseUrl: String = BASE_URL) {
        tokenStore = TokenStore(context)

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val mediaType = "application/json".toMediaType()
        val converter = json.asConverterFactory(mediaType)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // (1) 인증 없이 쓰는 클라이언트 & Retrofit (AuthApi용)
        val baseClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    // 필요한 다른 헤더 추가
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15.seconds.toJavaDuration())
            .readTimeout(15.seconds.toJavaDuration())
            .writeTimeout(15.seconds.toJavaDuration())
            .build()

        val retrofitForAuth = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(baseClient)
            .addConverterFactory(converter)
            .build()

        authApi = retrofitForAuth.create(AuthApi::class.java)



        // (2) 인증 인터셉터/리프레시 인증자 부착한 클라이언트 & Retrofit (CarApi 등)
        val authedClient = baseClient.newBuilder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenAuthenticator(tokenStore, authApi))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authedClient)
            .addConverterFactory(converter)
            .build()

        vehicleApi = retrofit.create(VehicleApi::class.java)
        walletApi = retrofit.create(WalletApi::class.java)
        profileApi = retrofit.create(ProfileApi::class.java)
        profileApi = retrofit.create(ProfileApi::class.java)
        searchApi = retrofit.create(SearchApi::class.java)
        auctionApi = retrofit.create(AuctionApi::class.java)


    }
}