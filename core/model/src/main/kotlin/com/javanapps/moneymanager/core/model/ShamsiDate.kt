package com.javanapps.moneymanager.core.model

import kotlinx.serialization.Serializable

/**
 * A date and time on the Shamsi (Jalali / Persian) calendar.
 *
 * Mirrors the fields the legacy database stored per transaction ([year], [month], [day], plus hour
 * and minute), keeping the app fully Shamsi-native. Conversions to/from the Gregorian calendar and
 * helpers (month length, names, weekday) live in `:core:common` `ShamsiCalendar` so this type stays
 * a pure, framework-free value object.
 *
 * @param year Shamsi year (e.g. 1403).
 * @param month Shamsi month, 1..12.
 * @param day Shamsi day of month, 1..31.
 * @param hour Hour of day, 0..23.
 * @param minute Minute, 0..59.
 */
@Serializable
data class ShamsiDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
) : Comparable<ShamsiDate> {
    /** The year/month this date belongs to, useful for monthly grouping and navigation. */
    val monthKey: MonthKey get() = MonthKey(year, month)

    override fun compareTo(other: ShamsiDate): Int = COMPARATOR.compare(this, other)

    companion object {
        private val COMPARATOR =
            compareBy<ShamsiDate>(
                { it.year },
                { it.month },
                { it.day },
                { it.hour },
                { it.minute },
            )
    }
}
