package com.software.sello.feature.income

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class IncomeFormScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun aReceiptWithoutAnOptionalNoteDoesNotClaimItsDetailsCouldNotBeRead() {
        val today = LocalDate.of(2026, 10, 13)
        rule.setContent {
            SelloTheme(reducedMotion = true) {
                IncomeFormScreen(
                    IncomeFormState(
                        date = today,
                        today = today,
                        receipt = IncomeReceiptView(
                            "synthetic",
                            IncomeReceiptView.Details(
                                MoneyTextValue(MoneySign.None, "", "5.000", "5.000 pesos"),
                                "salary",
                                null,
                                today,
                                null
                            )
                        )
                    ),
                    {}
                )
            }
        }
        rule.onNodeWithText("El ingreso quedó guardado, pero no pudimos leer sus detalles.")
            .assertDoesNotExist()
        rule.onNodeWithContentDescription("5.000 pesos").assertIsDisplayed()
    }

    @Test
    fun largeFontKeepsExactAmountAndBusySaveCannotBeRepeated() {
        val today = LocalDate.of(2026, 10, 13)
        rule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(LocalDensity.current.density, 2f)
            ) {
                SelloTheme(reducedMotion = true) {
                    IncomeFormScreen(
                        IncomeFormState(
                            loading = false,
                            date = today,
                            today = today,
                            amount = MoneyTextValue(
                                MoneySign.None,
                                "",
                                "999.999.999.999",
                                "999.999.999.999 pesos"
                            ),
                            sourceKeys = listOf("salary", "other"),
                            sourceKey = "salary",
                            missing = null,
                            busy = true
                        ),
                        {}
                    )
                }
            }
        }
        rule.onNodeWithContentDescription(
            "999.999.999.999 pesos"
        ).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("income.submit").performScrollTo().assertIsNotEnabled()
    }
}
