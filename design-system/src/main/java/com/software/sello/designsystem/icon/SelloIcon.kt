package com.software.sello.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.software.sello.designsystem.R

/**
 * Material Symbols Rounded, weight 500, bundled one vector per icon. Icons are filled;
 * the outlined drawing exists only where the reference uses it (unselected tabs).
 */
enum class SelloIcon(@DrawableRes val filled: Int, @DrawableRes val outlined: Int? = null) {
    AccountBalance(
        R.drawable.ic_sello_account_balance,
        R.drawable.ic_sello_account_balance_outlined
    ),
    Add(R.drawable.ic_sello_add),
    ArrowBack(R.drawable.ic_sello_arrow_back),
    Backspace(R.drawable.ic_sello_backspace),
    Check(R.drawable.ic_sello_check),
    ChevronLeft(R.drawable.ic_sello_chevron_left),
    ChevronRight(R.drawable.ic_sello_chevron_right),
    ContentPaste(R.drawable.ic_sello_content_paste),
    DirectionsBus(R.drawable.ic_sello_directions_bus),
    EditNote(R.drawable.ic_sello_edit_note),
    Error(R.drawable.ic_sello_error),
    Event(R.drawable.ic_sello_event),
    Home(R.drawable.ic_sello_home),
    Insights(R.drawable.ic_sello_insights),
    KeyboardArrowDown(R.drawable.ic_sello_keyboard_arrow_down),
    LocalCafe(R.drawable.ic_sello_local_cafe),
    ReceiptLong(R.drawable.ic_sello_receipt_long, R.drawable.ic_sello_receipt_long_outlined),
    Restaurant(R.drawable.ic_sello_restaurant),
    Settings(R.drawable.ic_sello_settings),
    Theaters(R.drawable.ic_sello_theaters)
}

@Composable
fun SelloIcon.painter(filled: Boolean = true): Painter =
    painterResource(if (filled) this.filled else outlined ?: this.filled)
