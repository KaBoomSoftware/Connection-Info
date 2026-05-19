package cz.kaboom.connectioninfo.presentation.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that replaces [Dispatchers.Main] with a test dispatcher for ViewModel tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    /** Dispatcher installed as Main for the lifetime of each test. */
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    /** Installs the test dispatcher before the test body runs. */
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    /** Restores the real Main dispatcher after the test completes. */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
