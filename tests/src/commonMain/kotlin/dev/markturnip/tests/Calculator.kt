package dev.markturnip.tests

class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun subtract(a: Int, b: Int): Int = a - b
    fun multiply(a: Int, b: Int): Int = a * b
    fun divide(a: Int, b: Int): Double {
        require(b != 0) { "Cannot divide by zero" }
        return a.toDouble() / b.toDouble()
    }
}
