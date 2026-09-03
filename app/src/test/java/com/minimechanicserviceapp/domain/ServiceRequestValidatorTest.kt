package com.minimechanicserviceapp.domain

import com.minimechanicserviceapp.domain.validation.FieldError
import com.minimechanicserviceapp.domain.validation.ServiceRequestValidator as V
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceRequestValidatorTest {

    @Test
    fun `name rejects blank and too short, accepts valid`() {
        assertEquals(FieldError.NAME_BLANK, V.validateName(""))
        assertEquals(FieldError.NAME_BLANK, V.validateName("   "))
        assertEquals(FieldError.NAME_TOO_SHORT, V.validateName("K"))
        assertNull(V.validateName("Krish"))
        assertNull(V.validateName("  Ravi Kumar  "))
    }

    @Test
    fun `phone accepts ten digits starting six to nine`() {
        assertNull(V.validatePhone("9876543210"))
        assertNull(V.validatePhone("6000000000"))
        assertEquals(FieldError.PHONE_BLANK, V.validatePhone(""))
        assertEquals(FieldError.PHONE_INVALID, V.validatePhone("98765"))
        assertEquals(FieldError.PHONE_INVALID, V.validatePhone("98765432100"))
    }

    @Test
    fun `phone rejects landline-style leading digits`() {
        assertEquals(FieldError.PHONE_INVALID, V.validatePhone("1234567890"))
        assertEquals(FieldError.PHONE_INVALID, V.validatePhone("5876543210"))
    }

    @Test
    fun `phone normalisation strips separators and country code`() {
        assertEquals("9876543210", V.normalisePhone("+91 98765 43210"))
        assertEquals("9876543210", V.normalisePhone("098765-43210"))
        assertEquals("9876543210", V.normalisePhone("(987) 654-3210"))
        assertNull(V.validatePhone("+91 98765 43210"))
    }

    @Test
    fun `vehicle accepts real Indian plate formats`() {
        assertNull(V.validateVehicle("KA01AB1234"))
        assertNull(V.validateVehicle("ka 01 ab 1234"))
        assertNull(V.validateVehicle("MH12DE1433"))
        assertNull(V.validateVehicle("DL8CAF5030"))
    }

    @Test
    fun `vehicle rejects blank and malformed`() {
        assertEquals(FieldError.VEHICLE_BLANK, V.validateVehicle(""))
        assertEquals(FieldError.VEHICLE_INVALID, V.validateVehicle("1234"))
        assertEquals(FieldError.VEHICLE_INVALID, V.validateVehicle("KABCDEFGH"))
    }

    @Test
    fun `vehicle normalisation uppercases and removes separators`() {
        assertEquals("KA01AB1234", V.normaliseVehicle("ka-01 ab/1234"))
    }

    @Test
    fun `service must be selected`() {
        assertEquals(FieldError.SERVICE_NOT_SELECTED, V.validateService(""))
        assertNull(V.validateService("Brake Repair"))
    }

    @Test
    fun `description enforces minimum length on trimmed text`() {
        assertEquals(FieldError.DESCRIPTION_BLANK, V.validateDescription("   "))
        assertEquals(FieldError.DESCRIPTION_TOO_SHORT, V.validateDescription("noisy"))
        // exactly at the boundary, and padded so trimming is what decides
        assertEquals(FieldError.DESCRIPTION_TOO_SHORT, V.validateDescription("  short   "))
        assertNull(V.validateDescription("Brakes squeal when stopping"))
    }
}
