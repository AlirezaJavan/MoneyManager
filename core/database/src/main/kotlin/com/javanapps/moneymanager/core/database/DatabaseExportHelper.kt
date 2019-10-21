package com.javanapps.moneymanager.core.database

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseExportHelper
    @Inject
    constructor(
        private val database: MoneyManagerDatabase,
        @ApplicationContext private val context: Context,
    ) {
        fun checkpointWal() {
            try {
                Log.d("DatabaseExportHelper", "Starting TRUNCATE checkpoint...")
                val cursor = database.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)"))
                cursor.use {
                    if (it.moveToFirst()) {
                        val busy = it.getInt(0)
                        val log = it.getInt(1)
                        val checkpointed = it.getInt(2)
                        Log.d("DatabaseExportHelper", "Checkpoint result: busy=$busy, log=$log, checkpointed=$checkpointed")
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseExportHelper", "Checkpoint failed", e)
            }
        }

        fun databaseFile(): File {
            val dbPath = database.openHelper.writableDatabase.path
            Log.d("DatabaseExportHelper", "Database path from openHelper: $dbPath")

            if (dbPath != null) {
                val file = File(dbPath)
                if (file.exists()) {
                    Log.d("DatabaseExportHelper", "File found at path, size: ${file.length()} bytes")
                    return file
                } else {
                    Log.w("DatabaseExportHelper", "File at path does not exist")
                }
            }

            val dbName = database.openHelper.databaseName ?: "moneymanager-database"
            val fallbackFile = context.getDatabasePath(dbName)
            Log.d(
                "DatabaseExportHelper",
                "Fallback path: ${fallbackFile.absolutePath}, exists: ${fallbackFile.exists()}, size: ${fallbackFile.length()}",
            )
            return fallbackFile
        }
    }
