package com.hackathon.finni.core.util

import androidx.compose.runtime.Composable
import com.hackathon.finni.resources.*
import kotlinx.datetime.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Основные настройки форматирования даты
 */
data class DateFormatOptions(
    val showRelative: Boolean = true,           // "Сегодня", "Вчера"
    val showYearIfDifferent: Boolean = true,    // Добавлять год, если он не текущий
    val includeDayOfWeek: Boolean = false,      // Добавлять "понедельник", "вторник"
    val capitalize: Boolean = true,             // Начинать с большой буквы
    val timeZone: TimeZone = TimeZone.currentSystemDefault()
)

// --- Suspend API (для ViewModel/Repository) ---

suspend fun Long.formatDateTime(
    options: DateFormatOptions = DateFormatOptions(),
    includeTime: Boolean = true
): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.toLocalDateTime(options.timeZone).formatDateTime(options, includeTime)
}

suspend fun LocalDateTime.formatDateTime(
    options: DateFormatOptions = DateFormatOptions(),
    includeTime: Boolean = true
): String = formatDateTimeInternal(options, includeTime) { getString(it) }

suspend fun LocalDate.formatDate(
    options: DateFormatOptions = DateFormatOptions()
): String {
    val now = Clock.System.now().toLocalDateTime(options.timeZone).date
    return formatDateInternal(now, options) { getString(it) }
}

// --- Compose API (для использования в @Composable) ---

@Composable
fun LocalDateTime.formatDateTimeCompose(
    options: DateFormatOptions = DateFormatOptions(),
    includeTime: Boolean = true
): String = formatDateTimeInternal(options, includeTime) { stringResource(it) }

@Composable
fun LocalDate.formatDateCompose(
    options: DateFormatOptions = DateFormatOptions()
): String {
    val now = Clock.System.now().toLocalDateTime(options.timeZone).date
    return formatDateInternal(now, options) { stringResource(it) }
}

// --- Core (Internal Logic) ---

/**
 * Внутренняя логика форматирования времени. 
 * Не привязан к способу получения строк.
 */
fun LocalTime.formatTime(): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

/**
 * Общая логика сборки даты и времени.
 */
private inline fun LocalDateTime.formatDateTimeInternal(
    options: DateFormatOptions,
    includeTime: Boolean,
    resolve: (StringResource) -> String
): String {
    val now = Clock.System.now().toLocalDateTime(options.timeZone).date
    val dateStr = date.formatDateInternal(now, options, resolve)
    if (!includeTime) return dateStr

    val timeStr = time.formatTime()
    return "$dateStr, $timeStr"
}

/**
 * Базовая логика форматирования LocalDate.
 * Вся "математика" и выбор ресурсов здесь.
 */
private inline fun LocalDate.formatDateInternal(
    now: LocalDate,
    options: DateFormatOptions,
    resolve: (StringResource) -> String
): String {
    val daysDiff = now.daysUntil(this)

    // 1. Относительные даты (Сегодня/Вчера/Завтра)
    if (options.showRelative) {
        val relativeRes = when (daysDiff) {
            0 -> Res.string.common_datetime_today
            -1 -> Res.string.common_datetime_yesterday
            1 -> Res.string.common_datetime_tomorrow
            else -> null
        }
        if (relativeRes != null) {
            val str = resolve(relativeRes)
            return if (options.capitalize) str.replaceFirstChar { it.uppercase() } else str
        }
    }

    // 2. Сборка строки: "День Месяц"
    val monthStr = resolve(getMonthResource(month))
    var result = "$day $monthStr"

    // 3. Год (если отличается от текущего)
    if (options.showYearIfDifferent && year != now.year) {
        result += " $year ${resolve(Res.string.common_datetime_year_suffix)}"
    }

    // 4. День недели
    if (options.includeDayOfWeek) {
        result += ", ${resolve(getWeekDayResource(dayOfWeek))}"
    }

    // 5. Капитализация
    return if (options.capitalize) {
        result.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    } else {
        result
    }
}

// --- Resource Mapping ---

private fun getMonthResource(month: Month): StringResource = when (month) {
    Month.JANUARY -> Res.string.common_datetime_month_1
    Month.FEBRUARY -> Res.string.common_datetime_month_2
    Month.MARCH -> Res.string.common_datetime_month_3
    Month.APRIL -> Res.string.common_datetime_month_4
    Month.MAY -> Res.string.common_datetime_month_5
    Month.JUNE -> Res.string.common_datetime_month_6
    Month.JULY -> Res.string.common_datetime_month_7
    Month.AUGUST -> Res.string.common_datetime_month_8
    Month.SEPTEMBER -> Res.string.common_datetime_month_9
    Month.OCTOBER -> Res.string.common_datetime_month_10
    Month.NOVEMBER -> Res.string.common_datetime_month_11
    Month.DECEMBER -> Res.string.common_datetime_month_12
}

private fun getWeekDayResource(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> Res.string.common_datetime_weekday_1
    DayOfWeek.TUESDAY -> Res.string.common_datetime_weekday_2
    DayOfWeek.WEDNESDAY -> Res.string.common_datetime_weekday_3
    DayOfWeek.THURSDAY -> Res.string.common_datetime_weekday_4
    DayOfWeek.FRIDAY -> Res.string.common_datetime_weekday_5
    DayOfWeek.SATURDAY -> Res.string.common_datetime_weekday_6
    DayOfWeek.SUNDAY -> Res.string.common_datetime_weekday_7
}
