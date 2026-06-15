package cz.kaboom.connectioninfo.di

import android.content.Context
import cz.kaboom.connectioninfo.data.connectivity.AndroidConnectivityObserver
import cz.kaboom.connectioninfo.data.network.DefaultNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.networkJson
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import cz.kaboom.connectioninfo.presentation.main.MainPresenter
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@DependencyGraph(AppScope::class)
interface AndroidAppGraph {
    val mainPresenter: MainPresenter

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AndroidAppGraph
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 30_000
        }
        install(ContentNegotiation) { json(networkJson) }
    }

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @SingleIn(AppScope::class)
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Provides
    fun bindConnectivityObserver(impl: AndroidConnectivityObserver): ConnectivityObserver = impl

    @Provides
    fun bindNetworkInfoRepository(impl: DefaultNetworkInfoRepository): NetworkInfoRepository = impl

    @Provides
    fun bindSpeedTestRepository(impl: DefaultSpeedTestRepository): SpeedTestRepository = impl
}

fun createAndroidAppGraph(context: Context): AndroidAppGraph =
    createGraphFactory<AndroidAppGraph.Factory>().create(context)
