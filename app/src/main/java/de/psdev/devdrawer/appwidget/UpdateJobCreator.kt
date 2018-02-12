package de.psdev.devdrawer.appwidget

import android.app.Application
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class UpdateJobCreator(private val application: Application): JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            UpdateJob.TAG -> UpdateJob(application)
            else -> null
        }
    }
}