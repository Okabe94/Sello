package com.software.sello.prototype

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.software.sello.designsystem.component.AmountKeypad
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.theme.SelloTheme

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás") }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(Icons.Rounded.Close, "Cerrar") }
}

@Composable
fun ActionIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(icon, label) }
}

/** A "more" button with a menu of named actions. */
@Composable
fun Overflow(vararg items: Pair<String, () -> Unit>) {
    var open by remember { mutableStateOf(false) }
    Box {
        ActionIcon(Icons.Rounded.MoreVert, "Más opciones") { open = true }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            containerColor = SelloTheme.colors.paper
        ) {
            items.forEach { (label, action) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        open = false
                        action()
                    }
                )
            }
        }
    }
}

/** The usual scrolling column of slips on the desk. */
@Composable
fun Desk(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(bottom = SelloTheme.spacing.lg),
        content = content
    )
}

/** A slip's title with an optional quiet caption beside it, like "Tus categorías · queda en cada una". */
@Composable
fun SlipTitle(title: String, caption: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelloTheme.colors.inkSoft
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun Soft(text: String, modifier: Modifier = Modifier, color: Color = SelloTheme.colors.inkSoft) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color, modifier = modifier)
}

/** A label above a big answer, for a slip's first lines. */
@Composable
fun Answer(
    question: String,
    value: MoneyTextValue,
    style: MoneyStyle = MoneyStyle.Total,
    color: Color = Color.Unspecified,
    stamp: (@Composable () -> Unit)? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                question,
                style = MaterialTheme.typography.bodyLarge,
                color = SelloTheme.colors.inkSoft,
                modifier = Modifier.weight(1f)
            )
            if (stamp != null) Box(Modifier.padding(vertical = 6.dp)) { stamp() }
        }
        MoneyText(value, style = style, color = color)
    }
}

/** A thin bar filled to [fraction], used for goals, targets and category shares. */
@Composable
fun Bar(fraction: Float, modifier: Modifier = Modifier, color: Color = SelloTheme.colors.brand) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(SelloTheme.colors.paperDim)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .background(color)
        )
    }
}

/** The four round stamps of a goal: 25 %, 50 %, 75 % and the goal itself. */
@Composable
fun Milestones(milestones: List<Sample.Milestone>, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()) {
        milestones.forEach { stone ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val colors = SelloTheme.colors
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (stone.reached) colors.brand else colors.paper)
                        .border(
                            2.dp,
                            if (stone.reached) colors.brand else colors.outline,
                            CircleShape
                        )
                ) {
                    if (stone.reached) {
                        Icon(
                            Icons.Rounded.Check,
                            null,
                            tint = colors.onBrand,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            stone.label.take(2),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSoft
                        )
                    }
                }
                Text(
                    stone.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.W700
                )
                Text(
                    stone.month,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSoft
                )
            }
        }
    }
}

/**
 * Prototype only: names the screen this navigation entry shows, such as
 * "4 · Detalle de categoría", numbered as in the reference.
 */
@Composable
fun ScreenTag(label: String) {
    val proto = LocalProto.current
    val entry = LocalViewModelStoreOwner.current as? NavBackStackEntry ?: return
    SideEffect { proto.tags[entry.id] = label }
    DisposableEffect(entry.id) { onDispose { proto.tags.remove(entry.id) } }
}

/** Draws the current screen's label over the top of everything else. */
@Composable
fun CurrentScreenTag(nav: NavHostController, proto: ProtoState, modifier: Modifier = Modifier) {
    val entry by nav.currentBackStackEntryAsState()
    val label = entry?.id?.let { proto.tags[it] } ?: return
    // In the gesture-bar strip at the bottom: it covers no content, and a camera cutout
    // in the middle of the status bar cannot hide it.
    Box(modifier.windowInsetsBottomHeight(WindowInsets.navigationBars), contentAlignment = Alignment.Center) {
        TagPill(label)
    }
}

@Composable
fun TagPill(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = SelloTheme.colors.paper,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(SelloTheme.colors.ink.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** A Material sheet dressed in Sello paper; [tag] names it like a screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelloSheet(
    title: String,
    onDismiss: () -> Unit,
    tag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SelloTheme.colors.paper,
        contentColor = SelloTheme.colors.ink
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SelloTheme.spacing.lg)
                .padding(bottom = SelloTheme.spacing.xl)
                .navigationBarsPadding()
        ) {
            TagPill(tag)
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics {
                    heading()
                }
            )
            content()
        }
    }
}

/** A typed amount for the keypad screens: just enough to make the keys feel real. */
class Draft(initial: String) {
    var digits by mutableStateOf(initial)
    val amount: Long get() = digits.toLongOrNull() ?: 0
    fun digit(d: Int) {
        if (digits.length < 12 && !(digits.isEmpty() && d == 0)) digits += d
    }
    fun tripleZero() {
        if (digits.isNotEmpty() && digits.length <= 9) digits += "000"
    }
    fun back() {
        digits = digits.dropLast(1)
    }
    fun clear() {
        digits = ""
    }
}

@Composable
fun rememberDraft(initial: Long?): Draft {
    var saved by rememberSaveable { mutableStateOf(initial?.toString().orEmpty()) }
    val draft = remember { Draft(saved) }
    saved = draft.digits
    return draft
}

@Composable
fun Keys(draft: Draft, modifier: Modifier = Modifier) {
    AmountKeypad(
        onDigit = draft::digit,
        onTripleZero = draft::tripleZero,
        onBackspace = draft::back,
        onClearAll = draft::clear,
        modifier = modifier
    )
}

/**
 * The bottom of every keypad sheet: what is missing, the keys and one button. It stays
 * put while the draft above it scrolls.
 */
@Composable
fun EntryControls(
    draft: Draft,
    button: String,
    enabled: Boolean,
    missing: String?,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm),
        modifier = modifier.padding(top = SelloTheme.spacing.sm)
    ) {
        if (missing != null) Soft(missing, Modifier.fillMaxWidth(), SelloTheme.colors.inkSoft)
        Keys(draft)
        SelloButton(button, onSubmit, Modifier.fillMaxWidth(), enabled = enabled)
    }
}

/** Splits a keypad screen: the draft scrolls, the controls stay pinned under it. */
@Composable
fun EntryLayout(
    padding: PaddingValues,
    draft: @Composable ColumnScope.() -> Unit,
    controls: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            content = draft
        )
        controls()
    }
}

/** Asks before leaving with something typed; not in destructive colours. */
@Composable
fun DiscardGuard(dirty: Boolean, question: String, onLeave: () -> Unit): () -> Unit {
    var asking by rememberSaveable { mutableStateOf(false) }
    val leave = { if (dirty) asking = true else onLeave() }
    BackHandler(onBack = leave)
    if (asking) {
        ConfirmSlip(
            title = question,
            message = AnnotatedString("Lo que escribiste no se guarda."),
            confirmLabel = "Descartar",
            onConfirm = {
                asking = false
                onLeave()
            },
            onDismiss = { asking = false },
            destructive = false,
            dismissLabel = "Seguir"
        )
    }
    return leave
}

/** One numbered step of the first-run slip. */
@Composable
fun Step(number: Int, text: String, done: Boolean) {
    val colors = SelloTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) colors.brand else colors.brandSoft)
        ) {
            if (done) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    tint = colors.onBrand,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text("$number", color = colors.onBrandSoft, fontWeight = FontWeight.W700)
            }
        }
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

/** A tappable row with a chevron, for settings and lists that lead somewhere. */
@Composable
fun LinkRow(
    title: String,
    caption: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = SelloTheme.colors.inkSoft)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (caption != null) Soft(caption)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = SelloTheme.colors.inkSoft)
    }
}

/** Centres a receipt on the desk, for the moments after saving. */
@Composable
fun ReceiptDesk(padding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(top = SelloTheme.spacing.xl)
    ) {
        Slip(Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
fun Centered(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = SelloTheme.colors.inkSoft,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun Gap() = Spacer(Modifier.height(SelloTheme.spacing.xs))
