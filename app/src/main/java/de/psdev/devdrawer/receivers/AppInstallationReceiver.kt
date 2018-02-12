package de.psdev.devdrawer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mu.KLogging

class AppInstallationReceiver: BroadcastReceiver() {

    companion object: KLogging()

    override fun onReceive(context: Context, intent: Intent) {
        logger.warn { "onReceive[context=$context, intent=$intent]" }
        UpdateReceiver.send(context)
    }
}
