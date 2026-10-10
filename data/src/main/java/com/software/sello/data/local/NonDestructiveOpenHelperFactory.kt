package com.software.sello.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

/**
 * Opens the file the usual way, except for one thing. When Android finds a database
 * file damaged, its default reaction is to delete the file and start an empty one,
 * which would silently replace a person's finances with nothing. Here the damaged file
 * is left where it is and the open fails, so the failure is reported and recovery
 * stays a decision for its owner.
 */
internal class NonDestructiveOpenHelperFactory(
    private val delegate: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory()
) : SupportSQLiteOpenHelper.Factory {
    override fun create(
        configuration: SupportSQLiteOpenHelper.Configuration
    ): SupportSQLiteOpenHelper = delegate.create(
        SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
            .name(configuration.name)
            .callback(KeepDamagedFile(configuration.callback))
            .noBackupDirectory(configuration.useNoBackupDirectory)
            .allowDataLossOnRecovery(false)
            .build()
    )

    private class KeepDamagedFile(private val callback: SupportSQLiteOpenHelper.Callback) :
        SupportSQLiteOpenHelper.Callback(callback.version) {
        override fun onConfigure(db: SupportSQLiteDatabase) = callback.onConfigure(db)

        override fun onCreate(db: SupportSQLiteDatabase) = callback.onCreate(db)

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
            callback.onUpgrade(db, oldVersion, newVersion)

        override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
            callback.onDowngrade(db, oldVersion, newVersion)

        override fun onOpen(db: SupportSQLiteDatabase) = callback.onOpen(db)

        // Deliberately not delegated: the inherited behaviour deletes the file.
        override fun onCorruption(db: SupportSQLiteDatabase) = Unit
    }
}
