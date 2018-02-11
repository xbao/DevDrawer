package com.owentech.devdrawer.appwidget

import android.content.Intent
import android.widget.RemoteViewsService

class DDWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return DDWidgetViewsFactory(applicationContext, intent)
    }
}