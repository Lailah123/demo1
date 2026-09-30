package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link StringUtils} 的单元测试。
 */
class StringUtilsTest {

    @Test
    void reverseReversesString() {
        assertEquals("cba", StringUtils.reverse("abc"));
        assertEquals("上海", StringUtils.reverse("海上"));
    }

    @Test
    void reverseRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.reverse(null));
    }

    @Test
    void palindromeIgnoresCaseAndPunctuation() {
        assertTrue(StringUtils.isPalindrome("A man, a plan, a canal: Panama"));
    }

    @Test
    void palindromeDetectsChinesePalindrome() {
        assertTrue(StringUtils.isPalindrome("上海自来水来自海上"));
    }

    @Test
    void palindromeReturnsFalseForNonPalindrome() {
        assertFalse(StringUtils.isPalindrome("hello"));
    }

    @Test
    void palindromeRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.isPalindrome(null));
    }

    @Test
    void wordCountCountsWhitespaceSeparatedWords() {
        assertEquals(4, StringUtils.wordCount("the quick brown fox"));
        assertEquals(1, StringUtils.wordCount("hello"));
    }

    @Test
    void wordCountOfBlankOrNullIsZero() {
        assertEquals(0, StringUtils.wordCount(null));
        assertEquals(0, StringUtils.wordCount("   "));
    }

    @Test
    void titleCaseCapitalizesEachWord() {
        assertEquals("Hello World", StringUtils.titleCase("hello world"));
        assertEquals("The Quick Brown Fox", StringUtils.titleCase("tHe quICk bRoWn fox"));
    }

    @Test
    void titleCaseOfBlankOrNullIsEmpty() {
        assertEquals("", StringUtils.titleCase(null));
        assertEquals("", StringUtils.titleCase("  "));
    }
}
