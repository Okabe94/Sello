package com.software.sello.presentation.count

import org.junit.Assert.assertEquals
import org.junit.Test

class CountTextTest {
    @Test
    fun countsUnderAThousandAreUnchanged() {
        assertEquals("0", countText(0))
        assertEquals("1", countText(1))
        assertEquals("999", countText(999))
    }

    @Test
    fun thousandsAreGroupedWithAPointAsAmountsAre() {
        assertEquals("1.000", countText(1_000))
        assertEquals("10.000", countText(10_000))
        assertEquals("100.000", countText(100_000))
        assertEquals("1.234.567", countText(1_234_567L))
        assertEquals("2.147.483.647", countText(Int.MAX_VALUE))
    }
}
