package com.owentech.devdrawer

import android.arch.persistence.room.Room
import android.support.multidex.MultiDexApplication
import com.owentech.devdrawer.database.DevDrawerDatabase

class DevDrawerApplication: MultiDexApplication() {

    val devDrawerDatabase: DevDrawerDatabase by lazy { Room.databaseBuilder(this, DevDrawerDatabase::class.java, DevDrawerDatabase.NAME).build() }

}