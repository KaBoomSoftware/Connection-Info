package cz.kaboom.connectioninfo.di

import cz.kaboom.connectioninfo.data.connectivity.IosConnectivityObserver
import cz.kaboom.connectioninfo.data.network.IosNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.networkJson
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import cz.kaboom.connectioninfo.presentation.main.MainPresenter
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@DependencyGraph(AppScope::class)
interface IosAppGraph {
    val mainPresenter: MainPresenter

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = HttpClient(Darwin) {
        install(ContentNegotiation) { json(networkJson) }
    }

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @SingleIn(AppScope::class)
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun bindConnectivityObserver(impl: IosConnectivityObserver): ConnectivityObserver = impl

    @Provides
    fun bindNetworkInfoRepository(impl: IosNetworkInfoRepository): NetworkInfoRepository = impl

    @Provides
    fun bindSpeedTestRepository(impl: DefaultSpeedTestRepository): SpeedTestRepository = impl
}

fun createIosAppGraph(): IosAppGraph = createGraph<IosAppGraph>()
