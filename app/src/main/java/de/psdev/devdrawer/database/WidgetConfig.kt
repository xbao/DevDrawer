package de.psdev.devdrawer.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetConfig(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    val id: Int = 0,
    @ColumnInfo(name = "name", typeAffinity = ColumnInfo.TEXT)
    val name: String,
    @ColumnInfo(name = "widgetId", typeAffinity = ColumnInfo.INTEGER)
    val widgetId: Int,
    @ColumnInfo(name = "color", typeAffinity = ColumnInfo.INTEGER)
    val color: Int)
