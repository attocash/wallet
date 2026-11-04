package cash.atto.wallet.password

import java.util.Calendar
import java.util.GregorianCalendar

internal actual fun currentPasswordReferenceYear(): Int = GregorianCalendar().get(Calendar.YEAR)
