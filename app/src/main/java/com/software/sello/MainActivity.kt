package com.software.sello

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.software.sello.composition.SelloApplication
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.navigation.SelloAppRoot
import com.software.sello.navigation.ShellAction
import com.software.sello.navigation.ShellViewModel

class MainActivity : ComponentActivity() {
    private val factory get() = (application as SelloApplication).viewModels
    private val shell: ShellViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh start carries a link to act on; a restored one already did.
        if (savedInstanceState == null) open(intent)
        setContent {
            SelloTheme { SelloAppRoot(factory) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        open(intent)
    }

    /** A link only ever becomes a request to prefill a draft; see `EntryLinks`. */
    private fun open(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            shell.onAction(ShellAction.OpenLink(intent.dataString))
        }
    }
}
