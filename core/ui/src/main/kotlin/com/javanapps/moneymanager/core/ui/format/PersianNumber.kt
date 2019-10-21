package com.javanapps.moneymanager.core.ui.format

/** Persian/Farsi number formatting helpers. Pure functions, fully unit-testable. */
object PersianNumber {
    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /** Persian thousands separator (U+066C ARABIC THOUSANDS SEPARATOR). */
    private const val GROUP_SEPARATOR = '٬'

    const val TOMAN_SUFFIX = "تومان"

    /** Converts every Latin/Arabic-Indic digit in [text] to its Persian glyph. */
    fun toPersianDigits(text: String): String =
        buildString(text.length) {
            for (ch in text) {
                append(
                    when (ch) {
                        in '0'..'9' -> PERSIAN_DIGITS[ch - '0']
                        in '٠'..'٩' -> PERSIAN_DIGITS[ch - '٠'] // Arabic-Indic digits
                        else -> ch
                    },
                )
            }
        }

    fun toPersianDigits(value: Long): String = toPersianDigits(value.toString())

    /** Groups [amount] in thousands with the Persian separator and Persian digits, e.g. ۱٬۲۳۴٬۵۶۷. */
    fun grouped(amount: Long): String {
        val negative = amount < 0
        val digits = kotlin.math.abs(amount).toString()
        val grouped =
            buildString {
                val firstGroup = digits.length % 3
                for (i in digits.indices) {
                    if (i != 0 && (i - firstGroup) % 3 == 0) append(GROUP_SEPARATOR)
                    append(digits[i])
                }
            }
        val persian = toPersianDigits(grouped)
        return if (negative) "-$persian" else persian
    }

    /** Formats [amountToman] as a grouped Persian amount with the «تومان» suffix. */
    fun toman(amountToman: Long): String = "${grouped(amountToman)} $TOMAN_SUFFIX"
}
