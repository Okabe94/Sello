package com.software.sello.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

@Composable
fun CatalogApp() {
    var settings by rememberSaveable(stateSaver = CatalogSettings.Saver) {
        mutableStateOf(CatalogSettings())
    }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val open = catalogExamples.firstOrNull { it.id == openId }
    val dark = when (settings.mode) {
        CatalogMode.System -> isSystemInDarkTheme()
        CatalogMode.Light -> false
        CatalogMode.Dark -> true
    }

    BackHandler(enabled = open != null) { openId = null }

    SelloTheme(
        ink = settings.ink,
        darkTheme = dark,
        reducedMotion = settings.motion == CatalogMotion.Reduced
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SelloTheme.colors.desk
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                CatalogControls(settings = settings, onChange = { settings = it })
                CatalogStage(settings = settings) {
                    if (open == null) {
                        CatalogHome(onOpen = { openId = it.id })
                    } else {
                        CatalogExampleScreen(example = open, onBack = { openId = null })
                    }
                }
            }
        }
    }
}

/** Applies the chosen font scale and width to the examples only, not to the controls. */
@Composable
private fun CatalogStage(settings: CatalogSettings, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scaled = Density(density.density, settings.fontScale.scale)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = settings.window.width
        val stage: @Composable (Modifier) -> Unit = { modifier ->
            Box(modifier = modifier.fillMaxHeight().testTag(STAGE_TAG)) {
                CompositionLocalProvider(LocalDensity provides scaled, content = content)
            }
        }
        if (width == null || width <= maxWidth) {
            stage(if (width == null) Modifier.fillMaxWidth() else Modifier.widthIn(max = width))
        } else {
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                stage(Modifier.requiredWidth(width))
            }
        }
    }
}

@Composable
private fun CatalogHome(onOpen: (CatalogExample) -> Unit) {
    val spacing = SelloTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.deskMargin),
        verticalArrangement = Arrangement.spacedBy(spacing.slipGap)
    ) {
        Text(
            text = stringResource(R.string.catalog_name),
            style = MaterialTheme.typography.headlineSmall,
            color = SelloTheme.colors.ink,
            modifier = Modifier.semantics { heading() }
        )
        // Only groups that already have examples are listed.
        CatalogGroup.entries.forEach { group ->
            val examples = catalogExamples.filter { it.group == group }
            if (examples.isNotEmpty()) {
                CatalogGroupCard(group = group, examples = examples, onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun CatalogGroupCard(
    group: CatalogGroup,
    examples: List<CatalogExample>,
    onOpen: (CatalogExample) -> Unit
) {
    val spacing = SelloTheme.spacing
    Surface(
        color = SelloTheme.colors.paper,
        contentColor = SelloTheme.colors.ink,
        shape = SelloTheme.shapes.control,
        modifier = Modifier.fillMaxWidth().testTag(groupTag(group))
    ) {
        Column(modifier = Modifier.padding(vertical = spacing.md)) {
            Text(
                text = stringResource(group.title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = spacing.slipPadding)
                    .semantics { heading() }
            )
            examples.forEach { example ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = spacing.minTouchTarget)
                        .clickable { onOpen(example) }
                        .testTag(exampleLinkTag(example.id))
                        .padding(horizontal = spacing.slipPadding)
                ) {
                    Text(
                        text = stringResource(example.title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogExampleScreen(example: CatalogExample, onBack: () -> Unit) {
    val spacing = SelloTheme.spacing
    Column(modifier = Modifier.fillMaxSize().testTag(exampleTag(example.id))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag(BACK_TAG)) {
                Icon(
                    painter = SelloIcon.ArrowBack.painter(),
                    contentDescription = stringResource(R.string.catalog_back),
                    tint = SelloTheme.colors.brand
                )
            }
            Text(
                text = stringResource(example.title),
                style = MaterialTheme.typography.titleLarge,
                color = SelloTheme.colors.ink,
                modifier = Modifier.semantics { heading() }
            )
        }
        Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.deskMargin)
                .padding(bottom = spacing.xl)
        ) {
            example.content()
        }
    }
}

const val STAGE_TAG = "catalog:stage"
const val BACK_TAG = "catalog:back"

fun groupTag(group: CatalogGroup) = "group:${group.id}"

fun exampleLinkTag(id: String) = "link:$id"

fun exampleTag(id: String) = "example:$id"
