package com.javanapps.moneymanager.core.data.sms

import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.model.BankSmsRule

/**
 * Seed rules for common Iranian banks based on typical SMS formats.
 */
object DefaultBankSmsRules {
    fun all(): List<BankSmsRule> =
        listOf(
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "Bank Melli",
                bankName = "بانک ملی",
                incomeKeywords = listOf("واریز", "واريز", "انتقال از"),
                expenseKeywords = listOf("برداشت", "خرید", "خريد", "انتقال به", "پرداخت", "انتقال:"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "بانك ملي ايران\nكارت: 0465\nخريد: 2,330,000\nمانده: 60,514,629\nتاريخ: 1405/04/04",
            ),
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "Sina",
                bankName = "بانک سینا",
                incomeKeywords = listOf("واريز به", "واریز به"),
                expenseKeywords = listOf("برداشت از"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "*بانک سينا*\nبرداشت از ‪187-12-5304730-1‬\nمبلغ  500,000 ريال\nمانده 46,673,222 ريال",
            ),
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "Sepah",
                bankName = "بانک سپه",
                incomeKeywords = listOf("واریز", "واريز"),
                expenseKeywords = listOf("برداشت", "خرید", "خريد", "پرداخت"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "بانک سپه\nخريد پايانه فروش: 900,000\nحساب :‪70430500767114‬\nمانده:13,126,187",
            ),
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "Tejarat",
                bankName = "بانک تجارت",
                incomeKeywords = listOf("واریز", "واريز"),
                expenseKeywords = listOf("برداشت", "خرید", "خريد", "پرداخت", "قبض"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "بانک تجارت\nشماره کارت: 5056****585983\nنوع تراکنش: پرداخت قبض\nمبلغ: 1,000,000 ريال",
            ),
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "Saman",
                bankName = "بانک سامان",
                incomeKeywords = listOf("واریز", "واريز", "+"),
                expenseKeywords = listOf("برداشت", "خرید", "خريد", "پرداخت", "-"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "بانک سامان\nبرداشت:‎-1,000,000,000‎\nحساب:152900499640\nمانده:4,273,043,600",
            ),
            BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = "982000",
                bankName = "بلو بانک",
                incomeKeywords = listOf("+", "واریز", "واريز"),
                expenseKeywords = listOf("-", "−", "برداشت", "خرید", "خريد"),
                amountInRial = true,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = "1709.116.17132644.1\n-10,780,000\nمانده: 9,795,629",
            ),
        )
}
