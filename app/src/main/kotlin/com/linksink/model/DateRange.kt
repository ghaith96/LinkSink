package com.linksink.model

import java.time.Instant
import java.time.temporal.ChronoUnit

data class DateRange(
    val start: Instant,
    val end: Instant
) {
    companion object {
        fun today(): DateRange {
            val now = Instant.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
            return DateRange(startOfDay, now)
        }

        fun thisWeek(): DateRange {
            val now = Instant.now()
            val weekAgo = now.minus(7, ChronoUnit.DAYS)
            return DateRange(weekAgo, now)
        }

        fun thisMonth(): DateRange {
            val now = Instant.now()
            val monthAgo = now.minus(30, ChronoUnit.DAYS)
            return DateRange(monthAgo, now)
        }
    }
}
