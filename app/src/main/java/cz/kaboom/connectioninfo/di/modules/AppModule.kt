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

/**
 * Qualifier for coroutine dispatchers intended for blocking or network-heavy work.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Provides singleton infrastructure dependencies that do not have interface bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Dispatcher used by repositories for platform and network IO. */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /** Shared Ktor client configured for Android networking and Gson content negotiation. */
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

/**
 * Binds concrete data-layer implementations to the repository interfaces used by ViewModels.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Supplies network availability observation to presentation code. */
    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        observer: AndroidConnectivityObserver
    ): ConnectivityObserver

    /** Supplies current network details and public IP lookup behavior. */
    @Binds
    @Singleton
    abstract fun bindNetworkInfoRepository(
        repository: DefaultNetworkInfoRepository
    ): NetworkInfoRepository

    /** Supplies ping, download, and upload speed test execution. */
    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(
        repository: DefaultSpeedTestRepository
    ): SpeedTestRepository
}
