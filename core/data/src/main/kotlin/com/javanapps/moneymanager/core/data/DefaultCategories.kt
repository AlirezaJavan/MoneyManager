package com.javanapps.moneymanager.core.data

import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType

/** Seed categories carried over from the legacy app (shown on first run / after migration). */
object DefaultCategories {
    private val EXPENSE =
        listOf(
            "لباس و کفش",
            "شارژ و اینترنت",
            "مایحتاج خانه",
            "مربوط به ماشین",
            "اعضای خانواده",
            "حقوق ماهانه",
            "خرج مطب",
            "قرض دادن",
            "قسط و قبض",
            "متفرقه",
        )

    private val INCOME =
        listOf(
            "حقوق ماهانه",
            "سود بانکی",
            "وام",
            "اجاره",
            "درآمد مطب",
            "قرض گرفتن",
            "فروش لوازم",
            "متفرقه",
        )

    /** Fallback category used for SMS-created transactions when none is chosen. */
    const val MISC = "متفرقه"

    fun all(): List<Category> =
        EXPENSE.map { Category(Category.NO_ID, it, TransactionType.EXPENSE) } +
            INCOME.map { Category(Category.NO_ID, it, TransactionType.INCOME) }
}
