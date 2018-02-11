package de.psdev.devdrawer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.psdev.devdrawer.utils.Constants

class AppInstalledReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> context.sendBroadcast(Intent(Constants.ACTION_REFRESH_APPS).apply {
                setPackage(context.packageName)
            })
        }
    }
}
