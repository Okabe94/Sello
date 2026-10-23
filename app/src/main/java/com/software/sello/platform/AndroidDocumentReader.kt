package com.software.sello.platform

import android.content.ContentResolver
import androidx.core.net.toUri
import com.software.sello.domain.port.DispatcherProvider
import com.software.sello.domain.port.DocumentRead
import com.software.sello.domain.port.DocumentReader
import com.software.sello.domain.port.DocumentSource
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.withContext

/**
 * Reads a document the system picker returned, through its provider, holding at most
 * one byte more than [maxBytes] so a huge or endless document is never held whole.
 */
class AndroidDocumentReader(
    private val resolver: ContentResolver,
    private val dispatchers: DispatcherProvider
) : DocumentReader {
    override suspend fun read(source: DocumentSource, maxBytes: Int): DocumentRead =
        withContext(dispatchers.io) {
            try {
                val stream = resolver.openInputStream(source.id.toUri())
                    ?: return@withContext DocumentRead.Failed
                stream.use {
                    val held = ByteArrayOutputStream()
                    val buffer = ByteArray(BUFFER_BYTES)
                    while (held.size() <= maxBytes) {
                        val read = it.read(
                            buffer,
                            0,
                            minOf(buffer.size, maxBytes + 1 - held.size())
                        )
                        if (read < 0) break
                        held.write(buffer, 0, read)
                    }
                    if (held.size() > maxBytes) {
                        DocumentRead.TooLarge(maxBytes)
                    } else {
                        DocumentRead.Read(held.toByteArray())
                    }
                }
            } catch (_: IOException) {
                DocumentRead.Failed
            } catch (_: SecurityException) {
                DocumentRead.Failed
            } catch (_: IllegalArgumentException) {
                DocumentRead.Failed
            } catch (_: IllegalStateException) {
                DocumentRead.Failed
            }
        }

    private companion object {
        const val BUFFER_BYTES = 64 * 1024
    }
}
