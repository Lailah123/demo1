package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * {@link FizzBuzz} 的单元测试。
 */
class FizzBuzzTest {

    @Test
    void ofReturnsFizzForMultiplesOfThree() {
        assertEquals("Fizz", FizzBuzz.of(3));
        assertEquals("Fizz", FizzBuzz.of(6));
        assertEquals("Fizz", FizzBuzz.of(9));
    }

    @Test
    void ofReturnsBuzzForMultiplesOfFive() {
        assertEquals("Buzz", FizzBuzz.of(5));
        assertEquals("Buzz", FizzBuzz.of(10));
    }

    @Test
    void ofReturnsFizzBuzzForMultiplesOfFifteen() {
        assertEquals("FizzBuzz", FizzBuzz.of(15));
        assertEquals("FizzBuzz", FizzBuzz.of(30));
    }

    @Test
    void ofReturnsNumberOtherwise() {
        assertEquals("1", FizzBuzz.of(1));
        assertEquals("2", FizzBuzz.of(2));
        assertEquals("7", FizzBuzz.of(7));
    }

    @Test
    void upToReturnsSequenceOfExpectedLength() {
        List<String> result = FizzBuzz.upTo(15);
        assertEquals(15, result.size());
        assertEquals("1", result.get(0));
        assertEquals("Fizz", result.get(2));
        assertEquals("Buzz", result.get(4));
        assertEquals("FizzBuzz", result.get(14));
    }

    @Test
    void upToRejectsNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> FizzBuzz.upTo(0));
        assertThrows(IllegalArgumentException.class, () -> FizzBuzz.upTo(-3));
    }

    @Test
    void printJoinsSequenceWithComma() {
        assertEquals("1, 2, Fizz, 4, Buzz", FizzBuzz.print(5));
        assertTrue(FizzBuzz.print(15).endsWith("FizzBuzz"));
    }
}
