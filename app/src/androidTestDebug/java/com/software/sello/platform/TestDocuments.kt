package com.software.sello.platform

import android.net.Uri

/** Addresses of [TestDocumentsProvider]'s documents. */
object TestDocuments {
    fun uri(vararg path: String): Uri = Uri.Builder()
        .scheme("content")
        .authority(TestDocumentsProvider.AUTHORITY)
        .apply { path.forEach { appendPath(it) } }
        .build()
}
