package dev.markturnip.tests

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalculatorTest {

    private val calculator = Calculator()

    @Test
    fun testAdd() {
        assertEquals(4, calculator.add(2, 2))
        assertEquals(0, calculator.add(-1, 1))
        assertEquals(-3, calculator.add(-1, -2))
    }

    @Test
    fun testSubtract() {
        assertEquals(1, calculator.subtract(3, 2))
        assertEquals(-2, calculator.subtract(0, 2))
    }

    @Test
    fun testMultiply() {
        assertEquals(6, calculator.multiply(2, 3))
        assertEquals(0, calculator.multiply(5, 0))
        assertEquals(-4, calculator.multiply(-2, 2))
    }

    @Test
    fun testDivide() {
        assertEquals(2.0, calculator.divide(4, 2))
        assertEquals(0.5, calculator.divide(1, 2))
    }

    @Test
    fun testDivideByZeroThrows() {
        assertFailsWith<IllegalArgumentException> {
            calculator.divide(1, 0)
        }
    }
}
