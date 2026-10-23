package com.software.sello

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.software.sello.composition.SelloApplication
import com.software.sello.composition.StartupState
import com.software.sello.composition.startupFailureViewModels
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.feature.settings.AppearanceViewModel
import com.software.sello.feature.settings.asSelloTheme
import com.software.sello.feature.startup.StartupFailureRoot
import com.software.sello.navigation.SelloAppRoot
import com.software.sello.navigation.ShellAction
import com.software.sello.navigation.ShellViewModel
import com.software.sello.platform.ProcessAppRestart

class MainActivity : ComponentActivity() {
    private val startup get() = (application as SelloApplication).startup

    /** Set once the unreadable data was set aside and this activity is being replaced. */
    private var startingOver by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start carries a link to act on; a restored one already did.
        if (savedInstanceState == null) open(intent)
        setContent {
            val state by startup.state.collectAsStateWithLifecycle()
            when (val now = state) {
                // The app is shown only on data that opened. Recomposition changes its
                // tokens without replacing any saved route, draft or financial view model.
                is StartupState.Ready -> {
                    val appearance: AppearanceViewModel = viewModel(factory = now.viewModels)
                    val preference by appearance.state.collectAsStateWithLifecycle()
                    val theme = preference.appearance.asSelloTheme(isSystemInDarkTheme())
                    SelloTheme(ink = theme.ink, darkTheme = theme.dark) {
                        if (!startingOver) SelloAppRoot(now.viewModels)
                    }
                }

                // The graph cannot be opened here, so its persisted preference is unavailable.
                is StartupState.Failed -> SelloTheme {
                    StartupFailureRoot(
                        viewModels = remember {
                            startupFailureViewModels(startup, ProcessAppRestart(applicationContext))
                        },
                        onStartedOver = ::startOver
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        open(intent)
    }

    /**
     * A link only ever becomes a request to prefill a draft; see `EntryLinks`. Without
     * data there is no draft to prefill, so the link is dropped.
     */
    private fun open(intent: Intent?) {
        val ready = startup.state.value as? StartupState.Ready ?: return
        if (intent?.action == Intent.ACTION_VIEW) {
            ViewModelProvider(this, ready.viewModels)[ShellViewModel::class.java]
                .onAction(ShellAction.OpenLink(intent.dataString))
        }
    }

    /**
     * The unreadable data was set aside, so opening now starts an empty history. The
     * app is shown in a new task: whatever this one had saved (a selected category, a
     * draft) belongs to the history that is gone and must not be restored onto the new.
     */
    private fun startOver() {
        startingOver = true
        startup.retry()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
