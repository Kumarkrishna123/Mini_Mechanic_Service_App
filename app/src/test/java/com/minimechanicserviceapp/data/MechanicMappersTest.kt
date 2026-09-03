package com.minimechanicserviceapp.data

import com.minimechanicserviceapp.data.local.entity.CachedWorkingHours
import com.minimechanicserviceapp.data.local.entity.MechanicEntity
import com.minimechanicserviceapp.data.mapper.toDomain
import com.minimechanicserviceapp.data.mapper.toEntityOrNull
import com.minimechanicserviceapp.data.remote.dto.MechanicDto
import com.minimechanicserviceapp.data.remote.dto.WorkingHoursDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class MechanicMappersTest {

    private fun dto(
        id: String = "1",
        name: String? = "Sharma Auto Works",
        rating: Double? = 4.6,
        latitude: Double? = 12.9719,
        longitude: Double? = 77.6412,
        services: List<String>? = listOf("Brake Repair"),
        workingHours: List<WorkingHoursDto>? = listOf(
            WorkingHoursDto("MONDAY", "09:00", "19:00"),
        ),
    ) = MechanicDto(
        id = id,
        name = name,
        rating = rating,
        reviewCount = 128,
        address = "12 MG Road",
        locality = "Indiranagar",
        latitude = latitude,
        longitude = longitude,
        phoneNumber = "+919876543210",
        services = services,
        workingHours = workingHours,
    )

    @Test
    fun `a complete record maps through to the entity`() {
        val entity = dto().toEntityOrNull()
        assertNotNull(entity)
        entity!!
        assertEquals("1", entity.id)
        assertEquals("Sharma Auto Works", entity.name)
        assertEquals(4.6, entity.rating, 0.0001)
        assertEquals(listOf("Brake Repair"), entity.services)
        assertEquals(1, entity.workingHours.size)
    }

    @Test
    fun `a record without a name is dropped`() {
        assertNull(dto(name = null).toEntityOrNull())
        assertNull(dto(name = "   ").toEntityOrNull())
    }

    @Test
    fun `a record without coordinates is dropped because distance is underivable`() {
        assertNull(dto(latitude = null).toEntityOrNull())
        assertNull(dto(longitude = null).toEntityOrNull())
    }

    @Test
    fun `an out-of-range rating is clamped rather than trusted`() {
        assertEquals(5.0, dto(rating = 7.3).toEntityOrNull()!!.rating, 0.0001)
        assertEquals(0.0, dto(rating = -2.0).toEntityOrNull()!!.rating, 0.0001)
        assertEquals(0.0, dto(rating = null).toEntityOrNull()!!.rating, 0.0001)
    }

    @Test
    fun `missing lists become empty rather than null`() {
        val entity = dto(services = null, workingHours = null).toEntityOrNull()!!
        assertTrue(entity.services.isEmpty())
        assertTrue(entity.workingHours.isEmpty())
    }

    @Test
    fun `blank service names are filtered out`() {
        val entity = dto(services = listOf("Oil Change", "", "  ")).toEntityOrNull()!!
        assertEquals(listOf("Oil Change"), entity.services)
    }

    @Test
    fun `one incomplete working-hours row is dropped, the rest survive`() {
        val entity = dto(
            workingHours = listOf(
                WorkingHoursDto("MONDAY", "09:00", "19:00"),
                WorkingHoursDto("TUESDAY", null, "19:00"),
                WorkingHoursDto(null, "09:00", "19:00"),
            ),
        ).toEntityOrNull()!!
        assertEquals(1, entity.workingHours.size)
        assertEquals("MONDAY", entity.workingHours.first().day)
    }

    @Test
    fun `entity to domain parses day and time strings`() {
        val domain = entity(
            hours = listOf(CachedWorkingHours("monday", "09:00", "19:00")),
        ).toDomain()
        assertEquals(1, domain.workingHours.size)
        assertEquals(DayOfWeek.MONDAY, domain.workingHours.first().day)
        assertEquals(LocalTime.parse("09:00"), domain.workingHours.first().opensAt)
    }

    @Test
    fun `unparseable cached hours are dropped instead of throwing`() {
        val domain = entity(
            hours = listOf(
                CachedWorkingHours("MONDAY", "09:00", "19:00"),
                CachedWorkingHours("NOTADAY", "09:00", "19:00"),
                CachedWorkingHours("TUESDAY", "half past nine", "19:00"),
            ),
        ).toDomain()
        assertEquals(1, domain.workingHours.size)
    }

    @Test
    fun `a full round trip preserves the identifying fields`() {
        val domain = dto().toEntityOrNull()!!.toDomain()
        assertEquals("1", domain.id)
        assertEquals("Sharma Auto Works", domain.name)
        assertEquals("+919876543210", domain.phoneNumber)
    }

    private fun entity(hours: List<CachedWorkingHours>) = MechanicEntity(
        id = "1",
        name = "Sharma Auto Works",
        rating = 4.6,
        reviewCount = 128,
        address = "12 MG Road",
        locality = "Indiranagar",
        latitude = 12.9719,
        longitude = 77.6412,
        phoneNumber = "+919876543210",
        services = listOf("Brake Repair"),
        workingHours = hours,
    )
}
