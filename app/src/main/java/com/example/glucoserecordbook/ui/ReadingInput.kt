package com.example.glucoserecordbook.ui

object ReadingInput {
    const val MAX_DIGITS = 3
    const val MAX_VALUE = 999

    fun validationMessage(text: String): String? = when {
        text.isBlank() -> "Enter a reading before saving."
        text.length > MAX_DIGITS -> "Please check the number entered."
        text.toIntOrNull() == null || text.toInt() !in 1..MAX_VALUE -> "Please check the number entered."
        else -> null
    }
}
