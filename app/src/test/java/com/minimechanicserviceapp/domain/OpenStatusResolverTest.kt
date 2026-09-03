package com.minimechanicserviceapp.domain

import com.minimechanicserviceapp.domain.model.OpenStatus
import com.minimechanicserviceapp.domain.model.WorkingHours
import com.minimechanicserviceapp.domain.utill.OpenStatusResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class OpenStatusResolverTest {

    private fun hours(day: DayOfWeek, open: String, close: String) =
        WorkingHours(day, LocalTime.parse(open), LocalTime.parse(close))

    /** 2026-09-07 is a Monday, so these times are unambiguous. */
    private fun monday(time: String): LocalDateTime =
        LocalDateTime.parse("2026-09-07T$time")

    private val mondayNineToSeven = listOf(hours(DayOfWeek.MONDAY, "09:00", "19:00"))

    @Test
    fun `open during business hours`() {
        val status = OpenStatusResolver.resolve(mondayNineToSeven, monday("10:30"))
        assertTrue(status is OpenStatus.Open)
        assertEquals(LocalTime.parse("19:00"), (status as OpenStatus.Open).closesAt)
        assertTrue(status.isOpen)
    }

    @Test
    fun `opening time is inclusive`() {
        assertTrue(OpenStatusResolver.resolve(mondayNineToSeven, monday("09:00")).isOpen)
    }

    @Test
    fun `closing time is exclusive`() {
        val status = OpenStatusResolver.resolve(mondayNineToSeven, monday("19:00"))
        assertTrue(status is OpenStatus.Closed)
    }

    @Test
    fun `before opening reports today's opening time`() {
        val status = OpenStatusResolver.resolve(mondayNineToSeven, monday("07:15"))
        status as OpenStatus.Closed
        assertEquals(LocalTime.parse("09:00"), status.opensAt)
        assertEquals(DayOfWeek.MONDAY, status.opensOn)
    }

    @Test
    fun `after closing rolls forward to the next open day`() {
        val week = listOf(
            hours(DayOfWeek.MONDAY, "09:00", "19:00"),
            hours(DayOfWeek.WEDNESDAY, "10:00", "18:00"),
        )
        val status = OpenStatusResolver.resolve(week, monday("21:00"))
        status as OpenStatus.Closed
        assertEquals(DayOfWeek.WEDNESDAY, status.opensOn)
        assertEquals(LocalTime.parse("10:00"), status.opensAt)
    }

    @Test
    fun `a day the garage does not work skips to the next open day`() {
        val sundayOnly = listOf(hours(DayOfWeek.SUNDAY, "11:00", "15:00"))
        val status = OpenStatusResolver.resolve(sundayOnly, monday("12:00"))
        status as OpenStatus.Closed
        assertEquals(DayOfWeek.SUNDAY, status.opensOn)
    }

    @Test
    fun `single open day wraps around the week back to itself`() {
        val status = OpenStatusResolver.resolve(mondayNineToSeven, monday("23:00"))
        status as OpenStatus.Closed
        assertEquals(DayOfWeek.MONDAY, status.opensOn)
    }

    @Test
    fun `no working hours is closed with nothing to report`() {
        val status = OpenStatusResolver.resolve(emptyList(), monday("12:00"))
        status as OpenStatus.Closed
        assertNull(status.opensAt)
        assertNull(status.opensOn)
    }
}
