package cz.kaboom.connectioninfo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation coordinator for connectivity, network details, and speed-test state.
 *
 * The ViewModel exposes a single immutable [MainUiState] stream and translates repository events
 * into stable state for Compose.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val networkInfoRepository: NetworkInfoRepository,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    /** Mutable backing state kept private to preserve unidirectional data flow. */
    private val _uiState = MutableStateFlow(MainUiState())

    /** Public state observed by the Compose screen. */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** Active speed test collection job, if a test is running. */
    private var speedTestJob: Job? = null

    /** Active network refresh job; cancelled when a newer refresh starts. */
    private var networkRefreshJob: Job? = null

    init {
        observeConnectivity()
    }

    /** Handles UI intents from tabs, buttons, and refresh requests. */
    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> _uiState.update { it.copy(selectedTab = action.tab) }
            MainAction.ToggleSpeedTest -> toggleSpeedTest()
            MainAction.RefreshNetworkInfo -> refreshNetworkInfo()
        }
    }

    /** Keeps UI state synchronized with Android's validated connectivity stream. */
    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isConnected.collect { connected ->
                _uiState.update {
                    it.copy(
                        internetAvailable = connected,
                        networkInfo = if (connected) it.networkInfo else null,
                        errorMessage = null
                    )
                }

                if (connected) {
                    refreshNetworkInfo()
                } else {
                    stopSpeedTest()
                }
            }
        }
    }

    /** Refreshes network info and stores either the latest details or an error message. */
    private fun refreshNetworkInfo() {
        networkRefreshJob?.cancel()
        networkRefreshJob = viewModelScope.launch {
            networkInfoRepository.refresh()
                .onSuccess { details ->
                    _uiState.update { it.copy(networkInfo = details, errorMessage = null) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.localizedMessage) }
                }
        }
    }

    /** Starts a new speed test when possible or cancels the currently running one. */
    private fun toggleSpeedTest() {
        if (speedTestJob?.isActive == true) {
            stopSpeedTest()
        } else if (_uiState.value.internetAvailable) {
            startSpeedTest()
        }
    }

    /** Collects repository events for a fresh speed test run. */
    private fun startSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            speedTestRepository.runSpeedTest().collect { update ->
                reduceSpeedTestUpdate(update)
            }
        }
    }

    /** Cancels the speed test job and resets transient running/progress flags. */
    private fun stopSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        _uiState.update { it.copy(speedTest = it.speedTest.stopped()) }
    }

    /** Reduces one speed-test domain event into presentation state. */
    private fun reduceSpeedTestUpdate(update: SpeedTestUpdate) {
        when (update) {
            SpeedTestUpdate.Started -> _uiState.update {
                it.copy(speedTest = SpeedTestUiState(running = true), errorMessage = null)
            }

            SpeedTestUpdate.Finished -> _uiState.update {
                it.copy(speedTest = it.speedTest.stopped())
            }

            is SpeedTestUpdate.Failed -> _uiState.update {
                it.copy(
                    speedTest = it.speedTest.stopped(),
                    errorMessage = update.message
                )
            }

            is SpeedTestUpdate.Progress -> _uiState.update { state ->
                state.copy(speedTest = state.speedTest.withProgress(update))
            }

            is SpeedTestUpdate.Latency -> _uiState.update { state ->
                state.copy(speedTest = state.speedTest.withLatency(update))
            }
        }
    }

    /** Adds a throughput sample to the matching phase while updating the gauge. */
    private fun SpeedTestUiState.withProgress(update: SpeedTestUpdate.Progress): SpeedTestUiState {
        val mbps = (update.bitsPerSecond / 1_000_000.0).toFloat()
        return when (update.phase) {
            SpeedTestPhase.DOWNLOAD -> copy(
                running = true,
                phase = update.phase,
                gaugeValue = mbps,
                progress = update.percent.coerceIn(0f, 100f) / 100f,
                download = download.add(mbps)
            )

            SpeedTestPhase.UPLOAD -> copy(
                running = true,
                phase = update.phase,
                gaugeValue = mbps,
                progress = update.percent.coerceIn(0f, 100f) / 100f,
                upload = upload.add(mbps)
            )

            SpeedTestPhase.IDLE,
            SpeedTestPhase.PING -> this
        }
    }

    /** Adds a latency sample and keeps the gauge idle during the ping phase. */
    private fun SpeedTestUiState.withLatency(update: SpeedTestUpdate.Latency): SpeedTestUiState {
        val milliseconds = update.milliseconds.toFloat()
        return copy(
            running = true,
            phase = SpeedTestPhase.PING,
            gaugeValue = 0f,
            progress = update.percent.coerceIn(0f, 100f) / 100f,
            ping = ping.add(milliseconds)
        )
    }

    /** Resets transient execution state while preserving collected metrics. */
    private fun SpeedTestUiState.stopped() = copy(
        running = false,
        phase = SpeedTestPhase.IDLE,
        gaugeValue = 0f,
        progress = 0f
    )
}
