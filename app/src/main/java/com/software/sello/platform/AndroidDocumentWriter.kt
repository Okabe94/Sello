package com.software.sello.platform

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.software.sello.domain.port.DispatcherProvider
import com.software.sello.domain.port.DocumentDestination
import com.software.sello.domain.port.DocumentWrite
import com.software.sello.domain.port.DocumentWriter
import java.io.IOException
import kotlinx.coroutines.withContext

/**
 * Writes to a document the system picker created, through its provider. Success means
 * every byte was written, flushed, synced where the provider allows, and the document
 * closed without error; anything less is [DocumentWrite.Failed]. A failed document is
 * left as it is: it belongs to the provider and to the person, not to Sello.
 */
class AndroidDocumentWriter(
    private val resolver: ContentResolver,
    private val dispatchers: DispatcherProvider
) : DocumentWriter {
    override suspend fun write(destination: DocumentDestination, bytes: ByteArray): DocumentWrite =
        withContext(dispatchers.io) {
            val uri = providerDocument(destination.id) ?: return@withContext DocumentWrite.Failed
            try {
                // "wt" truncates, so an existing document chosen again is replaced, not mixed.
                val descriptor = resolver.openFileDescriptor(uri, "wt")
                    ?: return@withContext DocumentWrite.Failed
                // Closing the stream closes the descriptor, and a provider that failed to
                // commit the document reports it from that close.
                ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { stream ->
                    stream.write(bytes)
                    stream.flush()
                    // Some providers are not backed by a real file and cannot sync.
                    try {
                        stream.fd.sync()
                    } catch (_: IOException) {
                        Unit
                    }
                }
                DocumentWrite.Completed(displayName(uri), bytes.size.toLong())
            } catch (_: IOException) {
                DocumentWrite.Failed
            } catch (_: SecurityException) {
                DocumentWrite.Failed
            } catch (_: IllegalArgumentException) {
                DocumentWrite.Failed
            } catch (_: IllegalStateException) {
                DocumentWrite.Failed
            }
        }

    private fun displayName(uri: Uri): String? = try {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst() && !it.isNull(0)) it.getString(0) else null
        }
    } catch (_: RuntimeException) {
        null
    }
}
