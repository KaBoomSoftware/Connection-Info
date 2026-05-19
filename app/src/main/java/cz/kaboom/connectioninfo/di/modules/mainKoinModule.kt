package cz.kaboom.connectioninfo.di.modules

import cz.kaboom.connectioninfo.common.Const
import cz.kaboom.connectioninfo.data.connectivity.AndroidConnectivityObserver
import cz.kaboom.connectioninfo.data.network.DefaultNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.NetworkLookupApi
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import cz.kaboom.connectioninfo.presentation.main.MainViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

val mainKoinModule = module {
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }

    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(named(IP_API)) {
        retrofit(Const.IPAPI_BASE_URL).create(NetworkLookupApi::class.java)
    }

    single(named(LOOKUP_API)) {
        retrofit(Const.LOOKUP_BASE_URL).create(NetworkLookupApi::class.java)
    }

    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }

    single<NetworkInfoRepository> {
        DefaultNetworkInfoRepository(
            context = androidContext(),
            ipApi = get(named(IP_API)),
            lookupApi = get(named(LOOKUP_API)),
            ioDispatcher = get(named(IO_DISPATCHER))
        )
    }

    single<SpeedTestRepository> {
        DefaultSpeedTestRepository(
            client = get(),
            ioDispatcher = get(named(IO_DISPATCHER))
        )
    }

    viewModel { MainViewModel(get(), get(), get()) }
}

private fun retrofit(baseUrl: String): Retrofit {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private const val IO_DISPATCHER = "ioDispatcher"
private const val IP_API = "ipApi"
private const val LOOKUP_API = "lookupApi"
