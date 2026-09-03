package com.minimechanicserviceapp.domain

import com.minimechanicserviceapp.domain.utill.DistanceCalculator.distanceKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    private val bengaluruLat = 12.9716
    private val bengaluruLng = 77.5946

    @Test
    fun `the same point is zero distance`() {
        val d = distanceKm(bengaluruLat, bengaluruLng, bengaluruLat, bengaluruLng)
        assertEquals(0.0, d, 0.0001)
    }

    @Test
    fun `a nearby locality is a few kilometres away`() {
        // Indiranagar, roughly 5 km east of the city centre
        val d = distanceKm(bengaluruLat, bengaluruLng, 12.9719, 77.6412)
        assertTrue("expected roughly 5 km but was $d", d in 4.8..5.3)
    }

    @Test
    fun `a long haul matches the known great-circle distance`() {
        // Bengaluru to Delhi is about 1740 km
        val d = distanceKm(bengaluruLat, bengaluruLng, 28.6139, 77.2090)
        assertTrue("expected roughly 1740 km but was $d", d in 1700.0..1780.0)
    }

    @Test
    fun `distance is symmetric`() {
        val there = distanceKm(bengaluruLat, bengaluruLng, 13.1007, 77.5963)
        val back = distanceKm(13.1007, 77.5963, bengaluruLat, bengaluruLng)
        assertEquals(there, back, 0.0001)
    }

    @Test
    fun `crossing the equator does not break the formula`() {
        val d = distanceKm(-1.0, 36.0, 1.0, 36.0)
        assertTrue("expected roughly 222 km but was $d", d in 220.0..224.0)
    }
}
