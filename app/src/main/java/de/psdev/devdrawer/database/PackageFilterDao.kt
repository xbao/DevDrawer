package de.psdev.devdrawer.database

import android.arch.persistence.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@Dao
abstract class PackageFilterDao {

    @Query("SELECT * FROM filters")
    abstract fun filters(): Flowable<List<PackageFilter>>

    @Insert
    abstract fun addFilter(vararg filters: PackageFilter)

    @Delete
    abstract fun delete(filter: PackageFilter)

    @Update
    abstract fun update(filter: PackageFilter)

    fun addFilterAsync(vararg filters: PackageFilter): Completable = Completable.fromAction { addFilter(*filters) }

    fun deleteAsync(filter: PackageFilter): Completable = Completable.fromAction { delete(filter) }.subscribeOn(Schedulers.io())

    fun updateFilter(id: Int, newFilter: String): Disposable {
        return Completable.fromAction { update(PackageFilter(id, newFilter)) }.subscribeOn(Schedulers.io()).subscribe()
    }

}