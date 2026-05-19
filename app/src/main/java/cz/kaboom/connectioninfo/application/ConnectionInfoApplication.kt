@file:Suppress("unused")
package cz.kaboom.connectioninfo.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application root used by Hilt to create the dependency graph before activities start.
 */
@HiltAndroidApp
class ConnectionInfoApplication : Application()
