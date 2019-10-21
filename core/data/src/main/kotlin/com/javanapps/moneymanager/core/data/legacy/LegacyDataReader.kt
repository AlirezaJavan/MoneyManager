package com.javanapps.moneymanager.core.data.legacy

import com.javanapps.moneymanager.core.model.MigrationData
import java.io.File

/** Reads transactions and categories from a legacy `Contacts_DB` SQLite file. */
interface LegacyDataReader {
    /** Reads the legacy `TBL2` (transactions) and `Cats` (categories) tables. */
    fun read(databaseFile: File): MigrationData
}
