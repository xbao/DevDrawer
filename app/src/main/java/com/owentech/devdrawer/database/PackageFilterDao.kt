package com.owentech.devdrawer.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

@Dao
abstract class PackageFilterDao {

    @Query("SELECT * FROM filters")
    abstract fun filters(): Flowable<List<PackageFilter>>

    @Insert
    abstract fun addFilter(vararg filters: PackageFilter)

    @Delete
    abstract fun delete(filter: PackageFilter)

    fun addFilterAsync(vararg filters: PackageFilter): Completable = Completable.fromAction { addFilter(*filters) }

    fun deleteAsync(filter: PackageFilter): Completable = Completable.fromAction { delete(filter) }.subscribeOn(Schedulers.io())

}