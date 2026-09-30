package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.zjx.Greeter.Language;

/**
 * {@link Greeter} 的单元测试。
 */
class GreeterTest {

    @Test
    void greetChineseWithName() {
        assertEquals("你好，张三！", Greeter.greet("张三", Language.CHINESE));
    }

    @Test
    void greetEnglishWithName() {
        assertEquals("Hello, Alice!", Greeter.greet("Alice", Language.ENGLISH));
    }

    @Test
    void greetJapaneseWithName() {
        assertEquals("こんにちは、太郎！", Greeter.greet("太郎", Language.JAPANESE));
    }

    @Test
    void greetTrimsName() {
        assertEquals("Hello, Bob!", Greeter.greet("  Bob  ", Language.ENGLISH));
    }

    @Test
    void greetWithoutNameReturnsGenericGreeting() {
        assertEquals("你好！", Greeter.greet(null, Language.CHINESE));
        assertEquals("你好！", Greeter.greet("   ", Language.CHINESE));
        assertEquals("你好！", Greeter.greet(Language.CHINESE));
    }

    @Test
    void greetRejectsNullLanguage() {
        assertThrows(IllegalArgumentException.class, () -> Greeter.greet("张三", null));
    }
}
