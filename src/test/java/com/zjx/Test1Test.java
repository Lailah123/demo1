package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * {@link Test1} 的单元测试。
 */
class Test1Test {

    @Test
    void messageReturnsHelloWorld() {
        assertEquals("hello world!", Test1.message(), "问候语应精确等于 hello world!");
    }

    @Test
    void messageIsNotNullAndNonBlank() {
        String message = Test1.message();
        assertNotNull(message, "问候语不应为 null");
        assertFalse(message.isBlank(), "问候语不应为空");
    }

    @Test
    void messageDoesNotContainTypo() {
        assertFalse(Test1.message().contains("word!"), "不应包含拼写错误 word!");
    }
}
