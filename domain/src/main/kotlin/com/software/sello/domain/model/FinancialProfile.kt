package com.software.sello.domain.model

import java.time.ZoneId

/**
 * The single record every other financial fact hangs from.
 *
 * [zone] decides which calendar day an entry belongs to and does not follow the
 * device. [generation] starts at 1 and advances only when the whole history is
 * replaced (restore or reset), so anything prepared against the old history can be
 * refused. [revision] starts at 0 and advances with every committed change.
 */
data class FinancialProfile(val zone: ZoneId, val generation: Long, val revision: Long)
