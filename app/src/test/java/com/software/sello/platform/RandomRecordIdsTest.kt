package com.software.sello.platform

import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomRecordIdsTest {
    @Test
    fun everyIdentifierIsCanonicalAndNew() {
        val ids = RandomRecordIds()

        val issued = List(1_000) { ids.next() }

        assertEquals(1_000, issued.toSet().size)
        for (id in issued) {
            assertTrue(id, ExpenseId.of(id) is Outcome.Success)
            assertTrue(id, OperationId.of(id) is Outcome.Success)
        }
    }
}
