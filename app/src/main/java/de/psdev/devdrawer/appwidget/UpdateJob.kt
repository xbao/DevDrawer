package de.psdev.devdrawer.appwidget

import android.app.Application
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import de.psdev.devdrawer.receivers.UpdateReceiver
import mu.KLogging
import java.util.concurrent.TimeUnit

class UpdateJob(private val application: Application): Job() {
    companion object: KLogging() {
        @JvmField
        val TAG: String = UpdateJob::class.java.simpleName

        @JvmStatic
        fun scheduleJob() = JobRequest.Builder(UpdateJob.TAG)
            .setPeriodic(TimeUnit.HOURS.toMillis(1))
            .build()
            .schedule()
    }

    override fun onRunJob(params: Params): Result {
        logger.debug { "Run update job" }
        UpdateReceiver.send(application)
        return Result.SUCCESS
    }
}