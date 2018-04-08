package de.psdev.devdrawer.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [PackageFilter::class, WidgetConfig::class], version = DevDrawerDatabase.VERSION)
abstract class DevDrawerDatabase: RoomDatabase() {

    companion object {
        const val NAME = "DevDrawer.db"
        const val VERSION = 1
    }

    abstract fun packageFilterDao(): PackageFilterDao

    abstract fun widgetConfigDao(): WidgetConfigDao
}