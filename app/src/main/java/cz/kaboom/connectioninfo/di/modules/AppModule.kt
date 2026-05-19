package cz.kaboom.connectioninfo.di.modules

import cz.kaboom.connectioninfo.data.connectivity.AndroidConnectivityObserver
import cz.kaboom.connectioninfo.data.network.DefaultNetworkInfoRepository
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            engine {
                connectTimeout = 10_000
                socketTimeout = 30_000
            }
            install(ContentNegotiation) {
                gson()
            }
        }
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
