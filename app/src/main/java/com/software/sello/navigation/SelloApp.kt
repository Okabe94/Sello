package com.software.sello.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.software.sello.R
import com.software.sello.designsystem.component.Blotter
import com.software.sello.designsystem.component.MonthSwitcher
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.feature.category.CategoryEditorRoot
import com.software.sello.feature.recibo.ReciboRoot
import com.software.sello.presentation.month.MonthNames
import com.software.sello.presentation.month.monthNames

/**
 * The root of the app's interface: it owns navigation and the frame's lifecycle
 * effects, and hands [ShellScreen] plain state and actions.
 */
@Composable
fun SelloAppRoot(viewModels: ViewModelProvider.Factory) {
    val shell: ShellViewModel = viewModel(factory = viewModels)
    val state by shell.state.collectAsStateWithLifecycle()
    // Stop and start, not pause and resume: a dialog over the app is not time away.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { shell.onAction(ShellAction.EnteredBackground) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        shell.onAction(ShellAction.ReturnedToForeground)
    }

    val navigation = rememberNavController()
    val entry by navigation.currentBackStackEntryAsState()
    val onDetail = entry != null && navigation.previousBackStackEntry != null

    NavHost(navController = navigation, startDestination = ReciboRoute) {
        composable<ReciboRoute> {
            ShellScreen(state, monthNames(), shell::onAction) { padding ->
                ReciboRoot(viewModels, padding) { navigation.navigate(CategoryEditorRoute()) }
            }
        }
        composable<CategoryEditorRoute> {
            CategoryEditorRoot(viewModels) { createdId ->
                val onEditor =
                    navigation.currentBackStackEntry?.destination?.hasRoute<CategoryEditorRoute>()
                if (onEditor == true) {
                    // Only the identifier goes back; whoever opened the form reads the rest.
                    if (createdId != null) {
                        navigation.previousBackStackEntry?.savedStateHandle
                            ?.set(CREATED_CATEGORY_RESULT, createdId)
                    }
                    navigation.popBackStack()
                }
            }
        }
    }
    // An entry cannot be recorded without a category, so the form to create one opens
    // with that explanation instead of an entry form that could not be completed.
    LaunchedEffect(state.entryNeedsCategory) {
        if (state.entryNeedsCategory) {
            shell.onAction(ShellAction.CategoryPrerequisiteShown)
            navigation.navigate(CategoryEditorRoute(forEntry = true)) { launchSingleTop = true }
        }
    }
    // Declared after the content so that it is asked before the navigation host: an
    // open panel closes first, whatever screen is underneath.
    val target = backTarget(state.monthPickerOpen, onDetail, ShellTab.Recibo)
    BackHandler(enabled = target == BackTarget.ClosePanel) {
        shell.onAction(ShellAction.CloseMonthPicker)
    }
}

/**
 * The frame: the month in the title, the content, and the month picker. The picker is
 * a bottom sheet, or a side panel beside the content in a window wide enough for one.
 * There is no tab bar while there is one tab, and no dock until the entry form exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(
    state: ShellState,
    names: MonthNames,
    onAction: (ShellAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val title = names.title(state.selectedMonth)
    val openPicker = stringResource(R.string.month_picker_open, title)
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sidePanel = selloWindowLayout(maxWidth, maxHeight).sidePanelWidth != null
        SelloScaffold(
            title = title,
            titleContent = {
                MonthSwitcher(
                    label = title,
                    onClick = { onAction(ShellAction.OpenMonthPicker) },
                    modifier = Modifier
                        .testTag(MONTH_SWITCHER_TAG)
                        .semantics { contentDescription = openPicker }
                )
            },
            blotter = Blotter.Short,
            secondaryPane = if (state.monthPickerOpen && sidePanel) {
                { padding ->
                    Column(
                        Modifier.fillMaxSize().verticalScroll(
                            rememberScrollState()
                        ).padding(padding)
                    ) {
                        Slip(pinked = false) { MonthPicker(state, names, onAction) }
                    }
                }
            } else {
                null
            },
            content = content
        )
        if (state.monthPickerOpen && !sidePanel) {
            ModalBottomSheet(
                onDismissRequest = { onAction(ShellAction.CloseMonthPicker) },
                // Opened whole: a half-open sheet would hide the later months.
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SelloTheme.colors.paper,
                contentColor = SelloTheme.colors.ink
            ) {
                // Scrolls, so a large font or a short window cannot cut months off.
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MonthPicker(state, names, onAction)
                }
            }
        }
    }
}

const val MONTH_SWITCHER_TAG = "shell:month-switcher"
