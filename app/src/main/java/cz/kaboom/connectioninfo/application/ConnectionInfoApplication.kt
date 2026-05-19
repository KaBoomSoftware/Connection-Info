@file:Suppress("unused")
package cz.kaboom.connectioninfo.application

import android.app.Application
import cz.kaboom.connectioninfo.di.modules.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ConnectionInfoApplication : Application() {
    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@ConnectionInfoApplication)
            modules(mainKoinModule)
        }
    }
}