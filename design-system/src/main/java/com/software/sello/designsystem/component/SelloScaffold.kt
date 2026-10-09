package com.software.sello.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.theme.SelloTheme

/** How far the band colour reaches below the status bar: 0, 100dp or 132dp. */
enum class Blotter(val height: Dp) { None(0.dp), Short(100.dp), Tall(132.dp) }

/**
 * The frame of a screen: top bar, content on the desk, and the app's navigation and
 * entry action arranged for the window it is given.
 *
 * In an upright phone or a medium window the [dock] and tab bar sit under the content,
 * never over it. In a short or expanded window a rail at the leading edge carries the
 * [navigation] and the dock's "+", and [secondaryPane], if given, sits beside [content];
 * each pane scrolls on its own. Both bottom controls step aside for the system keyboard.
 * The scaffold holds no state: selection, the month and what a pane shows are the app's.
 *
 * @param blotterScroll how far, in pixels, the content has scrolled. The band shrinks
 * by that much; the top bar keeps the band colour until the blotter is gone.
 * @param windowInsets the system areas to keep clear. The paddings passed to [content]
 * and [secondaryPane] already include what they must avoid.
 */
@Composable
fun SelloScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
    blotter: Blotter = Blotter.None,
    blotterScroll: () -> Int = { 0 },
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    navigation: SelloNavigation? = null,
    dock: SelloDock? = null,
    windowInsets: WindowInsets = WindowInsets.safeDrawing,
    secondaryPane: (@Composable (PaddingValues) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val colors = SelloTheme.colors
    val density = LocalDensity.current
    val keyboardOpen = WindowInsets.ime.getBottom(density) > 0
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(colors.desk)) {
        val layout = selloWindowLayout(maxWidth, maxHeight)
        val rail = layout.useRail && navigation != null
        val bottomControls = !rail && !keyboardOpen
        val showBar = bottomControls && navigation != null
        val showDock = bottomControls && dock != null

        val scrolled = with(density) { blotterScroll().toDp() }
        val bandLeft = blotter.height - scrolled
        val onBand = blotter != Blotter.None && bandLeft > 0.dp
        if (onBand) {
            val statusBar = windowInsets.asPaddingValues().calculateTopPadding()
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(statusBar + maxOf(bandLeft, TopBarHeight))
                    .background(colors.band)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (rail) {
                SelloNavigationRail(
                    navigation = navigation,
                    dock = dock,
                    modifier = Modifier
                        .testTag(SCAFFOLD_RAIL_TAG)
                        .background(colors.paper)
                        .windowInsetsPadding(
                            windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical)
                        )
                )
            }
            // Beside a rail, the rail already keeps the leading edge clear.
            val topBarSides = WindowInsetsSides.Top +
                if (rail) WindowInsetsSides.End else WindowInsetsSides.Horizontal
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CompositionLocalProvider(
                    LocalContentColor provides if (onBand) colors.onBand else colors.ink
                ) {
                    TopBar(
                        title = title,
                        subtitle = subtitle,
                        titleContent = titleContent,
                        navigationIcon = navigationIcon,
                        actions = actions,
                        modifier = Modifier.windowInsetsPadding(windowInsets.only(topBarSides))
                    )
                }
                // Without bottom controls the content itself must clear the system bar.
                val bottomInset = if (showBar || showDock) {
                    0.dp
                } else {
                    windowInsets.asPaddingValues().calculateBottomPadding()
                }
                val padding = PaddingValues(
                    start = ContentMargin,
                    end = ContentMargin,
                    top = 0.dp,
                    bottom = ContentMargin + bottomInset
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (layout.twoPane && secondaryPane != null) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            val first = layout.listPaneWidth
                                ?.let { Modifier.width(it) } ?: Modifier.weight(1f)
                            Box(first.fillMaxHeight().testTag(SCAFFOLD_PRIMARY_TAG)) {
                                content(padding)
                            }
                            Box(
                                Modifier.weight(1f).fillMaxHeight().testTag(SCAFFOLD_SECONDARY_TAG)
                            ) {
                                secondaryPane(padding)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = layout.contentMaxWidth ?: Dp.Unspecified)
                                .fillMaxSize()
                                .testTag(SCAFFOLD_PRIMARY_TAG)
                        ) { content(padding) }
                    }
                }
                if (showDock) {
                    EntryDock(
                        dock = dock,
                        modifier = Modifier
                            .padding(horizontal = ContentMargin, vertical = 8.dp)
                            .then(
                                if (showBar) {
                                    Modifier
                                } else {
                                    Modifier.windowInsetsPadding(
                                        windowInsets.only(WindowInsetsSides.Bottom)
                                    )
                                }
                            )
                            .testTag(SCAFFOLD_DOCK_TAG)
                    )
                }
                if (showBar) {
                    Box(
                        Modifier
                            .testTag(SCAFFOLD_BAR_TAG)
                            .background(colors.paper)
                            .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Bottom))
                    ) { SelloNavigationBar(navigation) }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String?,
    titleContent: (@Composable () -> Unit)?,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TopBarHeight)
            .padding(horizontal = if (navigationIcon == null) ContentMargin else 0.dp)
    ) {
        navigationIcon?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
            }
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
        actions()
    }
}

private val TopBarHeight = 50.dp
private val ContentMargin = 12.dp

const val SCAFFOLD_RAIL_TAG = "sello:scaffold:rail"
const val SCAFFOLD_BAR_TAG = "sello:scaffold:bar"
const val SCAFFOLD_DOCK_TAG = "sello:scaffold:dock"
const val SCAFFOLD_PRIMARY_TAG = "sello:scaffold:primary"
const val SCAFFOLD_SECONDARY_TAG = "sello:scaffold:secondary"
