package com.software.sello.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme

/** Catalog chrome: choose the ink, light/dark, font scale and window width. */
@Composable
fun CatalogControls(settings: CatalogSettings, onChange: (CatalogSettings) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val spacing = SelloTheme.spacing
    Surface(
        color = SelloTheme.colors.paperAlt,
        contentColor = SelloTheme.colors.ink,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = spacing.deskMargin)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = settingsSummary(settings),
                    style = MaterialTheme.typography.labelMedium,
                    color = SelloTheme.colors.inkSoft,
                    modifier = Modifier.weight(1f).testTag(SUMMARY_TAG)
                )
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag(CONTROLS_TOGGLE_TAG)
                ) {
                    Text(
                        stringResource(
                            if (expanded) {
                                R.string.catalog_controls_hide
                            } else {
                                R.string.catalog_controls_show
                            }
                        )
                    )
                }
            }
            if (expanded) {
                ControlRow(
                    label = stringResource(R.string.catalog_control_ink),
                    options = SelloInk.entries,
                    selected = settings.ink,
                    name = { it.name },
                    tag = { controlTag("ink", it.name) },
                    onSelect = { onChange(settings.copy(ink = it)) }
                )
                ControlRow(
                    label = stringResource(R.string.catalog_control_mode),
                    options = CatalogMode.entries,
                    selected = settings.mode,
                    name = { stringResource(it.label) },
                    tag = { controlTag("mode", it.name) },
                    onSelect = { onChange(settings.copy(mode = it)) }
                )
                ControlRow(
                    label = stringResource(R.string.catalog_control_font),
                    options = CatalogFontScale.entries,
                    selected = settings.fontScale,
                    name = { fontScaleLabel(it) },
                    tag = { controlTag("font", it.name) },
                    onSelect = { onChange(settings.copy(fontScale = it)) }
                )
                ControlRow(
                    label = stringResource(R.string.catalog_control_window),
                    options = CatalogWindow.entries,
                    selected = settings.window,
                    name = { stringResource(it.label) },
                    tag = { controlTag("window", it.name) },
                    onSelect = { onChange(settings.copy(window = it)) }
                )
                ControlRow(
                    label = stringResource(R.string.catalog_control_motion),
                    options = CatalogMotion.entries,
                    selected = settings.motion,
                    name = { stringResource(it.label) },
                    tag = { controlTag("motion", it.name) },
                    onSelect = { onChange(settings.copy(motion = it)) }
                )
            }
        }
    }
}

@Composable
private fun <T> ControlRow(
    label: String,
    options: List<T>,
    selected: T,
    name: @Composable (T) -> String,
    tag: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.chipGap),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SelloTheme.colors.inkSoft,
            modifier = Modifier.width(56.dp)
        )
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = {
                    Text(name(option), style = MaterialTheme.typography.bodyMedium)
                },
                modifier = Modifier.testTag(tag(option))
            )
        }
    }
}

@Composable
private fun settingsSummary(settings: CatalogSettings): String = listOf(
    settings.ink.name,
    stringResource(settings.mode.label),
    fontScaleLabel(settings.fontScale),
    stringResource(settings.window.label)
).joinToString(" · ")

private fun fontScaleLabel(scale: CatalogFontScale) = "${scale.scale}×"

const val SUMMARY_TAG = "catalog:summary"
const val CONTROLS_TOGGLE_TAG = "catalog:controls"

fun controlTag(control: String, option: String) = "control:$control:$option"
