package com.minimechanicserviceapp

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private interface Sample {
    fun value(): Int
}

class ToolchainSmokeTest {

    @Test
    fun `mockk stubs an interface`() {
        val sample = mockk<Sample>()
        every { sample.value() } returns 42
        assertEquals(42, sample.value())
    }

    @Test
    fun `turbine collects a flow inside runTest`() = runTest {
        flowOf(1, 2, 3).test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }
}
