package de.psdev.devdrawer.database

import android.arch.persistence.room.*
import android.support.annotation.ColorInt
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@Dao
abstract class WidgetConfigDao {

    @Query("SELECT * FROM widgets")
    abstract fun widgets(): Flowable<List<WidgetConfig>>

    @Query("SELECT * FROM widgets WHERE widgetId=:widgetId")
    abstract fun widgets(widgetId: Int): Flowable<List<WidgetConfig>>

    @Query("DELETE FROM widgets WHERE widgetId=:widgetId")
    abstract fun delete(widgetId: Int): Int

    @Insert
    abstract fun addWidget(vararg widget: WidgetConfig)

    @Delete
    abstract fun delete(widget: WidgetConfig)

    @Update
    abstract fun update(widget: WidgetConfig)

    fun addWidgetAsync(vararg widgets: WidgetConfig): Completable = Completable.fromAction { addWidget(*widgets) }

    fun deleteAsync(widget: WidgetConfig): Completable = Completable.fromAction { delete(widget) }.subscribeOn(Schedulers.io())

    fun updateWidget(id: Int, name: String, widgetId: Int, @ColorInt color: Int): Disposable {
        return Completable.fromAction { update(WidgetConfig(id, name, widgetId, color)) }.subscribeOn(Schedulers.io()).subscribe()
    }
}
