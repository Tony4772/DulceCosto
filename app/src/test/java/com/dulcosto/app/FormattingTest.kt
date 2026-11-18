package com.dulcosto.app

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {
    @Test
    fun testDoubleClean() {
        assertEquals("0", 0.0.clean())
        assertEquals("5", 5.0.clean())
        assertEquals("5.5", 5.5.clean())
        assertEquals("12.25", 12.25.clean())
    }
}
