package com.software.sello.platform

import com.software.sello.domain.port.RecordIdSource
import java.util.UUID

/** New random identifiers. `UUID.toString` is already the canonical lower-case form. */
class RandomRecordIds : RecordIdSource {
    override fun next(): String = UUID.randomUUID().toString()
}
