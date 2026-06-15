package cz.kaboom.connectioninfo.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.kaboom.connectioninfo.R
import cz.kaboom.connectioninfo.feature.main.ConnectionInfoApp
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme

/**
 * Hosts the single Compose surface for the app.
 *
 * The activity starts with `AppLaunchTheme` from the manifest so Android can render the branded
 * splash screen, then switches to `AppTheme` before composing the real UI.
 */
class MainActivity : ComponentActivity() {

    /** Presentation layer entry point scoped to this activity. */
    private val viewModel: MainActivityViewModel by viewModels()

    /** Creates the Compose tree and binds it to the lifecycle-aware UI state flow. */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

        setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle()

            ConnectionInfoTheme {
                ConnectionInfoApp(
                    state = state.value,
                    versionName = versionName,
                    onAction = viewModel::onAction
                )
            }
        }
    }
}
