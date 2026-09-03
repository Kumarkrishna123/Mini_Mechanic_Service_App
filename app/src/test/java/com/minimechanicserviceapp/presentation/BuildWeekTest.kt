package com.minimechanicserviceapp.presentation

import com.minimechanicserviceapp.domain.model.WorkingHours
import com.minimechanicserviceapp.presentation.details.buildWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class BuildWeekTest {

    private val mondayAndWednesday = listOf(
        WorkingHours(DayOfWeek.MONDAY, LocalTime.parse("09:00"), LocalTime.parse("19:00")),
        WorkingHours(DayOfWeek.WEDNESDAY, LocalTime.parse("10:00"), LocalTime.parse("18:00")),
    )

    @Test
    fun `always returns all seven days in calendar order`() {
        val week = buildWeek(mondayAndWednesday, DayOfWeek.MONDAY)

        assertEquals(7, week.size)
        assertEquals(DayOfWeek.entries.toList(), week.map { it.day })
    }

    @Test
    fun `days with hours carry their times`() {
        val monday = buildWeek(mondayAndWednesday, DayOfWeek.FRIDAY)
            .first { it.day == DayOfWeek.MONDAY }

        assertEquals(LocalTime.parse("09:00"), monday.opensAt)
        assertEquals(LocalTime.parse("19:00"), monday.closesAt)
        assertFalse(monday.isClosedAllDay)
    }

    @Test
    fun `days without hours are marked closed all day`() {
        val sunday = buildWeek(mondayAndWednesday, DayOfWeek.MONDAY)
            .first { it.day == DayOfWeek.SUNDAY }

        assertNull(sunday.opensAt)
        assertNull(sunday.closesAt)
        assertTrue(sunday.isClosedAllDay)
    }

    @Test
    fun `exactly one day is flagged as today`() {
        val week = buildWeek(mondayAndWednesday, DayOfWeek.THURSDAY)

        assertEquals(1, week.count { it.isToday })
        assertEquals(DayOfWeek.THURSDAY, week.first { it.isToday }.day)
    }

    @Test
    fun `a day can be today and still be closed`() {
        val week = buildWeek(mondayAndWednesday, DayOfWeek.SUNDAY)
        val sunday = week.first { it.isToday }

        assertTrue(sunday.isClosedAllDay)
    }

    @Test
    fun `an empty schedule still yields a full closed week`() {
        val week = buildWeek(emptyList(), DayOfWeek.MONDAY)

        assertEquals(7, week.size)
        assertTrue(week.all { it.isClosedAllDay })
    }
}
