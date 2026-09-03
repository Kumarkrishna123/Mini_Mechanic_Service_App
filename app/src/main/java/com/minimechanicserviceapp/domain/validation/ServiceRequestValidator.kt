package com.minimechanicserviceapp.domain.validation

enum class FieldError(val message: String) {
    NAME_BLANK("Please enter your name"),
    NAME_TOO_SHORT("Name must be at least 2 characters"),
    PHONE_BLANK("Please enter your phone number"),
    PHONE_INVALID("Enter a valid 10-digit mobile number"),
    VEHICLE_BLANK("Please enter your vehicle number"),
    VEHICLE_INVALID("Use a format like KA01AB1234"),
    SERVICE_NOT_SELECTED("Please select a service"),
    DESCRIPTION_BLANK("Please describe the problem"),
    DESCRIPTION_TOO_SHORT("Add at least 10 characters so the garage can prepare"),
}

object ServiceRequestValidator {

    const val DESCRIPTION_MIN_LENGTH = 10
    const val DESCRIPTION_MAX_LENGTH = 500

    private val VEHICLE_PATTERN = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{1,4}$")

    fun validateName(value: String): FieldError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> FieldError.NAME_BLANK
            trimmed.length < 2 -> FieldError.NAME_TOO_SHORT
            else -> null
        }
    }

    fun validatePhone(value: String): FieldError? {
        val digits = normalisePhone(value)
        return when {
            digits.isEmpty() -> FieldError.PHONE_BLANK
            digits.length != 10 -> FieldError.PHONE_INVALID
            digits.first() !in '6'..'9' -> FieldError.PHONE_INVALID
            else -> null
        }
    }

    fun validateVehicle(value: String): FieldError? {
        val normalised = normaliseVehicle(value)
        return when {
            normalised.isEmpty() -> FieldError.VEHICLE_BLANK
            !VEHICLE_PATTERN.matches(normalised) -> FieldError.VEHICLE_INVALID
            else -> null
        }
    }

    fun validateService(value: String): FieldError? =
        if (value.isBlank()) FieldError.SERVICE_NOT_SELECTED else null

    fun validateDescription(value: String): FieldError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> FieldError.DESCRIPTION_BLANK
            trimmed.length < DESCRIPTION_MIN_LENGTH -> FieldError.DESCRIPTION_TOO_SHORT
            else -> null
        }
    }

    fun normalisePhone(value: String): String {
        val digits = value.filter { it.isDigit() }
        return when {
            digits.length == 12 && digits.startsWith("91") -> digits.drop(2)
            digits.length == 11 && digits.startsWith("0") -> digits.drop(1)
            else -> digits
        }
    }

    fun normaliseVehicle(value: String): String =
        value.filter { it.isLetterOrDigit() }.uppercase()
}
