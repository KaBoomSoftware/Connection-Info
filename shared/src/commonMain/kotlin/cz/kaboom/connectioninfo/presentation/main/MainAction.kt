package cz.kaboom.connectioninfo.presentation.main

/** User and lifecycle actions accepted by [MainPresenter]. */
sealed interface MainAction {
    /** Selects one of the top-level tabs. */
    data class SelectTab(val tab: MainTab) : MainAction

    /** Starts or cancels the speed test depending on the current state. */
    data object ToggleSpeedTest : MainAction

    /** Requests a manual network info refresh. */
    data object RefreshNetworkInfo : MainAction
}
