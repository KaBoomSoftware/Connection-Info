package cz.kaboom.connectioninfo.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.kaboom.connectioninfo.presentation.main.MainAction
import cz.kaboom.connectioninfo.presentation.main.MainPresenter
import cz.kaboom.connectioninfo.presentation.main.createMainPresenter

/** Android lifecycle wrapper around the shared multiplatform presenter. */
class MainActivityViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val presenter: MainPresenter = createMainPresenter(application)

    /** Shared immutable state consumed by Compose. */
    val uiState = presenter.uiState

    /** Forwards Android UI intents into the shared reducer. */
    fun onAction(action: MainAction) {
        presenter.onAction(action)
    }

    override fun onCleared() {
        presenter.close()
        super.onCleared()
    }
}
