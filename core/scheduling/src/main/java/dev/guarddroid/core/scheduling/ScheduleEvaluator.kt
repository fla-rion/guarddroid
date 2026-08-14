package dev.guarddroid.core.scheduling

import dev.guarddroid.core.database.entity.ScheduleEntity
import dev.guarddroid.core.database.entity.TimeWindow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleEvaluator @Inject constructor() {

    fun isCurrentlyAllowed(schedule: ScheduleEntity): Boolean =
        isAllowedAt(schedule, Calendar.getInstance())

    fun isAllowedAt(schedule: ScheduleEntity, calendar: Calendar): Boolean {
        if (!isDayEnabled(schedule, calendar)) return false
        val windows = parseTimeWindows(schedule.timeWindowsJson)
        if (windows.isEmpty()) return true
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return windows.any { window ->
            val startMinutes = window.startHour * 60 + window.startMinute
            val endMinutes = window.endHour * 60 + window.endMinute
            if (endMinutes > startMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                // Overnight window (e.g., 22:00–06:00)
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
        }
    }

    fun getNextChangeTimeMs(schedule: ScheduleEntity): Long? {
        val now = Calendar.getInstance()
        val windows = parseTimeWindows(schedule.timeWindowsJson)
        if (windows.isEmpty()) return null

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        // Find the next boundary within today
        val boundaries = mutableListOf<Int>()
        windows.forEach { w ->
            boundaries.add(w.startHour * 60 + w.startMinute)
            boundaries.add(w.endHour * 60 + w.endMinute)
        }
        boundaries.sort()

        val nextBoundaryToday = boundaries.firstOrNull { it > currentMinutes }
        return if (nextBoundaryToday != null) {
            val minutesUntil = nextBoundaryToday - currentMinutes
            (minutesUntil * 60 - currentSeconds) * 1000L
        } else {
            // Next boundary is tomorrow at first boundary
            val firstBoundary = boundaries.firstOrNull() ?: return null
            val minutesUntilMidnight = 24 * 60 - currentMinutes
            ((minutesUntilMidnight + firstBoundary) * 60 - currentSeconds) * 1000L
        }
    }

    private fun isDayEnabled(schedule: ScheduleEntity, calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY=2, TUESDAY=3, ... SUNDAY=1
        val bit = when (dayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 8
            Calendar.FRIDAY -> 16
            Calendar.SATURDAY -> 32
            Calendar.SUNDAY -> 64
            else -> 0
        }
        return (schedule.enabledDays and bit) != 0
    }

    fun parseTimeWindows(json: String): List<TimeWindow> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            TimeWindow(
                startHour = obj.getInt("startHour"),
                startMinute = obj.getInt("startMinute"),
                endHour = obj.getInt("endHour"),
                endMinute = obj.getInt("endMinute")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun timeWindowsToJson(windows: List<TimeWindow>): String {
        val arr = JSONArray()
        windows.forEach { w ->
            arr.put(JSONObject().apply {
                put("startHour", w.startHour)
                put("startMinute", w.startMinute)
                put("endHour", w.endHour)
                put("endMinute", w.endMinute)
            })
        }
        return arr.toString()
    }
}
