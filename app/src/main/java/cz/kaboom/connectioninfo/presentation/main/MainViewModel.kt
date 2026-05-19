package cz.kaboom.connectioninfo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.ConnectivityObserver
import cz.kaboom.connectioninfo.domain.repository.NetworkInfoRepository
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val connectivityObserver: ConnectivityObserver,
    private val networkInfoRepository: NetworkInfoRepository,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var speedTestJob: Job? = null
    private var networkRefreshJob: Job? = null

    init {
        observeConnectivity()
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SelectTab -> _uiState.update { it.copy(selectedTab = action.tab) }
            MainAction.ToggleSpeedTest -> toggleSpeedTest()
            MainAction.RefreshNetworkInfo -> refreshNetworkInfo()
        }
    }

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

    private fun toggleSpeedTest() {
        if (speedTestJob?.isActive == true) {
            stopSpeedTest()
        } else if (_uiState.value.internetAvailable) {
            startSpeedTest()
        }
    }

    private fun startSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            speedTestRepository.runSpeedTest().collect { update ->
                reduceSpeedTestUpdate(update)
            }
        }
    }

    private fun stopSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = null
        _uiState.update { it.copy(speedTest = it.speedTest.stopped()) }
    }

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
        }
    }

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

            SpeedTestPhase.IDLE -> this
        }
    }

    private fun SpeedTestUiState.stopped() = copy(
        running = false,
        phase = SpeedTestPhase.IDLE,
        gaugeValue = 0f,
        progress = 0f
    )
}
