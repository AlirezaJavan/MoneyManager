package com.javanapps.moneymanager.core.data.sms

import com.google.common.truth.Truth.assertThat
import com.javanapps.moneymanager.core.model.BankSmsRule
import com.javanapps.moneymanager.core.model.TransactionType
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class SmsHeuristicParserBankTest {
    private lateinit var parser: SmsHeuristicParser

    @Before
    fun setUp() {
        parser = SmsHeuristicParser()
    }

    private fun ruleFor(
        sender: String,
        bankName: String,
        incomeKeywords: List<String> = emptyList(),
        expenseKeywords: List<String> = emptyList(),
    ) = BankSmsRule(
        id = 1L,
        senderPattern = sender,
        bankName = bankName,
        incomeKeywords = incomeKeywords,
        expenseKeywords = expenseKeywords,
        amountInRial = true,
        defaultCategory = "متفرقه",
        sampleBody = "",
    )

    // Unrelated senders must never be parsed, even if the body looks financial.
    @Test
    fun `unknown sender is ignored even with a bank-like body`() {
        val body = "واریز 5,000,000 ریال به حساب شما"
        assertThat(parser.parse(body, "SomeRandomApp", emptyList())).isNull()
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
        val rule = ruleFor("BankSina", "بانک سینا", incomeKeywords = listOf("واريز"))
        val result = parser.parse(body, "BankSina", listOf(rule))
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
        val rule = ruleFor("BankSina", "بانک سینا", expenseKeywords = listOf("برداشت"))
        val result = parser.parse(body, "BankSina", listOf(rule))
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
        val rule = ruleFor("BankMelli", "بانک ملی", expenseKeywords = listOf("انتقال:"))
        val result = parser.parse(body, "BankMelli", listOf(rule))
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
        val rule = ruleFor("BankMelli", "بانک ملی", expenseKeywords = listOf("خريد"))
        val result = parser.parse(body, "BankMelli", listOf(rule))
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
        val rule = ruleFor("BankSaman", "بانک سامان", expenseKeywords = listOf("برداشت"))
        val result = parser.parse(body, "BankSaman", listOf(rule))
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
        val rule = ruleFor("BankSaman", "بانک سامان", incomeKeywords = listOf("واريز"))
        val result = parser.parse(body, "BankSaman", listOf(rule))
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
        val rule = ruleFor("BankSepah", "بانک سپه", expenseKeywords = listOf("خريد"))
        val result = parser.parse(body, "BankSepah", listOf(rule))
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
        val rule = ruleFor("BankTejarat", "بانک تجارت", expenseKeywords = listOf("پرداخت"))
        val result = parser.parse(body, "BankTejarat", listOf(rule))
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(100_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    // Regression test for a rule taught from a copy-pasted phone number such as
    // "+1 650 555-6789": the sender address delivered by Telephony rarely keeps the same
    // spacing/dash formatting the user pasted into the rule (e.g. it arrives as
    // "+16505556789"), so a naive equals/contains match on the raw strings silently fails
    // to detect the SMS even though the number is otherwise identical.
    @Test
    fun `sender pattern with spaces and dashes still matches an unformatted incoming sender`() {
        val body = "واریز 5,000,000 ریال به حساب شما"
        val rule = ruleFor("+1 650 555-6789", "بانک آمریکایی", incomeKeywords = listOf("واریز"))

        val result = parser.parse(body, "+16505556789", listOf(rule))

        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(TransactionType.INCOME)
    }

    @Test
    fun `sender pattern taught with formatting also matches the exact same formatting`() {
        val body = "واریز 5,000,000 ریال به حساب شما"
        val rule = ruleFor("+1 650 555-6789", "بانک آمریکایی", incomeKeywords = listOf("واریز"))

        val result = parser.parse(body, "+1 650 555-6789", listOf(rule))

        assertThat(result).isNotNull()
    }

    @Test
    fun `learnFromSample strips spacing and dashes from a pasted phone number sender`() {
        val body = "واریز 5,000,000 ریال به حساب شما"

        val rule = parser.learnFromSample(body, "+1 650 555-6789", "بانک آمریکایی")

        assertThat(rule).isNotNull()
        assertThat(rule!!.senderPattern).isEqualTo("+16505556789")
    }

    @Test
    fun `Generic SMS from a defined sender falls back to sign detection`() {
        val body =
            """
            1709.116.17132644.1
            -10,780,000
            02/15_07:33
            مانده: 9,795,629
            """.trimIndent()
        // No income/expense keyword in the body; sender is still recognized via a defined rule,
        // so the sign of the amount determines the transaction type.
        val rule = ruleFor("982000", "982000")
        val result = parser.parse(body, "982000", listOf(rule))
        assertThat(result).isNotNull()
        assertThat(result!!.amountToman).isEqualTo(1_078_000L)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }
}
