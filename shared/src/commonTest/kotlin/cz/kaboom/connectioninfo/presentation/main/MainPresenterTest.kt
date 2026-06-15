package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.network.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.network.NetworkTransport
import cz.kaboom.connectioninfo.domain.model.speedtest.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.speedtest.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.ConnectivityStatus
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Unit coverage for the shared presentation state reducer and connectivity reactions. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainPresenterTest {

    @Test
    fun connectivityFetchesNetworkDetailsWhenNetworkBecomesAvailable() = runTest {
        val connectivity = FakeConnectivityObserver(initialValue = false)
        val details = sampleNetworkDetails()
        val presenter = presenter(
            connectivityObserver = connectivity,
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(details))
        )

        runCurrent()
        assertFalse(presenter.uiState.value.internetAvailable)
        assertNull(presenter.uiState.value.networkInfo)

        connectivity.status.value = ConnectivityStatus(isConnected = true, activeNetworkKey = "wifi")
        runCurrent()

        assertTrue(presenter.uiState.value.internetAvailable)
        assertEquals(details, presenter.uiState.value.networkInfo)
    }

    @Test
    fun activeNetworkChangeRefreshesNetworkDetailsWhileStayingOnline() = runTest {
        val wifiDetails = sampleNetworkDetails(transport = NetworkTransport.WIFI, internalIp = "192.168.1.10")
        val cellularDetails = sampleNetworkDetails(transport = NetworkTransport.CELLULAR, internalIp = "10.22.0.5")
        val connectivity = FakeConnectivityObserver(
            initialStatus = ConnectivityStatus(isConnected = true, activeNetworkKey = "wifi")
        )
        val repository = QueueNetworkInfoRepository(wifiDetails, cellularDetails)
        val presenter = presenter(connectivity, repository)

        runCurrent()
        assertEquals(wifiDetails, presenter.uiState.value.networkInfo)

        connectivity.status.value = ConnectivityStatus(isConnected = true, activeNetworkKey = "cellular")
        runCurrent()

        assertTrue(presenter.uiState.value.internetAvailable)
        assertEquals(cellularDetails, presenter.uiState.value.networkInfo)
        assertEquals(2, repository.refreshCount)
    }

    @Test
    fun speedTestUpdatesStatsFromRepositoryProgress() = runTest {
        val presenter = presenter(
            connectivityObserver = FakeConnectivityObserver(initialValue = true),
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(sampleNetworkDetails())),
            speedTestRepository = FakeSpeedTestRepository(
                SpeedTestUpdate.Started,
                SpeedTestUpdate.Latency(percent = 100f, milliseconds = 18.0),
                SpeedTestUpdate.Progress(
                    phase = SpeedTestPhase.DOWNLOAD,
                    percent = 50f,
                    bitsPerSecond = 250_000_000.0
                ),
                SpeedTestUpdate.Finished
            )
        )

        runCurrent()
        presenter.onAction(MainAction.ToggleSpeedTest)
        runCurrent()

        val speedState = presenter.uiState.value.speedTest
        assertFalse(speedState.running)
        assertEquals(18f, speedState.ping.current)
        assertEquals(18f, speedState.ping.best)
        assertEquals(1, speedState.ping.count)
        assertEquals(250f, speedState.download.current)
        assertEquals(250f, speedState.download.maximum)
        assertEquals(1, speedState.download.count)
    }

    @Test
    fun rateAggregatesKeepExpectedValues() {
        val speed = SpeedRateStats()
            .add(12f)
            .add(48f)
            .add(30f)

        assertEquals(30f, speed.current)
        assertEquals(48f, speed.maximum)
        assertEquals(30f, speed.average)
        assertEquals(3, speed.count)

        val latency = LatencyStats()
            .add(24f)
            .add(18f)
            .add(30f)

        assertEquals(30f, latency.current)
        assertEquals(18f, latency.best)
        assertEquals(24f, latency.average)
        assertEquals(3, latency.count)
    }

    private fun TestScope.presenter(
        connectivityObserver: ConnectivityObserver,
        networkInfoRepository: NetworkInfoRepository,
        speedTestRepository: SpeedTestRepository = FakeSpeedTestRepository()
    ): MainPresenter {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return MainPresenter(
            connectivityObserver = connectivityObserver,
            networkInfoRepository = networkInfoRepository,
            speedTestRepository = speedTestRepository,
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
        )
    }

    private class FakeConnectivityObserver(
        initialStatus: ConnectivityStatus
    ) : ConnectivityObserver {
        constructor(initialValue: Boolean) : this(
            ConnectivityStatus(
                isConnected = initialValue,
                activeNetworkKey = if (initialValue) "initial" else null
            )
        )

        override val status = MutableStateFlow(initialStatus)
    }

    private class FakeNetworkInfoRepository(
        private val result: Result<NetworkDetails>
    ) : NetworkInfoRepository {
        override suspend fun refresh(): Result<NetworkDetails> = result
    }

    private class QueueNetworkInfoRepository(
        private vararg val details: NetworkDetails
    ) : NetworkInfoRepository {
        var refreshCount = 0
            private set

        override suspend fun refresh(): Result<NetworkDetails> {
            val index = refreshCount.coerceAtMost(details.lastIndex)
            refreshCount += 1
            return Result.success(details[index])
        }
    }

    private class FakeSpeedTestRepository(
        private vararg val updates: SpeedTestUpdate
    ) : SpeedTestRepository {
        override fun runSpeedTest(): Flow<SpeedTestUpdate> = flowOf(*updates)
    }

    private class RunningSpeedTestRepository : SpeedTestRepository {
        override fun runSpeedTest(): Flow<SpeedTestUpdate> = flow {
            emit(SpeedTestUpdate.Started)
            awaitCancellation()
        }
    }

    private fun sampleNetworkDetails(
        transport: NetworkTransport = NetworkTransport.WIFI,
        internalIp: String = "2001:1AEB:7E80:AF00:3412:90FF:FE94:4E0C"
    ) = NetworkDetails(
        transport = transport,
        internalIp = internalIp,
        externalIp = "193.86.34.190",
        lookup = NetworkLookup(
            isp = "T-Mobile Czech Republic a.s.",
            organization = "TMCZ FIXoVF STATIC",
            city = "Prague",
            region = "10",
            regionName = "Prague",
            country = "Czechia",
            countryCode = "CZ",
            latitude = "50.0948",
            longitude = "14.4785"
        )
    )
}
