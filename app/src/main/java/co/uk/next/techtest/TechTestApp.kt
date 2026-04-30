package co.uk.next.techtest

import android.app.Application
import co.uk.next.techtest.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TechTestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TechTestApp)
            modules(appModule)
        }
    }
}

