package de.psdev.devdrawer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.psdev.devdrawer.utils.Constants
import mu.KLogging

class AppInstallationReceiver: BroadcastReceiver() {

    companion object: KLogging()

    override fun onReceive(context: Context, intent: Intent) {
        logger.warn { "onReceive[context=$context, intent=$intent]" }
        context.sendBroadcast(Intent(Constants.ACTION_REFRESH_APPS).apply {
            setPackage(context.packageName)
        })
    }
}
