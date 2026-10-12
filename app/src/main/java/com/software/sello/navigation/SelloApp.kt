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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import androidx.navigation.toRoute
import com.software.sello.R
import com.software.sello.designsystem.component.Blotter
import com.software.sello.designsystem.component.MonthSwitcher
import com.software.sello.designsystem.component.SelloDock
import com.software.sello.designsystem.component.SelloNotice
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.feature.category.CategoryEditorRoot
import com.software.sello.feature.expense.ExpenseEntryRoot
import com.software.sello.feature.recibo.CategoryDetailAction
import com.software.sello.feature.recibo.CategoryDetailScreen
import com.software.sello.feature.recibo.CategoryDetailViewModel
import com.software.sello.feature.recibo.ReciboAction
import com.software.sello.feature.recibo.ReciboScreen
import com.software.sello.feature.recibo.ReciboState
import com.software.sello.feature.recibo.ReciboViewModel
import com.software.sello.presentation.corrections.CorrectionNotice
import com.software.sello.presentation.corrections.CorrectionsViewModel
import com.software.sello.presentation.date.dateLabels
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
    val corrections: CorrectionsViewModel = viewModel(factory = viewModels)
    val notice by corrections.notice.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        shell.onAction(ShellAction.ReturnedToForeground)
        corrections.returned()
    }

    val navigation = rememberNavController()
    val entry by navigation.currentBackStackEntryAsState()
    val onDetail = entry != null && navigation.previousBackStackEntry != null

    NavHost(navController = navigation, startDestination = ReciboRoute) {
        composable<ReciboRoute> {
            ReciboDestination(
                state = state,
                viewModels = viewModels,
                onShell = shell::onAction,
                notice = notice,
                onUndo = corrections::undo,
                onEditExpense = { id ->
                    navigation.navigate(ExpenseEditRoute(id)) { launchSingleTop = true }
                }
            ) { navigation.navigate(CategoryEditorRoute()) }
        }
        composable<CategoryEditorRoute> { entry ->
            val forEntry = entry.toRoute<CategoryEditorRoute>().forEntry
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
                    // The category was created so that an expense could be recorded:
                    // carry on to that, with the new category chosen.
                    if (createdId != null && forEntry) {
                        val amount = state.pendingEntry?.amount?.money?.minorUnits?.toString()
                        shell.onAction(ShellAction.EntryTaken)
                        navigation.navigate(ExpenseEntryRoute(createdId, amount))
                    }
                }
            }
        }
        composable<ExpenseEntryRoute> {
            ExpenseEntryRoot(viewModels) {
                val onEntry =
                    navigation.currentBackStackEntry?.destination?.hasRoute<ExpenseEntryRoute>()
                if (onEntry == true) navigation.popBackStack()
            }
        }
        // The same form: the route's identifier makes it correct that expense.
        composable<ExpenseEditRoute> {
            ExpenseEntryRoot(viewModels) {
                val onEdit =
                    navigation.currentBackStackEntry?.destination?.hasRoute<ExpenseEditRoute>()
                if (onEdit == true) navigation.popBackStack()
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
    LaunchedEffect(state.entryReady) {
        if (state.entryReady) {
            val request = state.pendingEntry
            shell.onAction(ShellAction.EntryTaken)
            navigation.navigate(
                ExpenseEntryRoute(
                    categoryId = request?.categoryId?.value,
                    amount = request?.amount?.money?.minorUnits?.toString()
                )
            ) { launchSingleTop = true }
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
 * Recibo and the detail of one of its categories: one screen at a time where there is
 * room for one, side by side where there is room for two. Which category is open is
 * the frame's saved state, so it is the same category after a rotation, a resize or
 * the process being killed. With room for two and none opened, the first one is shown.
 */
@Composable
private fun ReciboDestination(
    state: ShellState,
    viewModels: ViewModelProvider.Factory,
    onShell: (ShellAction) -> Unit,
    notice: CorrectionNotice?,
    onUndo: () -> Unit,
    onEditExpense: (String) -> Unit,
    onCreateCategory: () -> Unit
) {
    val recibo: ReciboViewModel = viewModel(factory = viewModels)
    val detail: CategoryDetailViewModel = viewModel(factory = viewModels)
    val reciboState by recibo.state.collectAsStateWithLifecycle()
    val detailState by detail.state.collectAsStateWithLifecycle()
    val names = monthNames()
    val dates = dateLabels()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val twoPane = showsDetailBeside(maxWidth, maxHeight)
        val first = (reciboState as? ReciboState.Month)?.categories?.firstOrNull()?.id
        val shownId = state.detailCategoryId ?: first.takeIf { twoPane }
        LaunchedEffect(shownId) { detail.onAction(CategoryDetailAction.Show(shownId)) }
        // The name and the detail of the category chosen, never of the one before it.
        val shownDetail = detailOf(shownId, detailState)
        val title = detailTitle(shownId, detailState, reciboState)
            ?: stringResource(R.string.category_detail_title_fallback)
        ShellScreen(
            state = state,
            names = names,
            onAction = onShell,
            notice = notice?.let { { CorrectionNoticeBar(it, onUndo) } },
            detail = shownId?.let { id ->
                ShellDetail(title) { padding ->
                    CategoryDetailScreen(
                        state = shownDetail,
                        months = names,
                        dates = dates,
                        showName = twoPane,
                        onAction = detail::onAction,
                        onRecordHere = { onShell(ShellAction.StartEntryIn(id)) },
                        contentPadding = padding,
                        onEditExpense = onEditExpense
                    )
                }
            }
        ) { padding ->
            ReciboScreen(
                state = reciboState,
                onAction = { action ->
                    when (action) {
                        ReciboAction.CreateCategory -> onCreateCategory()
                        is ReciboAction.OpenCategory -> onShell(ShellAction.OpenCategory(action.id))
                        ReciboAction.Retry -> Unit
                    }
                    recibo.onAction(action)
                },
                contentPadding = padding,
                selectedCategoryId = shownId.takeIf { twoPane }
            )
        }
        // Beside Recibo the detail is not a screen to leave: Back does what it does on Recibo.
        val onDetail = state.detailCategoryId != null && !twoPane
        val target = backTarget(state.monthPickerOpen, onDetail, ShellTab.Recibo)
        BackHandler(enabled = target == BackTarget.LeaveDetail) {
            onShell(ShellAction.CloseCategory)
        }
    }
}

/**
 * Whether a detail fits beside what it is the detail of: a window laid out in two
 * columns that is also wide enough for both to be read. A window that is short and
 * narrow, such as half a phone's screen, shows one at a time like any phone.
 */
fun showsDetailBeside(width: Dp, height: Dp): Boolean =
    selloWindowLayout(width, height).twoPane && width >= SideBySideWidth

private val SideBySideWidth = 600.dp

/** The detail of what the content lists: its [title] and how to draw it. */
class ShellDetail(val title: String, val content: @Composable (PaddingValues) -> Unit)

/**
 * The frame: the month in the title, the content, and the month picker. With a
 * [detail], a window with room for two columns shows it beside the content; any other
 * window shows it instead of the content once it has been opened, under its own title
 * and with a way back. The picker is
 * a bottom sheet, or a side panel beside the content in a window wide enough for one.
 * There is no tab bar while there is one tab. The dock asks to record an expense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(
    state: ShellState,
    names: MonthNames,
    onAction: (ShellAction) -> Unit,
    modifier: Modifier = Modifier,
    detail: ShellDetail? = null,
    notice: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val title = names.title(state.selectedMonth)
    // "Mes actual" is said only of the month the financial day is in.
    val openPicker = stringResource(
        if (state.selectedMonth == state.currentMonth) {
            R.string.month_picker_open_current
        } else {
            R.string.month_picker_open_selected
        },
        title
    )
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layout = selloWindowLayout(maxWidth, maxHeight)
        val sidePanel = layout.sidePanelWidth != null
        val beside = showsDetailBeside(maxWidth, maxHeight)
        val openedId = state.detailCategoryId.takeIf { detail != null && !beside }
        if (openedId != null && detail != null) {
            SelloScaffold(
                title = detail.title,
                subtitle = title,
                blotter = Blotter.Short,
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ShellAction.CloseCategory) },
                        modifier = Modifier.testTag(DETAIL_BACK_TAG)
                    ) {
                        Icon(
                            SelloIcon.ArrowBack.painter(),
                            stringResource(R.string.category_detail_back)
                        )
                    }
                },
                // From a category's own screen, an expense starts in that category.
                dock = SelloDock(
                    label = stringResource(R.string.dock_entry_label),
                    actionLabel = stringResource(R.string.dock_entry_action),
                    onClick = { onAction(ShellAction.StartEntryIn(openedId)) }
                ),
                notice = notice,
                content = detail.content
            )
        } else {
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
                dock = SelloDock(
                    label = stringResource(R.string.dock_entry_label),
                    actionLabel = stringResource(R.string.dock_entry_action),
                    onClick = { onAction(ShellAction.StartEntry) }
                ),
                notice = notice,
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
                    detail?.content.takeIf { beside }
                },
                content = content
            )
        }
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

/**
 * What is said about the last correction. Only a deletion that can still be undone
 * has something to press; everything else is a statement that goes away by itself.
 */
@Composable
private fun CorrectionNoticeBar(notice: CorrectionNotice, onUndo: () -> Unit) {
    val tagged = Modifier.testTag(CORRECTION_NOTICE_TAG)
    when (notice) {
        is CorrectionNotice.Deleted -> SelloNotice(
            message = stringResource(
                if (notice.undoFailed) {
                    R.string.correction_undo_failed
                } else {
                    R.string.correction_deleted
                }
            ),
            modifier = tagged,
            actionLabel = stringResource(R.string.correction_undo).takeIf { notice.canUndo },
            onAction = onUndo.takeIf { notice.canUndo },
            busy = notice.undoing,
            spoken = stringResource(
                when {
                    notice.undoFailed -> R.string.correction_undo_failed
                    notice.canUndo -> R.string.correction_deleted_spoken
                    else -> R.string.correction_deleted
                }
            )
        )

        CorrectionNotice.Restored ->
            SelloNotice(stringResource(R.string.correction_restored), tagged)

        CorrectionNotice.UndoTooLate ->
            SelloNotice(stringResource(R.string.correction_too_late), tagged)

        CorrectionNotice.UndoBlocked ->
            SelloNotice(stringResource(R.string.correction_blocked), tagged)

        CorrectionNotice.Updated ->
            SelloNotice(stringResource(R.string.correction_updated), tagged)
    }
}

const val MONTH_SWITCHER_TAG = "shell:month-switcher"
const val DETAIL_BACK_TAG = "shell:detail-back"
const val CORRECTION_NOTICE_TAG = "shell:correction-notice"
