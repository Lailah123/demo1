package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * {@link Calculator} 的单元测试。
 */
class CalculatorTest {

    @Test
    void addReturnsSum() {
        assertEquals(5.0, Calculator.add(2.0, 3.0), 1e-9);
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(-1.0, Calculator.subtract(2.0, 3.0), 1e-9);
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(6.0, Calculator.multiply(2.0, 3.0), 1e-9);
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.5, Calculator.divide(5.0, 2.0), 1e-9);
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> Calculator.divide(1.0, 0.0));
    }

    @Test
    void factorialOfZeroIsOne() {
        assertEquals(1L, Calculator.factorial(0));
    }

    @Test
    void factorialOfPositiveNumber() {
        assertEquals(120L, Calculator.factorial(5));
    }

    @Test
    void factorialOfTwentyIsExact() {
        assertEquals(2432902008176640000L, Calculator.factorial(20));
    }

    @Test
    void factorialOfNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.factorial(-1));
    }

    @Test
    void factorialOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> Calculator.factorial(21));
    }

    @Test
    void powerWithPositiveExponent() {
        assertEquals(1024.0, Calculator.power(2.0, 10), 1e-9);
    }

    @Test
    void powerWithZeroExponent() {
        assertEquals(1.0, Calculator.power(123.0, 0), 1e-9);
    }

    @Test
    void powerWithNegativeExponent() {
        assertEquals(0.25, Calculator.power(2.0, -2), 1e-9);
    }

    @Test
    void powerOfZeroWithNegativeExponentThrows() {
        assertThrows(ArithmeticException.class, () -> Calculator.power(0.0, -1));
    }
}
