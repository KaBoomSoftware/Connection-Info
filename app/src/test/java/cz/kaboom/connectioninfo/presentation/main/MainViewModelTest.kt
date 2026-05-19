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

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Test
    fun `speed test updates stats from repository progress`() = runTest {
        val viewModel = MainViewModel(
            connectivityObserver = FakeConnectivityObserver(initialValue = true),
            networkInfoRepository = FakeNetworkInfoRepository(Result.success(sampleNetworkDetails())),
            speedTestRepository = FakeSpeedTestRepository(
                SpeedTestUpdate.Started,
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
        assertEquals(250f, speedState.download.current, 0.001f)
        assertEquals(250f, speedState.download.maximum, 0.001f)
        assertEquals(1, speedState.download.count)
    }

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

    private class FakeConnectivityObserver(
        initialValue: Boolean
    ) : ConnectivityObserver {
        val connected = MutableStateFlow(initialValue)
        override val isConnected: Flow<Boolean> = connected
    }

    private class FakeNetworkInfoRepository(
        private val result: Result<NetworkDetails>
    ) : NetworkInfoRepository {
        override suspend fun refresh(): Result<NetworkDetails> = result
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
