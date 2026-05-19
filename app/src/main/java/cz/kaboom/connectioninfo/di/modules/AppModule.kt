package cz.kaboom.connectioninfo.di.modules

import cz.kaboom.connectioninfo.common.Const
import cz.kaboom.connectioninfo.data.connectivity.AndroidConnectivityObserver
import cz.kaboom.connectioninfo.data.network.DefaultNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.NetworkLookupApi
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IpApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LookupApi

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @IpApi
    @Singleton
    fun provideIpApi(): NetworkLookupApi {
        return retrofit(Const.IPAPI_BASE_URL).create(NetworkLookupApi::class.java)
    }

    @Provides
    @LookupApi
    @Singleton
    fun provideLookupApi(): NetworkLookupApi {
        return retrofit(Const.LOOKUP_BASE_URL).create(NetworkLookupApi::class.java)
    }

    private fun retrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        observer: AndroidConnectivityObserver
    ): ConnectivityObserver

    @Binds
    @Singleton
    abstract fun bindNetworkInfoRepository(
        repository: DefaultNetworkInfoRepository
    ): NetworkInfoRepository

    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(
        repository: DefaultSpeedTestRepository
    ): SpeedTestRepository
}
