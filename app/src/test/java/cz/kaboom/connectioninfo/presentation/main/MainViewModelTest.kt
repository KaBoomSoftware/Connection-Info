package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.NetworkDetails
import cz.kaboom.connectioninfo.domain.model.NetworkLookup
import cz.kaboom.connectioninfo.domain.model.NetworkTransport
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit coverage for the main presentation state reducer and connectivity reactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    /** Replaces Main dispatcher so [MainViewModel] can use viewModelScope in unit tests. */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Verifies that becoming online triggers an immediate network info refresh. */
    @Test
    fun `connectivity fetches network details when network becomes available`() = runTest {
        val connectivity = FakeConnectivityObserver(initialValue = false)
        val details = sampleNetworkDetails()
        val viewModel = MainViewModel(
            connectivityObserver = connectivity,
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(details)),
            speedTestRepository = FakeSpeedTestRepository()
        )

        assertFalse(viewModel.uiState.value.internetAvailable)
        assertNull(viewModel.uiState.value.networkInfo)

        connectivity.connected.value = true

        assertTrue(viewModel.uiState.value.internetAvailable)
        assertEquals(details, viewModel.uiState.value.networkInfo)
    }

    /** Verifies speed test latency and throughput events are folded into UI statistics. */
    @Test
    fun `speed test updates stats from repository progress`() = runTest {
        val viewModel = MainViewModel(
            connectivityObserver = FakeConnectivityObserver(initialValue = true),
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(sampleNetworkDetails())),
            speedTestRepository = FakeSpeedTestRepository(
                SpeedTestUpdate.Started,
                SpeedTestUpdate.Latency(
                    percent = 100f,
                    milliseconds = 18.0
                ),
                SpeedTestUpdate.Progress(
                    phase = SpeedTestPhase.DOWNLOAD,
                    percent = 50f,
                    bitsPerSecond = 250_000_000.0
                ),
                SpeedTestUpdate.Finished
            )
        )

        viewModel.onAction(MainAction.ToggleSpeedTest)

        val speedState = viewModel.uiState.value.speedTest
        assertFalse(speedState.running)
        assertEquals(18f, speedState.ping.current, 0.001f)
        assertEquals(18f, speedState.ping.best, 0.001f)
        assertEquals(1, speedState.ping.count)
        assertEquals(250f, speedState.download.current, 0.001f)
        assertEquals(250f, speedState.download.maximum, 0.001f)
        assertEquals(1, speedState.download.count)
    }

    /** Verifies losing connectivity cancels active speed work and resets running state. */
    @Test
    fun `network loss stops active speed test`() = runTest {
        val connectivity = FakeConnectivityObserver(initialValue = true)
        val viewModel = MainViewModel(
            connectivityObserver = connectivity,
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(sampleNetworkDetails())),
            speedTestRepository = RunningSpeedTestRepository()
        )

        viewModel.onAction(MainAction.ToggleSpeedTest)

        assertTrue(viewModel.uiState.value.speedTest.running)

        connectivity.connected.value = false

        assertFalse(viewModel.uiState.value.internetAvailable)
        assertFalse(viewModel.uiState.value.speedTest.running)
    }

    /** Controllable connectivity observer used by tests. */
    private class FakeConnectivityObserver(
        initialValue: Boolean
    ) : ConnectivityObserver {
        /** Mutable test handle for network availability. */
        val connected = MutableStateFlow(initialValue)

        /** Domain stream exposed to the ViewModel under test. */
        override val isConnected: Flow<Boolean> = connected
    }

    /** Fake network repository returning a preconfigured result. */
    private class FakeNetworkInfoRepository(
        private val result: Result<NetworkDetails>
    ) : NetworkInfoRepository {
        /** Returns [result] synchronously for deterministic tests. */
        override suspend fun refresh(): Result<NetworkDetails> = result
    }

    /** Fake speed-test repository emitting a finite list of updates. */
    private class FakeSpeedTestRepository(
        private vararg val updates: SpeedTestUpdate
    ) : SpeedTestRepository {
        /** Emits the configured update sequence. */
        override fun runSpeedTest(): Flow<SpeedTestUpdate> = flowOf(*updates)
    }

    /** Fake speed-test repository that stays active until cancelled. */
    private class RunningSpeedTestRepository : SpeedTestRepository {
        /** Emits Started, then suspends forever so cancellation behavior can be asserted. */
        override fun runSpeedTest(): Flow<SpeedTestUpdate> = flow {
            emit(SpeedTestUpdate.Started)
            awaitCancellation()
        }
    }

    /** Representative successful network details fixture. */
    private fun sampleNetworkDetails() = NetworkDetails(
        transport = NetworkTransport.WIFI,
        internalIp = "2001:1AEB:7E80:AF00:3412:90FF:FE94:4E0C",
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
