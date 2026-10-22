package com.software.sello.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.software.sello.designsystem.component.Blotter
import com.software.sello.designsystem.component.SelloDestination
import com.software.sello.designsystem.component.SelloDock
import com.software.sello.designsystem.component.SelloNavigation
import com.software.sello.designsystem.component.SelloNotice
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.screens.AnotarScreen
import com.software.sello.prototype.screens.AportarScreen
import com.software.sello.prototype.screens.AporteReceiptScreen
import com.software.sello.prototype.screens.BackupScreen
import com.software.sello.prototype.screens.CategoryEditorScreen
import com.software.sello.prototype.screens.CategoryScreen
import com.software.sello.prototype.screens.ExpenseListScreen
import com.software.sello.prototype.screens.GoalDoneScreen
import com.software.sello.prototype.screens.GoalEditorScreen
import com.software.sello.prototype.screens.GoalScreen
import com.software.sello.prototype.screens.IncomeReceiptScreen
import com.software.sello.prototype.screens.IncomeScreen
import com.software.sello.prototype.screens.InvestmentEditorScreen
import com.software.sello.prototype.screens.InvestmentScreen
import com.software.sello.prototype.screens.MapScreen
import com.software.sello.prototype.screens.MovementScreen
import com.software.sello.prototype.screens.PatrimonioScreen
import com.software.sello.prototype.screens.ReciboScreen
import com.software.sello.prototype.screens.RecurringScreen
import com.software.sello.prototype.screens.ResumenScreen
import com.software.sello.prototype.screens.SettingsScreen
import com.software.sello.prototype.screens.VariosScreen
import kotlinx.coroutines.delay

class PrototypeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PrototypeApp() }
    }
}

enum class ThemeMode { Sistema, Claro, Oscuro }

/** What the prototype pretends to remember. Nothing financial lives here. */
@Stable
class ProtoState {
    var theme by mutableStateOf(ThemeMode.Sistema)
    var ink by mutableStateOf(SelloInk.Cobalto)
    var firstRun by mutableStateOf(false)
    var empty by mutableStateOf(false)
    var pastMonth by mutableStateOf<String?>(null)
    var patrimonioGoals by mutableStateOf(false)
    var notice by mutableStateOf<Notice?>(null)

    /** Each open navigation entry's screen label, keyed by the entry's id. */
    val tags = mutableStateMapOf<String, String>()

    private var noticeCount = 0L

    fun say(message: String, undo: Boolean = false) {
        // A new id each time, so the same message shown twice still restarts its timer.
        notice = Notice(message, undo, ++noticeCount)
    }
}

data class Notice(val message: String, val undo: Boolean, val id: Long)

val LocalProto = staticCompositionLocalOf<ProtoState> { error("No prototype state") }
val LocalNav = staticCompositionLocalOf<NavHostController> { error("No navigation") }

@Composable
fun PrototypeApp() {
    val proto = remember { ProtoState() }
    val dark = when (proto.theme) {
        ThemeMode.Sistema -> isSystemInDarkTheme()
        ThemeMode.Claro -> false
        ThemeMode.Oscuro -> true
    }
    val nav = rememberNavController()
    LaunchedEffect(proto.notice) {
        if (proto.notice != null) {
            delay(NOTICE_MILLIS)
            proto.notice = null
        }
    }
    SelloTheme(ink = proto.ink, darkTheme = dark) {
        CompositionLocalProvider(LocalProto provides proto, LocalNav provides nav) {
            Box(Modifier.fillMaxSize()) {
                PrototypeNavHost(nav)
                CurrentScreenTag(nav, proto, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

private const val NOTICE_MILLIS = 6_000L

private val push: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(240)) { it * 3 / 10 } + fadeIn(tween(240))
}
private val pushOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(120))
}
private val pop: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(240)) { it * 3 / 10 } + fadeOut(tween(240))
}
private val rise: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInVertically(tween(360)) { it / 4 } + fadeIn(tween(240))
}
private val sink: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutVertically(tween(240)) { it / 4 } + fadeOut(tween(240))
}

@Composable
private fun PrototypeNavHost(nav: NavHostController) {
    NavHost(
        navController = nav,
        startDestination = ReciboTab,
        enterTransition = push,
        exitTransition = pushOut,
        popEnterTransition = { fadeIn(tween(120)) },
        popExitTransition = pop
    ) {
        navigation<ReciboTab>(startDestination = ReciboRoute) {
            composable<ReciboRoute> { ReciboScreen() }
            composable<CategoryRoute> { CategoryScreen(it.toRoute<CategoryRoute>().id) }
            composable<ExpenseListRoute> { ExpenseListScreen(it.toRoute<ExpenseListRoute>().id) }
            composable<SettingsRoute> { SettingsScreen() }
            composable<BackupRoute> { BackupScreen() }
        }
        navigation<ResumenTab>(startDestination = ResumenRoute) {
            composable<ResumenRoute> { ResumenScreen() }
            composable<IncomeRoute> { IncomeScreen() }
            composable<RecurringRoute> { RecurringScreen() }
        }
        navigation<PatrimonioTab>(startDestination = PatrimonioRoute) {
            composable<PatrimonioRoute> { PatrimonioScreen() }
            composable<InvestmentRoute> { InvestmentScreen(it.toRoute<InvestmentRoute>().id) }
            composable<GoalRoute> { GoalScreen(it.toRoute<GoalRoute>().id) }
        }
        sheet<AnotarRoute> { AnotarScreen(it.toRoute()) }
        sheet<VariosRoute> { VariosScreen(it.toRoute()) }
        sheet<CategoryEditorRoute> { CategoryEditorScreen(it.toRoute()) }
        sheet<IncomeReceiptRoute> { IncomeReceiptScreen() }
        sheet<InvestmentEditorRoute> {
            InvestmentEditorScreen(it.toRoute<InvestmentEditorRoute>().id)
        }
        sheet<MovementRoute> { MovementScreen(it.toRoute<MovementRoute>().id) }
        sheet<GoalEditorRoute> { GoalEditorScreen(it.toRoute<GoalEditorRoute>().id) }
        sheet<AportarRoute> { AportarScreen(it.toRoute()) }
        sheet<AporteReceiptRoute> { AporteReceiptScreen(it.toRoute()) }
        sheet<GoalDoneRoute> { GoalDoneScreen(it.toRoute<GoalDoneRoute>().id) }
        composable<MapRoute> { MapScreen() }
    }
}

private inline fun <reified T : Any> androidx.navigation.NavGraphBuilder.sheet(
    noinline content: @Composable (NavBackStackEntry) -> Unit
) = composable<T>(
    enterTransition = rise,
    popExitTransition = sink,
    content = { content(it) }
)

/** Opens a tab the way the reference asks: its own stack kept, no duplicates. */
fun NavHostController.openTab(tab: Any) {
    navigate(tab) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Leaves whatever is open and shows a tab at its root, for jumps from the screen map. */
fun NavHostController.jumpTo(tab: Any) {
    popBackStack(ReciboRoute, inclusive = false)
    if (tab != ReciboTab) openTab(tab)
}

/** Opens Anotar, or the category editor first while there is no category yet. */
fun NavHostController.anotar(proto: ProtoState, categoryId: String? = null) {
    if (proto.firstRun) {
        proto.say("Primero crea una categoría para anotar el gasto")
        navigate(CategoryEditorRoute(forEntry = true))
    } else {
        navigate(AnotarRoute(categoryId = categoryId))
    }
}

/**
 * A screen inside a tab: top bar, the three tabs and the dock. With [onBack] it is a
 * pushed screen and shows Back; [tabs] false hides the tabs and dock as well.
 */
@Composable
fun Shell(
    title: String,
    subtitle: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
    blotter: Blotter = Blotter.None,
    blotterScroll: () -> Int = { 0 },
    onBack: (() -> Unit)? = null,
    tabs: Boolean = true,
    dockCategoryId: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    secondaryPane: (@Composable (PaddingValues) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val entry by nav.currentBackStackEntryAsState()
    val hierarchy = entry?.destination?.hierarchy ?: emptySequence()
    val selected = when {
        hierarchy.any { it.hasRoute(ResumenTab::class) } -> TAB_RESUMEN
        hierarchy.any { it.hasRoute(PatrimonioTab::class) } -> TAB_PATRIMONIO
        else -> TAB_RECIBO
    }
    val navigation = SelloNavigation(
        destinations = listOf(
            SelloDestination(TAB_RECIBO, "Recibo", SelloIcon.ReceiptLong),
            SelloDestination(TAB_RESUMEN, "Resumen", SelloIcon.Insights),
            SelloDestination(TAB_PATRIMONIO, "Patrimonio", SelloIcon.AccountBalance)
        ),
        selectedId = selected,
        onSelect = { id ->
            // Choosing the tab already shown returns to its first screen.
            if (id == selected) {
                nav.popBackStack(
                    when (id) {
                        TAB_RESUMEN -> ResumenRoute
                        TAB_PATRIMONIO -> PatrimonioRoute
                        else -> ReciboRoute
                    },
                    inclusive = false
                )
            } else {
                when (id) {
                    TAB_RECIBO -> nav.openTab(ReciboTab)
                    TAB_RESUMEN -> nav.openTab(ResumenTab)
                    else -> nav.openTab(PatrimonioTab)
                }
            }
        }
    )
    val dock = SelloDock(
        label = "Anotar un gasto",
        actionLabel = "Anotar",
        onClick = { nav.anotar(proto, dockCategoryId) }
    )
    SelloScaffold(
        title = title,
        subtitle = subtitle,
        titleContent = titleContent,
        blotter = blotter,
        blotterScroll = blotterScroll,
        navigationIcon = onBack?.let { back -> { BackButton(back) } },
        actions = actions,
        navigation = navigation.takeIf { tabs },
        dock = dock.takeIf { tabs },
        notice = proto.notice?.let { notice ->
            {
                SelloNotice(
                    message = notice.message,
                    actionLabel = "Deshacer".takeIf { notice.undo },
                    onAction = { proto.say("Listo, se deshizo") }.takeIf { notice.undo }
                )
            }
        },
        secondaryPane = secondaryPane,
        content = content
    )
}

private const val TAB_RECIBO = "recibo"
private const val TAB_RESUMEN = "resumen"
private const val TAB_PATRIMONIO = "patrimonio"
