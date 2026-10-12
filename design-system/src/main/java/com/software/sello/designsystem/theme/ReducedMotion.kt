package com.software.sello.designsystem.theme

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * True when the system asks for no animations (animator duration scale 0, which is
 * what "Remove animations" sets). Components then show their end state at once.
 *
 * It follows the setting while whatever calls it is on screen: turning animations off
 * or on takes effect at once, without the app being started again. It listens for
 * the change; it does not poll and needs no permission.
 */
@Composable
fun systemReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return reducedMotion(remember(resolver) { SystemAnimatorScale(resolver) })
}

/** The system's animation setting: what it says now, and being told when it changes. */
internal interface AnimatorScale {
    fun isOff(): Boolean

    /** Calls [onChange] on every change until the returned handle is closed. */
    fun watch(onChange: () -> Unit): AutoCloseable
}

internal class SystemAnimatorScale(private val resolver: ContentResolver) : AnimatorScale {
    override fun isOff(): Boolean =
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    override fun watch(onChange: () -> Unit): AutoCloseable {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = onChange()
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        return AutoCloseable { resolver.unregisterContentObserver(observer) }
    }
}

/**
 * [scale] as state. One listener for as long as the caller stays in the composition,
 * however often it is drawn again, and none once it has left.
 */
@Composable
internal fun reducedMotion(scale: AnimatorScale): Boolean {
    var off by remember(scale) { mutableStateOf(scale.isOff()) }
    DisposableEffect(scale) {
        val watching = scale.watch { off = scale.isOff() }
        // Read again now that someone is listening: a change in between was not heard.
        off = scale.isOff()
        onDispose { watching.close() }
    }
    return off
}

internal val LocalSelloReducedMotion = staticCompositionLocalOf { false }
