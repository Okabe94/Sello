package com.software.sello.platform

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri

/**
 * The address of a document a provider handed out, or null. The system pickers only
 * ever return content addresses; anything else, a file path above all, could reach
 * Sello's own private files through the resolver, so it is refused before use.
 */
internal fun providerDocument(id: String): Uri? = id.toUri().takeIf {
    it.scheme == ContentResolver.SCHEME_CONTENT && !it.authority.isNullOrEmpty()
}
