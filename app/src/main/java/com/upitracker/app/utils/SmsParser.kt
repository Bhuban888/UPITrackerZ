package com.upitracker.app.utils

import com.upitracker.app.data.model.Transaction
import com.upitracker.app.data.model.TransactionType

object SmsParser {

    // Patterns for major Indian banks and UPI apps
    private val DEBIT_KEYWORDS = listOf(
        "debited", "debit", "paid", "sent", "payment of", "transferred", "spent"
    )
    private val CREDIT_KEYWORDS = listOf(
        "credited", "credit", "received", "refund", "cashback"
    )

    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:INR|Rs\.?|₹)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+(?:\.\d{1,2})?)\s*(?:INR|Rs\.?)""", RegexOption.IGNORE_CASE)
    )

    private val UPI_REF_PATTERNS = listOf(
        Regex("""(?:UPI\s*Ref\.?\s*No\.?|UPI\s*Ref|Ref\s*No|RefNo|UTR|transaction\s*id)[:\s#]*([\w\d]+)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Ref\s*#)[:\s]*([\w\d]+)""", RegexOption.IGNORE_CASE)
    )

    private val UPI_ID_PATTERN = Regex("""[\w.\-+]+@[\w]+""")

    private val BANK_PATTERNS = mapOf(
        "HDFC" to Regex("""HDFC""", RegexOption.IGNORE_CASE),
        "SBI" to Regex("""SBI|State Bank""", RegexOption.IGNORE_CASE),
        "ICICI" to Regex("""ICICI""", RegexOption.IGNORE_CASE),
        "Axis" to Regex("""Axis""", RegexOption.IGNORE_CASE),
        "Kotak" to Regex("""Kotak""", RegexOption.IGNORE_CASE),
        "Yes Bank" to Regex("""Yes\s*Bank""", RegexOption.IGNORE_CASE),
        "PNB" to Regex("""PNB|Punjab National""", RegexOption.IGNORE_CASE),
        "Paytm" to Regex("""Paytm""", RegexOption.IGNORE_CASE),
        "PhonePe" to Regex("""PhonePe""", RegexOption.IGNORE_CASE),
        "Google Pay" to Regex("""GPay|Google\s*Pay""", RegexOption.IGNORE_CASE),
    )

    fun parse(sms: String, sender: String = ""): Transaction? {
        val lower = sms.lowercase()

        // Only process UPI / banking SMS
        if (!isUpiSms(lower)) return null

        val amount = extractAmount(sms) ?: return null
        val type = detectType(lower)
        val refId = extractRefId(sms)
        val upiId = UPI_ID_PATTERN.find(sms)?.value ?: ""
        val bankName = detectBank(sms, sender)
        val description = buildDescription(sms, type, upiId)
        val category = suggestCategory(sms)

        return Transaction(
            amount = amount,
            type = type,
            description = description,
            upiId = upiId,
            refId = refId,
            bankName = bankName,
            rawSms = sms,
            category = category
        )
    }

    private fun isUpiSms(lower: String): Boolean {
        val upiKeywords = listOf("upi", "imps", "neft", "debited", "credited", "paytm", "phonepe", "gpay", "bhim")
        return upiKeywords.any { it in lower }
    }

    private fun extractAmount(sms: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(sms) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            return raw.toDoubleOrNull()
        }
        return null
    }

    private fun detectType(lower: String): TransactionType {
        val debitScore = DEBIT_KEYWORDS.count { it in lower }
        val creditScore = CREDIT_KEYWORDS.count { it in lower }
        return if (creditScore > debitScore) TransactionType.CREDIT else TransactionType.DEBIT
    }

    private fun extractRefId(sms: String): String {
        for (pattern in UPI_REF_PATTERNS) {
            val match = pattern.find(sms)
            if (match != null) return match.groupValues[1]
        }
        return ""
    }

    private fun detectBank(sms: String, sender: String): String {
        val combined = "$sms $sender"
        for ((name, pattern) in BANK_PATTERNS) {
            if (pattern.containsMatchIn(combined)) return name
        }
        return "Unknown Bank"
    }

    private fun buildDescription(sms: String, type: TransactionType, upiId: String): String {
        return when {
            upiId.isNotEmpty() -> if (type == TransactionType.DEBIT) "Paid to $upiId" else "Received from $upiId"
            type == TransactionType.DEBIT -> "UPI Debit"
            else -> "UPI Credit"
        }
    }

    fun suggestCategory(sms: String): String {
        val lower = sms.lowercase()
        return when {
            listOf("swiggy", "zomato", "food", "restaurant", "cafe", "eat").any { it in lower } -> "Food & Dining"
            listOf("uber", "ola", "rapido", "metro", "irctc", "train", "bus", "flight").any { it in lower } -> "Travel"
            listOf("amazon", "flipkart", "myntra", "shop", "store", "mart").any { it in lower } -> "Shopping"
            listOf("netflix", "spotify", "prime", "hotstar", "movie", "entertainment").any { it in lower } -> "Entertainment"
            listOf("electricity", "water", "gas", "bill", "recharge", "dth", "broadband").any { it in lower } -> "Bills & Utilities"
            listOf("hospital", "medicine", "pharmacy", "doctor", "health", "clinic").any { it in lower } -> "Healthcare"
            listOf("college", "school", "tuition", "course", "education").any { it in lower } -> "Education"
            listOf("salary", "income", "payroll").any { it in lower } -> "Income"
            else -> "Uncategorized"
        }
    }
}
