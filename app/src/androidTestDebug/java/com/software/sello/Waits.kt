package com.software.sello

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule

/*
 * The app reads its database off the main thread, which the test framework does not
 * wait for. A test that looks at the screen right after starting the app, or after a
 * step that reads or writes, waits here for what it expects instead of assuming the
 * read has finished.
 */

private const val PATIENCE_MILLIS = 15_000L

fun ComposeTestRule.await(matcher: SemanticsMatcher) {
    waitUntil(PATIENCE_MILLIS) {
        onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeTestRule.awaitTag(tag: String) = await(hasTestTag(tag))

fun ComposeTestRule.awaitText(text: String) = await(hasText(text))

fun ComposeTestRule.awaitDescription(description: String) =
    await(hasContentDescription(description))
