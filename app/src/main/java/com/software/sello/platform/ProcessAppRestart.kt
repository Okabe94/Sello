package com.software.sello.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import com.software.sello.MainActivity
import com.software.sello.domain.port.AppRestart

/**
 * Starts Sello again from Recibo in a new task and a new process. After a restore the
 * history, its generation and its financial zone all changed; a fresh process reads
 * them from storage instead of keeping any screen, draft or zone from before.
 */
class ProcessAppRestart(private val context: Context) : AppRestart {
    override fun restart() {
        val main = Intent.makeRestartActivityTask(ComponentName(context, MainActivity::class.java))
        context.startActivity(main)
        Process.killProcess(Process.myPid())
    }
}
