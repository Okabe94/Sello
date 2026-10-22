package com.software.sello.devtools

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.software.sello.R
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.theme.SelloTheme
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.launch

/**
 * The debug build's tools: which data is in use, the sandbox's financial date and its
 * reset. It has its own launcher and is absent from release builds.
 */
class ToolsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tools = SandboxRegistry.tools
        setContent {
            SelloTheme {
                if (tools != null) ToolsScreen(tools, onShare = ::share)
            }
        }
    }

    /** The diagnostic leaves only through the share sheet the developer opens. */
    private fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, getString(R.string.tools_export)))
    }
}

@Composable
fun ToolsScreen(tools: SandboxTools, onShare: (String) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var moving by remember { mutableStateOf(false) }
    fun move(action: suspend SandboxProfiles.() -> Unit) {
        if (moving) return
        moving = true
        scope.launch { tools.profiles.action() }
    }
    SelloScaffold(title = stringResource(R.string.tools_title)) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileSlip(tools, moving, onEnter = {
                move { enter() }
            }, onLeave = { move { leave() } })
            tools.time?.let { TimeSlip(it) }
            ScenarioSlip(tools.scenarios, enabled = !moving, onLoad = { move { load(it) } })
            tools.diagnostics?.let {
                FaultSlip(it.faults)
                OperationsSlip(it, onShare)
            }
            if (tools.profiles.inSandbox) {
                ResetSlip(enabled = !moving, onReset = {
                    move { reset() }
                })
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun Note(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = SelloTheme.colors.inkSoft,
        modifier = modifier
    )
}

@Composable
private fun ProfileSlip(
    tools: SandboxTools,
    moving: Boolean,
    onEnter: () -> Unit,
    onLeave: () -> Unit
) {
    val inSandbox = tools.profiles.inSandbox
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_profile_heading))
        Text(
            stringResource(
                if (inSandbox) R.string.tools_profile_sandbox else R.string.tools_profile_normal
            ),
            modifier = Modifier.testTag(TOOLS_PROFILE_TAG)
        )
        if (tools.time == null && inSandbox) Note(stringResource(R.string.tools_profile_unopened))
        if (moving) {
            Note(
                stringResource(R.string.tools_moving),
                Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        SelloButton(
            text = stringResource(if (inSandbox) R.string.tools_leave else R.string.tools_enter),
            onClick = if (inSandbox) onLeave else onEnter,
            loading = moving,
            modifier = Modifier.fillMaxWidth().testTag(TOOLS_SWITCH_TAG)
        )
    }
}

@Composable
private fun TimeSlip(time: SandboxTime) {
    val state by time.state.collectAsState()
    var typed by rememberSaveable { mutableStateOf("") }
    var problem by rememberSaveable { mutableStateOf<Int?>(null) }
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_date_heading))
        LeaderLine(
            stringResource(R.string.tools_date),
            spokenDate(state.day.date),
            Modifier.testTag(TOOLS_DATE_TAG)
        )
        LeaderLine(stringResource(R.string.tools_zone), state.day.zone.id)
        Note(stringResource(R.string.tools_real_time_note))
        if (state.changed) Note(stringResource(R.string.tools_forward_only))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
            SelloButton(
                stringResource(R.string.tools_plus_day),
                { time.advanceDays(1) },
                Modifier.testTag(TOOLS_PLUS_DAY_TAG),
                kind = ButtonKind.Outlined
            )
            SelloButton(
                stringResource(R.string.tools_plus_month),
                { time.advanceMonths(1) },
                Modifier.testTag(TOOLS_PLUS_MONTH_TAG),
                kind = ButtonKind.Outlined
            )
        }
        FormField(
            label = stringResource(R.string.tools_jump_label),
            value = typed,
            onValueChange = {
                typed = it
                problem = null
            },
            message = problem?.let { FieldMessage(stringResource(it)) },
            modifier = Modifier.testTag(TOOLS_JUMP_FIELD_TAG)
        )
        SelloButton(
            stringResource(R.string.tools_jump),
            {
                problem = try {
                    when (time.jumpTo(LocalDate.parse(typed.trim()))) {
                        DateChange.Moved -> null
                        DateChange.RefusedBeforeSavedHistory -> R.string.tools_jump_refused
                    }
                } catch (_: DateTimeParseException) {
                    R.string.tools_jump_invalid
                }
            },
            Modifier.fillMaxWidth().testTag(TOOLS_JUMP_TAG),
            kind = ButtonKind.Outlined,
            enabled = typed.isNotBlank()
        )
        if (state.playing) {
            Note(
                pluralStringResource(R.plurals.tools_playing, state.stepsLeft, state.stepsLeft),
                Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        SelloButton(
            stringResource(if (state.playing) R.string.tools_pause else R.string.tools_play),
            { if (state.playing) time.pause() else time.play() },
            Modifier.fillMaxWidth().testTag(TOOLS_PLAY_TAG)
        )
    }
}

@Composable
private fun ScenarioSlip(runner: ScenarioRunner?, enabled: Boolean, onLoad: (Scenario) -> Unit) {
    var loading by rememberSaveable { mutableStateOf<String?>(null) }
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_scenarios_heading))
        runner?.let { ScenarioProgress(it) }
        Note(stringResource(R.string.tools_scenarios_note))
        Scenarios.all.forEach { scenario ->
            Text(scenario.title, style = MaterialTheme.typography.titleSmall)
            Note(
                "${scenario.summary} ${scenario.steps.size} pasos · ${scenario.key} · semilla ${scenario.seed}"
            )
            if (loading == scenario.key) {
                SelloButton(
                    stringResource(R.string.tools_scenario_load_confirm),
                    { onLoad(scenario) },
                    Modifier.fillMaxWidth().testTag(scenarioTag(scenario, "confirm")),
                    tone = ButtonTone.Loss,
                    enabled = enabled
                )
                SelloButton(
                    stringResource(R.string.tools_reset_cancel),
                    { loading = null },
                    Modifier.fillMaxWidth(),
                    kind = ButtonKind.Text
                )
            } else {
                SelloButton(
                    stringResource(R.string.tools_scenario_load),
                    { loading = scenario.key },
                    Modifier.fillMaxWidth().testTag(scenarioTag(scenario, "load")),
                    kind = ButtonKind.Outlined,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun ScenarioProgress(runner: ScenarioRunner) {
    val state by runner.state.collectAsState()
    val scenario = state.scenario ?: return
    val scope = rememberCoroutineScope()
    Text(scenario.title, style = MaterialTheme.typography.titleSmall)
    Text(
        if (state.finished) {
            pluralStringResource(R.plurals.tools_scenario_finished, state.total, state.total)
        } else {
            stringResource(R.string.tools_scenario_progress, state.next + 1, state.total)
        },
        modifier = Modifier
            .testTag(TOOLS_SCENARIO_PROGRESS_TAG)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
    state.problem?.let {
        Text(
            it,
            color = SelloTheme.colors.semantic.loss,
            modifier = Modifier.testTag(TOOLS_SCENARIO_PROBLEM_TAG)
        )
    }
    state.log.takeLast(LOG_LINES).forEach { record ->
        Note(
            "${record.index + 1}. ${record.action} · ${record.result}" +
                (record.revision?.let { " · rev $it" } ?: "") +
                (record.operation?.let { " · $it" } ?: "")
        )
    }
    if (!state.finished) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
            SelloButton(
                stringResource(R.string.tools_scenario_step),
                { scope.launch { runner.step() } },
                Modifier.testTag(TOOLS_SCENARIO_STEP_TAG),
                kind = ButtonKind.Outlined,
                enabled = !state.playing && !state.running
            )
            SelloButton(
                stringResource(
                    if (state.playing) R.string.tools_pause else R.string.tools_scenario_play
                ),
                { if (state.playing) runner.pause() else runner.play() },
                Modifier.testTag(TOOLS_SCENARIO_PLAY_TAG)
            )
        }
    }
}

private const val LOG_LINES = 6

fun scenarioTag(scenario: Scenario, action: String) = "tools:scenario:${scenario.id}:$action"

@Composable
private fun FaultSlip(faults: DebugFaults) {
    val armed by faults.state.collectAsState()
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_faults_heading))
        Note(stringResource(R.string.tools_faults_note))
        Fault.entries.forEach { fault ->
            val left = armed[fault] ?: 0
            SelloButton(
                if (left > 0) "${fault.label} · $left" else fault.label,
                { faults.arm(fault) },
                Modifier.fillMaxWidth().testTag(faultTag(fault)),
                kind = ButtonKind.Outlined
            )
        }
        SelloButton(
            stringResource(R.string.tools_faults_clear),
            faults::clear,
            Modifier.fillMaxWidth().testTag(TOOLS_FAULTS_CLEAR_TAG),
            kind = ButtonKind.Text,
            enabled = armed.isNotEmpty()
        )
    }
}

@Composable
private fun OperationsSlip(diagnostics: SandboxDiagnostics, onShare: (String) -> Unit) {
    val logged by diagnostics.log.state.collectAsState()
    val scope = rememberCoroutineScope()
    var stored by remember { mutableStateOf<Map<LoggedOperation, StoredOutcome>>(emptyMap()) }
    suspend fun check() = logged.associateWith { diagnostics.inspector.stored(it) }
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_operations_heading))
        if (logged.isEmpty()) Note(stringResource(R.string.tools_operations_none))
        logged.takeLast(LOG_LINES).forEach { entry ->
            val inStorage = when (val found = stored[entry]) {
                null -> "sin comprobar"
                is StoredOutcome.Saved -> "guardado, rev ${found.revision}"
                StoredOutcome.Nothing -> "nada escrito"
                StoredOutcome.Unreadable -> "no se pudo leer"
            }
            Note(
                "${entry.kind} · se le dijo: ${entry.told}" +
                    (entry.fault?.let { " (simulado: ${it.label})" } ?: "") +
                    " · almacenamiento: $inStorage · ${entry.operation.value}",
                Modifier.testTag(TOOLS_OPERATION_TAG)
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
            SelloButton(
                stringResource(R.string.tools_operations_check),
                { scope.launch { stored = check() } },
                Modifier.testTag(TOOLS_OPERATIONS_CHECK_TAG),
                kind = ButtonKind.Outlined,
                enabled = logged.isNotEmpty()
            )
            SelloButton(
                stringResource(R.string.tools_export),
                {
                    scope.launch {
                        val checked = check()
                        stored = checked
                        onShare(
                            diagnosticText(
                                diagnostics.session(),
                                diagnostics.faults.state.value,
                                logged.map { it to checked.getValue(it) }
                            )
                        )
                    }
                },
                Modifier.testTag(TOOLS_EXPORT_TAG),
                kind = ButtonKind.Outlined
            )
        }
    }
}

fun faultTag(fault: Fault) = "tools:fault:${fault.name}"

@Composable
private fun ResetSlip(enabled: Boolean, onReset: () -> Unit) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    Slip(Modifier.fillMaxWidth()) {
        Heading(stringResource(R.string.tools_reset_heading))
        Note(stringResource(R.string.tools_reset_note))
        if (confirming) {
            SelloButton(
                stringResource(R.string.tools_reset_confirm),
                onReset,
                Modifier.fillMaxWidth().testTag(TOOLS_RESET_CONFIRM_TAG),
                tone = ButtonTone.Loss,
                enabled = enabled
            )
            SelloButton(
                stringResource(R.string.tools_reset_cancel),
                { confirming = false },
                Modifier.fillMaxWidth(),
                kind = ButtonKind.Text
            )
        } else {
            SelloButton(
                stringResource(R.string.tools_reset),
                { confirming = true },
                Modifier.fillMaxWidth().testTag(TOOLS_RESET_TAG),
                kind = ButtonKind.Outlined,
                enabled = enabled
            )
        }
    }
}

const val TOOLS_PROFILE_TAG = "tools:profile"
const val TOOLS_SWITCH_TAG = "tools:switch"
const val TOOLS_DATE_TAG = "tools:date"
const val TOOLS_PLUS_DAY_TAG = "tools:plus-day"
const val TOOLS_PLUS_MONTH_TAG = "tools:plus-month"
const val TOOLS_JUMP_FIELD_TAG = "tools:jump-field"
const val TOOLS_JUMP_TAG = "tools:jump"
const val TOOLS_PLAY_TAG = "tools:play"
const val TOOLS_RESET_TAG = "tools:reset"
const val TOOLS_RESET_CONFIRM_TAG = "tools:reset-confirm"
const val TOOLS_SCENARIO_PROGRESS_TAG = "tools:scenario-progress"
const val TOOLS_SCENARIO_PROBLEM_TAG = "tools:scenario-problem"
const val TOOLS_SCENARIO_STEP_TAG = "tools:scenario-step"
const val TOOLS_SCENARIO_PLAY_TAG = "tools:scenario-play"
const val TOOLS_FAULTS_CLEAR_TAG = "tools:faults-clear"
const val TOOLS_OPERATION_TAG = "tools:operation"
const val TOOLS_OPERATIONS_CHECK_TAG = "tools:operations-check"
const val TOOLS_EXPORT_TAG = "tools:export"
