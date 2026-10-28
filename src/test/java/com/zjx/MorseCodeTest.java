package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * {@link MorseCode} 的单元测试。
 */
class MorseCodeTest {

    @Test
    void encodesLettersAndDigits() {
        assertEquals("... --- ...", MorseCode.encode("SOS"));
        assertEquals(".... . .-.. .-.. --- / .-- --- .-. .-.. -..", MorseCode.encode("HELLO WORLD"));
        assertEquals(".---- ..--- ...--", MorseCode.encode("123"));
    }

    @Test
    void encodesLowerCaseAsUpperCase() {
        assertEquals(".... ..", MorseCode.encode("hi"));
    }

    @Test
    void collapsesRepeatedSpacesIntoSingleWordSeparator() {
        assertEquals(".... .. / - .... . .-. .", MorseCode.encode("HI   THERE"));
    }

    @Test
    void encodesEmptyAndBlankTextToEmpty() {
        assertEquals("", MorseCode.encode(""));
        assertEquals("", MorseCode.encode("   "));
    }

    @Test
    void encodeNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> MorseCode.encode(null));
    }

    @Test
    void encodeUnsupportedCharacterThrows() {
        assertThrows(IllegalArgumentException.class, () -> MorseCode.encode("你好"));
    }

    @Test
    void decodesWordsAndLetters() {
        assertEquals("SOS", MorseCode.decode("... --- ..."));
        assertEquals("HELLO WORLD", MorseCode.decode(".... . .-.. .-.. --- / .-- --- .-. .-.. -.."));
    }

    @Test
    void decodeToleratesExtraWhitespace() {
        assertEquals("HI THERE", MorseCode.decode("  ....   ..   /   - .... . .-. . "));
    }

    @Test
    void decodesBlankCodeToEmpty() {
        assertEquals("", MorseCode.decode("   "));
    }

    @Test
    void decodeNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> MorseCode.decode(null));
    }

    @Test
    void decodeUnknownCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> MorseCode.decode("......"));
    }

    @Test
    void roundTripRestoresSupportedText() {
        String text = "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG 0123456789";
        assertEquals(text, MorseCode.decode(MorseCode.encode(text)));
    }

    @Test
    void supportedCharactersCoverLettersDigitsAndPunctuation() {
        Set<Character> supported = MorseCode.supportedCharacters();
        assertTrue(supported.contains('A'));
        assertTrue(supported.contains('Z'));
        assertTrue(supported.contains('0'));
        assertTrue(supported.contains('9'));
        assertTrue(supported.contains('.'));
        assertTrue(supported.contains('?'));
    }
}
