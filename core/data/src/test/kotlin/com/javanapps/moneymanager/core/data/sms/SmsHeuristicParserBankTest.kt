package com.javanapps.moneymanager.core.data.sms

import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.model.BankSmsRule
import com.javanapps.moneymanager.core.model.TransactionType
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class SmsHeuristicParserBankTest {
    private lateinit var parser: SmsHeuristicParser
    private val noRules = emptyList<BankSmsRule>()

    @Before
    fun setUp() {
        parser = SmsHeuristicParser()
    }

    @Test
    fun `Bank Sina - Income`() {
        val body =
            """
            ----------------------------------- 
            *بانک سينا*
            واريز به ‪187-12-5304730-1‬
            مبلغ  71,000,000 ريال
            مانده 118,407,422 ريال
            زمان : 1405/4/7 ; 10:34:04
            """.trimIndent()
        val result = parser.parse(body, "BankSina", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(7_100_000L)
        assertThat(result.type).isEqualTo(TransactionType.INCOME)
    }

    @Test
    fun `Bank Sina - Expense`() {
        val body =
            """
            ----------------------------------- 
            *بانک سينا*
            برداشت از ‪187-12-5304730-1‬
            مبلغ  500,000 ريال
            مانده 46,673,222 ريال
            زمان : 1405/4/8 ; 12:06:14
            """.trimIndent()
        val result = parser.parse(body, "BankSina", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(50_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Bank Melli - Transfer`() {
        val body =
            """
            ----------------------------------- 
            بانك ملي ايران
            كارت: 0465
            انتقال: 7,009,000
            مانده: 47,505,179
            تاريخ: 1405/04/06
            ساعت: 22:22:04
            """.trimIndent()
        val result = parser.parse(body, "BankMelli", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(700_900L) // Assuming Rial if not specified
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Bank Melli - Purchase`() {
        val body =
            """
            ----------------------------------- 
            بانك ملي ايران
            كارت: 0465
            خريد: 2,330,000
            مانده: 60,514,629
            تاريخ: 1405/04/04
            ساعت: 17:14:22
            """.trimIndent()
        val result = parser.parse(body, "BankMelli", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(233_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Bank Saman - Large Amount`() {
        val body =
            """
            برداشت پل:‎-71,000,000‎
            حساب:152900499640
            مانده:2,603,392,600
            0407-10:33
            """.trimIndent()
        val result = parser.parse(body, "BankSaman", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(7_100_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Bank Saman - Deposit`() {
        val body =
            """
            واريز:‎+8,000,000,000‎
            حساب:152900499640
            مانده:8,001,495,000
            0325-07:48
            """.trimIndent()
        val result = parser.parse(body, "BankSaman", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(800_000_000L)
        assertThat(result.type).isEqualTo(TransactionType.INCOME)
    }

    @Test
    fun `Bank Sepah - Terminal Purchase`() {
        val body =
            """
            بانک سپه
            خريد پايانه فروش: 900,000
            حساب :‪70430500767114‬
            مانده:13,126,187
            4/8-10:53
            """.trimIndent()
        val result = parser.parse(body, "BankSepah", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(90_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Bank Tejarat - Bill Payment`() {
        val body =
            """
            بانک تجارت
            شماره کارت: 5056****585983
            نوع تراکنش: پرداخت قبض
            مبلغ: 1,000,000 ريال
            از طريق: اینترنت بانک
            مانده: 1,356,500,045 ريال
            تاريخ:1405/04/07
            ساعت:16:21
            """.trimIndent()
        val result = parser.parse(body, "BankTejarat", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(100_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `Problematic Generic SMS`() {
        val body =
            """
            1709.116.17132644.1
            -10,780,000 
            02/15_07:33
            مانده: 9,795,629
            """.trimIndent()
        val result = parser.parse(body, "982000", noRules)
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(1_078_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }
}
