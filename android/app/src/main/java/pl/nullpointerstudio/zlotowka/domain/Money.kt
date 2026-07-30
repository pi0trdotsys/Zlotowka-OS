package pl.nullpointerstudio.zlotowka.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

@Suppress("DEPRECATION")
private val plLocale = Locale("pl", "PL")

/** Formatowanie PLN: „1 234,56 zł" — grosze jako Long, minus typograficzny. */
fun Long.toPln(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(plLocale).apply {
        currency = Currency.getInstance("PLN")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val text = nf.format(abs(this) / 100.0)
    if (!withSign) return text
    return (if (this < 0) "− " else "+ ") + text
}

/** Skrócona nazwa miesiąca po polsku, np. "lip 2026". */
fun formatMonthYear(epochMillis: Long): String {
    val cal = java.util.Calendar.getInstance(plLocale)
    cal.timeInMillis = epochMillis
    val fmt = java.text.SimpleDateFormat("LLL yyyy", plLocale)
    return fmt.format(cal.time)
}
