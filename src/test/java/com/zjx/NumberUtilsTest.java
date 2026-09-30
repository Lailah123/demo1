package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link NumberUtils} 的单元测试。
 */
class NumberUtilsTest {

    @Test
    void fibonacciOfFirstTerms() {
        assertEquals(0L, NumberUtils.fibonacci(0));
        assertEquals(1L, NumberUtils.fibonacci(1));
        assertEquals(1L, NumberUtils.fibonacci(2));
        assertEquals(2L, NumberUtils.fibonacci(3));
        assertEquals(3L, NumberUtils.fibonacci(4));
        assertEquals(5L, NumberUtils.fibonacci(5));
        assertEquals(55L, NumberUtils.fibonacci(10));
    }

    @Test
    void fibonacciOfLargeIndexIsExact() {
        assertEquals(7540113804746346429L, NumberUtils.fibonacci(92));
    }

    @Test
    void fibonacciOfNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> NumberUtils.fibonacci(-1));
    }

    @Test
    void fibonacciOverflowThrows() {
        assertThrows(ArithmeticException.class, () -> NumberUtils.fibonacci(93));
    }

    @Test
    void isPrimeDetectsPrimes() {
        assertTrue(NumberUtils.isPrime(2));
        assertTrue(NumberUtils.isPrime(3));
        assertTrue(NumberUtils.isPrime(17));
        assertTrue(NumberUtils.isPrime(97));
    }

    @Test
    void isPrimeRejectsNonPrimes() {
        assertFalse(NumberUtils.isPrime(0));
        assertFalse(NumberUtils.isPrime(1));
        assertFalse(NumberUtils.isPrime(4));
        assertFalse(NumberUtils.isPrime(100));
    }

    @Test
    void gcdComputesGreatestCommonDivisor() {
        assertEquals(6, NumberUtils.gcd(54, 24));
        assertEquals(1, NumberUtils.gcd(17, 13));
        assertEquals(0, NumberUtils.gcd(0, 0));
        assertEquals(5, NumberUtils.gcd(-15, 5));
    }
}
