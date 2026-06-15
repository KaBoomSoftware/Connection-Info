package cz.kaboom.connectioninfo.presentation.main

import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails

/** Immutable state consumed by the Compose main screen. */
data class MainUiState(
    /** Currently selected tab. */
    val selectedTab: MainTab = MainTab.SPEED_TEST,

    /** True when Android reports validated internet connectivity. */
    val internetAvailable: Boolean = false,

    /** Latest network information snapshot, or null before the first successful refresh. */
    val networkInfo: NetworkDetails? = null,

    /** Current speed test presentation state. */
    val speedTest: SpeedTestUiState = SpeedTestUiState(),

    /** Last user-facing error emitted by repositories. */
    val errorMessage: String? = null
)
