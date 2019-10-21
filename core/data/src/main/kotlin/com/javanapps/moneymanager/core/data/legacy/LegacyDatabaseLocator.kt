package com.javanapps.moneymanager.core.data.legacy

import android.os.Environment
import java.io.File
import javax.inject.Inject

/** Locates the legacy database at its known external-storage path, if readable. */
class LegacyDatabaseLocator
    @Inject
    constructor() {
        fun autoDetect(): File? {
            @Suppress("DEPRECATION")
            val file = File(Environment.getExternalStorageDirectory(), "MoneyManager/Contacts_DB")
            return file.takeIf { it.exists() && it.canRead() }
        }
    }
