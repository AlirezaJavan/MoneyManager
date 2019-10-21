package com.javanapps.moneymanager.core.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PersianNumberTest {
    @Test
    fun `converts latin digits to persian`() {
        assertThat(PersianNumber.toPersianDigits("2024")).isEqualTo("۲۰۲۴")
    }

    @Test
    fun `groups thousands with persian separator`() {
        assertThat(PersianNumber.grouped(1234567)).isEqualTo("۱٬۲۳۴٬۵۶۷")
        assertThat(PersianNumber.grouped(0)).isEqualTo("۰")
        assertThat(PersianNumber.grouped(999)).isEqualTo("۹۹۹")
        assertThat(PersianNumber.grouped(1000)).isEqualTo("۱٬۰۰۰")
    }

    @Test
    fun `negative amounts keep the sign`() {
        assertThat(PersianNumber.grouped(-2500)).isEqualTo("-۲٬۵۰۰")
    }

    @Test
    fun `toman appends the suffix`() {
        assertThat(PersianNumber.toman(45000)).isEqualTo("۴۵٬۰۰۰ تومان")
    }
}
