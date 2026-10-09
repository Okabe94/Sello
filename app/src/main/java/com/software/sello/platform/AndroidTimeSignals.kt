package com.software.sello.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.software.sello.domain.port.DispatcherProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Android sources of a possibly changed date: the app returning to the foreground
 * and the system's time, date and time-zone broadcasts. Collecting registers the
 * receiver and lifecycle observer; cancelling the collector removes both.
 */
class AndroidTimeSignals(
    private val context: Context,
    private val dispatchers: DispatcherProvider
) : TimeSignals {
    override fun changes(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        val resumed = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(resumed)
        awaitClose {
            context.unregisterReceiver(receiver)
            lifecycle.removeObserver(resumed)
        }
    }.flowOn(dispatchers.main)
}
