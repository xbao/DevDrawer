package com.owentech.devdrawer.utils

import android.content.Intent
import android.content.pm.PackageManager
import java.text.Collator

/**
 * Method to get all apps installed and return as List
 */
fun PackageManager.getExistingPackages(): List<String> {
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = queryIntentActivities(intent, 0)

    val appSet = mutableSetOf<String>()

    activities.forEach { resolveInfo ->
        var appName = resolveInfo.activityInfo.applicationInfo.packageName
        appSet.add(appName)
        while (appName.isNotEmpty()) {
            val lastIndex = appName.lastIndexOf(".")
            if (lastIndex > 0) {
                appName = appName.substring(0, lastIndex)
                appSet.add(appName + ".*")
            } else {
                appName = ""
            }
        }
    }

    return appSet.toList().sortedWith(Collator.getInstance())
}