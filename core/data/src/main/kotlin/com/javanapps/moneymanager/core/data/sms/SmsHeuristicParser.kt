package com.javanapps.moneymanager.core.data.sms

import com.javanapps.moneymanager.core.data.DefaultCategories
import com.javanapps.moneymanager.core.model.BankSmsRule
import com.javanapps.moneymanager.core.model.ParsedSms
import com.javanapps.moneymanager.core.model.TransactionType
import javax.inject.Inject

/**
 * Data-driven SMS parser. Only messages from senders matching a defined or user-taught
 * [BankSmsRule] are parsed as bank transactions — every other sender is ignored.
 *
 * OTP / ad detection runs before monetary parsing and returns null for non-financial messages.
 */
class SmsHeuristicParser
    @Inject
    constructor() {
        fun parse(
            body: String,
            sender: String,
            rules: List<BankSmsRule>,
        ): ParsedSms? {
            if (isOtpOrAd(body)) return null

            // Only senders matching a user-defined/learned rule are considered.
            for (rule in rules.filter { it.enabled }) {
                if (senderMatches(sender, rule.senderPattern)) {
                    val result = parseWithRule(body, sender, rule) ?: continue
                    return result
                }
            }

            return null
        }

        private fun parseWithRule(
            body: String,
            sender: String,
            rule: BankSmsRule,
        ): ParsedSms? {
            val (amount, matchValue) = extractAmountAndMatch(body, rule.amountInRial) ?: return null
            val type =
                detectTypeByKeywords(body, rule.incomeKeywords, rule.expenseKeywords)
                    ?: detectTypeBySign(matchValue)
                    ?: return null
            return ParsedSms(
                amountToman = amount,
                type = type,
                bankName = rule.bankName,
                confidence = CONFIDENCE_RULE_BASED,
                rawBody = body,
                sender = sender,
                ruleId = rule.id,
            )
        }

        // ─── OTP / ad filter ────────────────────────────────────────────────────

        private val otpPatterns =
            listOf(
                Regex("رمز\\s*(دوم|پویا|یکبار|موقت)"),
                Regex("(?:کد|code)\\s*(?:تأیید|فعالسازی|تایید|تکست)"),
                Regex("[Cc][Oo][Dd][Ee]\\s*:"),
                Regex("OTP"),
                Regex("password", RegexOption.IGNORE_CASE),
                Regex("https?://"),
                Regex("لینک\\s*تأیید"),
            )

        /**
         * Returns true if the SMS is likely an OTP or advertisement.
         * Financial indicators like "balance" (مانده/موجودی) override OTP patterns.
         */
        private fun isOtpOrAd(body: String): Boolean {
            if (body.contains("مانده") || body.contains("موجودی")) return false
            return otpPatterns.any { it.containsMatchIn(body) }
        }

        // ─── Amount extraction ────────────────────────────────────────────────────

        // Matches Persian/Arabic-Indic/Latin digits optionally grouped with commas or Persian separators.
        // Also supports leading +/- signs and various unicode minus/hyphen characters.
        private val amountRegex =
            Regex(
                "[+\\-−±]?[۰-۹0-9٠-٩][۰-۹0-9٠-٩,،٬\\u200c]{0,20}[۰-۹0-9٠-٩]",
            )

        private fun extractAmountAndMatch(
            body: String,
            amountInRial: Boolean?,
        ): Pair<Long, String>? {
            val matches = amountRegex.findAll(body).toList()
            if (matches.isEmpty()) return null

            val bestMatch = matches.maxByOrNull { calculateMatchScore(body, it) } ?: return null
            val rawValue = bestMatch.value
            val cleaned = rawValue.replace(Regex("[+\\-−±,،٬\\s\\u200c]"), "")
            val numeric = toLatinDigits(cleaned).toLongOrNull() ?: return null
            if (numeric <= 0) return null

            val isRial = amountInRial ?: detectCurrency(body)
            val amountToman = if (isRial) numeric / 10 else numeric
            return amountToman to rawValue
        }

        private fun calculateMatchScore(
            body: String,
            match: MatchResult,
        ): Int {
            var score = 0
            val value = match.value
            val cleanedValue = value.replace(Regex("[+\\-−±,،٬\\s\\u200c]"), "")
            val start = match.range.first
            val end = match.range.last

            if (value.startsWith('+') || value.startsWith('-') || value.startsWith('−')) score += 70

            val before = body.substring(0, start).takeLast(30)
            val after = body.substring((end + 1).coerceAtMost(body.length)).take(20)

            if (after.contains("ریال") || after.contains("ريال") || after.contains("تومان")) score += 80
            if (before.contains("مبلغ") ||
                before.contains("واریز") ||
                before.contains("برداشت") ||
                before.contains("خرید") ||
                before.contains("انتقال") ||
                before.contains("واريز") ||
                before.contains("پرداخت") ||
                before.contains("خريد")
            ) {
                score += 40
            }

            if (value.contains(',') || value.contains('،') || value.contains('٬')) score += 15
            if (cleanedValue.length in 4..12) score += 20
            score += (cleanedValue.length - 3).coerceIn(0, 10) * 2

            if (before.contains("مانده")) score -= 100
            if (after.startsWith('/') || after.startsWith(':') || after.startsWith('_')) score -= 60
            if (before.endsWith('/') || before.endsWith(':') || before.endsWith('_')) score -= 60

            if (before.contains("حساب") || before.contains("کارت") || before.contains("كارت")) {
                val immediateBefore = body.substring(0, start).takeLast(10)
                if (!immediateBefore.contains("مبلغ") &&
                    !immediateBefore.contains("واریز") &&
                    !immediateBefore.contains("برداشت") &&
                    !immediateBefore.contains("خرید")
                ) {
                    score -= 50
                }
            }

            if (before.endsWith('.') || after.startsWith('.')) score -= 80
            if (before.matches(Regex(".*[0-9]-$")) || after.matches(Regex("^-.*"))) score -= 70

            return score
        }

        private fun detectCurrency(body: String): Boolean {
            // If body mentions ریال or is silent → treat as Rial (most Iranian banks send in Rial)
            // If body explicitly mentions تومان → Toman already
            return "تومان" !in body
        }

        // ─── Direction detection ──────────────────────────────────────────────────

        private val defaultIncomeKeywords =
            listOf(
                "واریز",
                "بستانکار",
                "دریافت",
                "افزایش موجودی",
                "سپرده",
                "برگشت",
                "واريز",
            )
        private val defaultExpenseKeywords =
            listOf(
                "برداشت",
                "بدهکار",
                "خرید",
                "پرداخت",
                "کسر",
                "کارمزد",
                "انتقال وجه از",
                "مبلغ پرداخت",
                "خريد",
                "انتقال:",
                "انتقال به",
            )

        private fun detectTypeByKeywords(
            body: String,
            incomeKeywords: List<String>,
            expenseKeywords: List<String>,
        ): TransactionType? {
            if (incomeKeywords.any { it in body }) return TransactionType.INCOME
            if (expenseKeywords.any { it in body }) return TransactionType.EXPENSE
            return null
        }

        private fun detectTypeBySign(matchValue: String): TransactionType? {
            if (matchValue.startsWith('+')) return TransactionType.INCOME
            if (matchValue.startsWith('-') || matchValue.startsWith('−')) return TransactionType.EXPENSE
            return null
        }

        // ─── Helpers ─────────────────────────────────────────────────────────────

        private fun senderMatches(
            sender: String,
            pattern: String,
        ): Boolean {
            val normalizedSender = normalizeSenderId(sender)
            val normalizedPattern = normalizeSenderId(pattern)
            return normalizedSender.equals(normalizedPattern, ignoreCase = true) ||
                normalizedSender.contains(normalizedPattern, ignoreCase = true)
        }

        /**
         * Strips punctuation commonly introduced when a phone number is copy-pasted from a contacts
         * app or dialer (spaces, hyphens, parentheses) so a rule taught from "+1 650 555-6789" still
         * matches an incoming SMS whose sender address arrives as "+16505556789".
         */
        private fun normalizeSenderId(value: String): String = value.filterNot { it == ' ' || it == '-' || it == '(' || it == ')' }

        private fun toLatinDigits(text: String): String =
            buildString(text.length) {
                for (ch in text) {
                    append(
                        when (ch) {
                            in '۰'..'۹' -> '0' + (ch - '۰')
                            in '٠'..'٩' -> '0' + (ch - '٠')
                            else -> ch
                        },
                    )
                }
            }

        companion object {
            private const val CONFIDENCE_RULE_BASED = 90
        }

        /**
         * Attempts to infer a [BankSmsRule] from a sample SMS body + sender. Returns a partially
         * filled rule; the user should provide [bankName] and may tweak keywords.
         */
        fun learnFromSample(
            body: String,
            sender: String,
            bankName: String,
        ): BankSmsRule? {
            if (isOtpOrAd(body)) return null
            val amountInRial = detectCurrency(body)
            val incomeKws = defaultIncomeKeywords.filter { it in body }.toMutableList()
            val expenseKws = defaultExpenseKeywords.filter { it in body }.toMutableList()

            val (_, matchValue) = extractAmountAndMatch(body, amountInRial) ?: return null

            // If no keywords found, use signs as keywords
            if (incomeKws.isEmpty() && expenseKws.isEmpty()) {
                if (matchValue.startsWith('+')) {
                    incomeKws.add("+")
                } else if (matchValue.startsWith('-') || matchValue.startsWith('−')) {
                    expenseKws.add("-")
                }
            }

            if (incomeKws.isEmpty() && expenseKws.isEmpty()) return null

            return BankSmsRule(
                id = BankSmsRule.NO_ID,
                senderPattern = normalizeSenderId(sender),
                bankName = bankName,
                incomeKeywords = incomeKws.ifEmpty { defaultIncomeKeywords },
                expenseKeywords = expenseKws.ifEmpty { defaultExpenseKeywords },
                amountInRial = amountInRial,
                defaultCategory = DefaultCategories.MISC,
                sampleBody = body,
                enabled = true,
            )
        }
    }
