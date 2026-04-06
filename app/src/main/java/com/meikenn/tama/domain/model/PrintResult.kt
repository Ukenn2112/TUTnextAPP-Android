package com.meikenn.tama.domain.model

import java.text.DateFormat
import java.util.Date
import java.util.Locale

data class PrintResult(
    val printNumber: String,
    val fileName: String,
    val expiryDate: Long,
    val pageCount: Int,
    val duplex: String,
    val fileSize: String,
    val nUp: String
) {
    val formattedExpiryDate: String
        get() {
            val date = Date(expiryDate)
            val formatter = DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.SHORT,
                Locale.JAPAN
            )
            return formatter.format(date)
        }
}
