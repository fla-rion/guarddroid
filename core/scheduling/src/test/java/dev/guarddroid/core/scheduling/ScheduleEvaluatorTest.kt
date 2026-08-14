package dev.guarddroid.core.scheduling

import dev.guarddroid.core.database.entity.ScheduleEntity
import dev.guarddroid.core.database.entity.TimeWindow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ScheduleEvaluatorTest {

    private lateinit var evaluator: ScheduleEvaluator

    @Before
    fun setup() {
        evaluator = ScheduleEvaluator()
    }

    private fun makeSchedule(enabledDays: Int, vararg windows: TimeWindow): ScheduleEntity {
        return ScheduleEntity(
            name = "Test",
            enabledDays = enabledDays,
            timeWindowsJson = evaluator.timeWindowsToJson(windows.toList())
        )
    }

    private fun makeCalendar(dayOfWeek: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
    }

    @Test
    fun `allowed during scheduled window on enabled day`() {
        val schedule = makeSchedule(
            enabledDays = 0b1111111, // all days
            TimeWindow(9, 0, 17, 0)
        )
        val cal = makeCalendar(Calendar.MONDAY, 12, 0)
        assertTrue(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `blocked outside scheduled window`() {
        val schedule = makeSchedule(
            enabledDays = 0b1111111,
            TimeWindow(9, 0, 17, 0)
        )
        val cal = makeCalendar(Calendar.MONDAY, 20, 0)
        assertFalse(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `blocked on disabled day`() {
        val schedule = makeSchedule(
            enabledDays = 0b0011111, // Mon-Fri only (bits 1-16)
            TimeWindow(9, 0, 17, 0)
        )
        // Saturday = bit 32
        val cal = makeCalendar(Calendar.SATURDAY, 12, 0)
        assertFalse(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `allowed on enabled weekend day`() {
        val schedule = makeSchedule(
            enabledDays = 0b1100000, // Sa=32 + So=64 = 96
            TimeWindow(10, 0, 20, 0)
        )
        val cal = makeCalendar(Calendar.SATURDAY, 15, 0)
        assertTrue(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `no windows means always allowed on that day`() {
        val schedule = ScheduleEntity(name = "Open", enabledDays = 0b1111111, timeWindowsJson = "[]")
        val cal = makeCalendar(Calendar.MONDAY, 3, 0)
        assertTrue(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `multiple windows allowed in second window`() {
        val schedule = makeSchedule(
            enabledDays = 0b1111111,
            TimeWindow(6, 0, 9, 0),
            TimeWindow(15, 0, 21, 0)
        )
        val cal = makeCalendar(Calendar.TUESDAY, 17, 30)
        assertTrue(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `multiple windows blocked between windows`() {
        val schedule = makeSchedule(
            enabledDays = 0b1111111,
            TimeWindow(6, 0, 9, 0),
            TimeWindow(15, 0, 21, 0)
        )
        val cal = makeCalendar(Calendar.TUESDAY, 11, 0)
        assertFalse(evaluator.isAllowedAt(schedule, cal))
    }

    @Test
    fun `json roundtrip preserves windows`() {
        val windows = listOf(TimeWindow(6, 30, 22, 0))
        val json = evaluator.timeWindowsToJson(windows)
        val parsed = evaluator.parseTimeWindows(json)
        assertEquals(1, parsed.size)
        assertEquals(6, parsed[0].startHour)
        assertEquals(30, parsed[0].startMinute)
        assertEquals(22, parsed[0].endHour)
        assertEquals(0, parsed[0].endMinute)
    }

    @Test
    fun `invalid json returns empty list`() {
        val result = evaluator.parseTimeWindows("not json")
        assertTrue(result.isEmpty())
    }
}
