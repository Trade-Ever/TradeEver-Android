//package com.trever.android.di
//
//import android.content.Context
//import com.google.firebase.auth.FirebaseAuth
//import com.trever.android.data.auth.AuthManager
//import com.trever.android.data.auth.TokenStore
//import com.trever.android.data.local.EncryptedStorageManager
//import com.trever.android.data.network.ApiClient
//import com.trever.android.data.repository.AuctionRepository
//import com.trever.android.data.repository.AuthRepository
//import com.trever.android.data.repository.MyPageRepository
//import com.trever.android.data.repository.RecentlyViewedRepository
//import com.trever.android.data.repository.SellRepository
//import com.trever.android.data.repository.VehicleDetailRepository
//import com.trever.android.data.repository.VehicleRepository
//import com.trever.android.ui.auth.AuthViewModel
//import com.trever.android.ui.main.MainViewModel
//import com.trever.android.ui.myPage.MyPageViewModel
//import com.trever.android.ui.myPage.charge.ChargeViewModel
//import com.trever.android.ui.myPage.edit.ProfileEditViewModel
//import com.trever.android.ui.myPage.wallet.WalletViewModel
//import com.trever.android.ui.myPage.withdraw.WithdrawViewModel
//import com.trever.android.ui.sellcar.SellCarViewModelFactory
//import com.trever.android.ui.sellcar.viewmodel.SellCarViewModel
//import com.trever.android.ui.sellcar.viewmodel.SellEntryViewModel
//import com.trever.android.ui.splash.SplashViewModel
//import com.trever.android.ui.vehicleDetail.VehicleDetailViewModel
//import org.koin.android.ext.koin.androidContext
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.dsl.module
//
//val appModule = module {
//    single { EncryptedStorageManager(androidContext()) }
//    single { TokenStore(get()) }
//    single { AuthManager(androidContext(), get()) } // TokenStore 주입
//    single { FirebaseAuth.getInstance() }
//    single { ApiClient }
//
//    // Repositories
//    single { AuthRepository(get(), get(), get()) } // ApiClient.authApi, TokenStore, AuthManager 주입
//    single { VehicleRepository(get(), androidContext()) }
//    single { MyPageRepository(get(), get()) } // ApiClient.myPageApi, ApiClient.likedApi 추가
//    single { RecentlyViewedRepository(androidContext()) } // Context 주입
//    single { SellRepository(get()) }
//    single { AuctionRepository(get()) } // ApiClient.vehicleApi 주입
//    single { VehicleDetailRepository(get(), androidContext()) }
//
//
//    // ViewModels
//    viewModel { AuthViewModel(get(), get()) } // AuthRepository, TokenStore 주입
//    viewModel { SplashViewModel(get()) }
//    viewModel { MainViewModel(get()) }
//    viewModel { SellEntryViewModel(androidContext().applicationContext) }
//    viewModel { MyPageViewModel(get(), get(), get(), get()) } // AuthRepository, TokenStore, AuctionRepository 추가
//    viewModel { ChargeViewModel(get()) }
//    viewModel { WithdrawViewModel(get()) }
//    viewModel { WalletViewModel(get()) }
//    viewModel { ProfileEditViewModel(get(), get()) }
//    viewModel { VehicleDetailViewModel(get(), get()) } // VehicleDetailRepository, AuctionRepository 주입
//
//    // ViewModelFactory
//    // SellCarViewModelFactory 등록 (필요한 경우)
//    single { (context: Context) -> SellCarViewModelFactory(context.applicationContext, get()) }
//
//    // SellCarViewModel 등록 (필요한 경우, ViewModelFactory를 통해 생성)
//    viewModel { (factory: SellCarViewModelFactory) -> SellCarViewModel(factory.application, factory.sellRepository) }
//
//}
