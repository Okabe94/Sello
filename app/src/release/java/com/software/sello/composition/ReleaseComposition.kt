package com.software.sello.composition

import android.app.Application

/** A release build has no developer tools: it is always the production composition. */
@Suppress("UNUSED_PARAMETER")
fun variantComposition(application: Application): VariantComposition = ProductionComposition
