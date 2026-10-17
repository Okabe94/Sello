package com.software.sello.designsystem.component.v2

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloMotion
import com.software.sello.designsystem.theme.SelloTheme

/** Presentation only: the host opens this after a confirmed completion, never during submission. */
@Composable
fun CelebrationOverlay(
    title: String,
    plateTitle: String,
    name: String,
    icon: SelloIcon,
    amount: MoneyTextValue,
    detail: String,
    places: List<MilestoneSpec>,
    doneLabel: String,
    viewLabel: String,
    onDone: () -> Unit,
    onView: () -> Unit
) {
    val reduced = SelloTheme.reducedMotion
    val entrance = remember { Animatable(if (reduced) 1f else 0f) }
    val flood = remember { Animatable(if (reduced) 1f else 0f) }
    val plate = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(reduced) {
        if (reduced) {
            entrance.snapTo(1f)
            flood.snapTo(1f)
            plate.snapTo(1f)
        } else {
            entrance.animateTo(1f, SelloMotion.count())
            flood.animateTo(1f, SelloMotion.emphasized())
            plate.animateTo(1f, SelloMotion.stamp())
        }
    }
    val colors = SelloTheme.colors
    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(colors.desk).semantics {
                paneTitle = title
            },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    colors.semantic.gainSoft,
                    radius = size.maxDimension * flood.value,
                    center = center
                )
                if (!reduced && plate.value > 0f) {
                    // Repeatable paper shapes, no random or wall-clock dependency.
                    repeat(24) { index ->
                        val x = size.width * ((index * 37 % 101) / 100f)
                        val y = size.height * ((index * 19 % 97) / 100f) * plate.value
                        rotate(index * 17f, Offset(x, y)) {
                            if (index % 3 ==
                                0
                            ) {
                                drawCircle(colors.paper, 3.dp.toPx(), Offset(x, y))
                            } else {
                                drawRect(
                                    colors.paper,
                                    Offset(x, y),
                                    androidx.compose.ui.geometry.Size(
                                        5.dp.toPx(),
                                        if (index %
                                            2 ==
                                            0
                                        ) {
                                            14.dp.toPx()
                                        } else {
                                            5.dp.toPx()
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val visiblePlaces = (entrance.value * places.size).toInt().coerceIn(0, places.size)
                MilestoneRow(
                    places.take(visiblePlaces),
                    landingIndex = if (reduced) null else visiblePlaces - 1
                )
                if (plate.value > 0f) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.semantic.onGainSoft,
                        textAlign = TextAlign.Center
                    )
                    Slip(
                        modifier = Modifier.fillMaxWidth().graphicsLayer {
                            scaleX =
                                if (reduced) {
                                    1f
                                } else {
                                    SelloMotion.STAMP_V2_INITIAL_SCALE +
                                        (1f - SelloMotion.STAMP_V2_INITIAL_SCALE) * plate.value
                                }
                            scaleY = scaleX
                            rotationZ = -4f * plate.value
                            alpha = plate.value.coerceIn(0f, 1f)
                        },
                        pinked = false
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                plateTitle,
                                style = SelloTheme.type.stampLarge,
                                color = colors.semantic.gain,
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                icon.painter(),
                                null,
                                Modifier.padding(8.dp).size(40.dp),
                                colors.semantic.gain
                            )
                            Text(
                                name,
                                fontWeight = FontWeight.W900,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            TearLine()
                            FittedMoneyText(amount)
                            TearLine()
                            Text(detail, textAlign = TextAlign.Center, color = colors.inkSoft)
                        }
                    }
                    SelloButton(doneLabel, onDone, Modifier.fillMaxWidth(), tone = ButtonTone.Gain)
                    SelloButton(
                        viewLabel,
                        onView,
                        Modifier.fillMaxWidth(),
                        kind = com.software.sello.designsystem.component.ButtonKind.Text,
                        tone = ButtonTone.Gain
                    )
                }
            }
        }
    }
}
