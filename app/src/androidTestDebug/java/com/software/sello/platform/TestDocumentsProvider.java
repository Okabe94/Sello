package com.software.sello.platform;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Documents for tests, each path a way a real provider behaves:
 * {@code ok/<name>}, a file in this provider's cache; {@code refuse}, the provider will
 * not open it; {@code readonly}, it opens only for reading, so the first write fails;
 * {@code broken}, a pipe whose reader has already gone, as when an upload is cut off.
 *
 * <p>Java, not Kotlin: the provider runs in the test package's own process, which has
 * no Kotlin standard library.
 */
public final class TestDocumentsProvider extends ContentProvider {
    public static final String AUTHORITY = "com.software.sello.test.documents";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        String kind = segments.isEmpty() ? "" : segments.get(0);
        switch (kind) {
            case "ok":
                return ParcelFileDescriptor.open(
                        file(segments.get(1)), ParcelFileDescriptor.parseMode(mode));
            case "readonly":
                File readOnly = file("readonly");
                try {
                    new FileOutputStream(readOnly).close();
                } catch (IOException e) {
                    throw new FileNotFoundException(e.getMessage());
                }
                return ParcelFileDescriptor.open(readOnly, ParcelFileDescriptor.MODE_READ_ONLY);
            case "broken":
                try {
                    ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createReliablePipe();
                    pipe[0].closeWithError("The upload was cut off");
                    return pipe[1];
                } catch (IOException e) {
                    throw new FileNotFoundException(e.getMessage());
                }
            default:
                throw new FileNotFoundException("refused");
        }
    }

    @Override
    public Cursor query(
            Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[] {OpenableColumns.DISPLAY_NAME});
        cursor.addRow(new Object[] {uri.getLastPathSegment()});
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "application/json";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private File file(String name) {
        return new File(getContext().getCacheDir(), name);
    }
}
