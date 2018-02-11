package de.psdev.devdrawer

import android.arch.persistence.room.Room
import android.support.multidex.MultiDexApplication
import com.squareup.leakcanary.LeakCanary
import de.psdev.devdrawer.database.DevDrawerDatabase
import mu.KLogging
import kotlin.system.measureTimeMillis

class DevDrawerApplication: MultiDexApplication() {

    companion object: KLogging()

    val devDrawerDatabase: DevDrawerDatabase by lazy { Room.databaseBuilder(this, DevDrawerDatabase::class.java, DevDrawerDatabase.NAME).build() }

    override fun onCreate() {
        super.onCreate()
        measureTimeMillis {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }
            LeakCanary.install(this)
        }.let {
            logger.warn("{} version {} ({}) took {}ms to init", this::class.java.simpleName, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, it)
        }
    }

}