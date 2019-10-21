package com.javanapps.moneymanager.core.ui.format

import com.javanapps.moneymanager.core.common.calendar.ShamsiCalendar
import com.javanapps.moneymanager.core.model.MonthKey
import com.javanapps.moneymanager.core.model.ShamsiDate

/** Persian display formatting for Shamsi dates. Pure functions, unit-testable. */
object ShamsiDateFormatter {
    /** e.g. «چهارشنبه ۱ فروردین ۱۴۰۳». */
    fun long(date: ShamsiDate): String {
        val weekday = ShamsiCalendar.weekdayName(date)
        val day = PersianNumber.toPersianDigits(date.day.toLong())
        val month = ShamsiCalendar.monthName(date.month)
        val year = PersianNumber.toPersianDigits(date.year.toLong())
        return "$weekday $day $month $year"
    }

    /** e.g. «۱۴۰۳/۰۱/۰۱». */
    fun short(date: ShamsiDate): String {
        val year = PersianNumber.toPersianDigits(date.year.toString())
        val month = PersianNumber.toPersianDigits(date.month.toString().padStart(2, '0'))
        val day = PersianNumber.toPersianDigits(date.day.toString().padStart(2, '0'))
        return "$year/$month/$day"
    }

    /** e.g. «۱۳:۴۵». */
    fun time(date: ShamsiDate): String {
        val hour = PersianNumber.toPersianDigits(date.hour.toString().padStart(2, '0'))
        val minute = PersianNumber.toPersianDigits(date.minute.toString().padStart(2, '0'))
        return "$hour:$minute"
    }

    /** e.g. «فروردین ۱۴۰۳». */
    fun monthTitle(monthKey: MonthKey): String {
        val month = ShamsiCalendar.monthName(monthKey.month)
        val year = PersianNumber.toPersianDigits(monthKey.year.toLong())
        return "$month $year"
    }
}
