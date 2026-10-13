package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * {@link LetterArt} 的单元测试。
 *
 * <p>字模数据内置于实现中，测试直接断言渲染结果，保证测试封闭、可复现。</p>
 */
class LetterArtTest {

    @Test
    void rendersSingleLetterExactly() {
        assertEquals(" ### \n#   #\n#####\n#   #\n#   #\n", LetterArt.render("A"),
                "字母 A 应渲染为固定的 5 行点阵");
    }

    @Test
    void rendersTwoLettersExactly() {
        assertEquals("#   # #####\n"
                + "#   #   #  \n"
                + "#####   #  \n"
                + "#   #   #  \n"
                + "#   # #####\n",
                LetterArt.render("HI"),
                "相邻字符之间应插入默认的一个空格间距");
    }

    @Test
    void lowercaseIsNormalizedToUppercase() {
        assertEquals(LetterArt.render("HELLO"), LetterArt.render("hello"),
                "小写字母应自动转为大写");
        assertEquals(LetterArt.render("HI 123!"), LetterArt.render("hi 123!"),
                "混合大小写与标点应同样归一化");
    }

    @Test
    void lineHeightIsAlwaysFontHeight() {
        assertEquals(5, LetterArt.FONT_HEIGHT, "字模高度应为 5 行");
        for (String text : new String[] {"A", "hello world", "0123456789", "a.b,c!"}) {
            assertEquals(LetterArt.FONT_HEIGHT, LetterArt.renderLines(text).size(),
                    "文本 " + text + " 应输出固定 5 行");
        }
    }

    @Test
    void allRowsOfOneRenderHaveEqualWidth() {
        List<String> lines = LetterArt.renderLines("Hello, World?! (v2)");
        int width = lines.get(0).length();
        for (String line : lines) {
            assertEquals(width, line.length(), "同一艺术字的各行宽度应一致");
        }
        assertTrue(width > 0, "渲染结果宽度应为正数");
    }

    @Test
    void letterSpacingControlsGapWidth() {
        int glyphWidth = LetterArt.renderLines("A", 0).get(0).length();
        assertEquals(2 * glyphWidth, LetterArt.renderLines("AA", 0).get(0).length(),
                "间距为 0 时两字母应紧邻");
        assertEquals(2 * glyphWidth + 3, LetterArt.renderLines("AA", 3).get(0).length(),
                "间距为 3 时两字母之间应有 3 个空格");
    }

    @Test
    void spaceGlyphSeparatesWords() {
        int letterWidth = LetterArt.renderLines("A", 0).get(0).length();
        int spaceWidth = LetterArt.renderLines(" ", 0).get(0).length();
        assertEquals(2 * letterWidth + spaceWidth, LetterArt.renderLines("A A", 0).get(0).length(),
                "空格字符应占据自身字模宽度");
        assertTrue(spaceWidth > 0, "空格字模宽度应为正数");
    }

    @Test
    void everySupportedCharacterRendersConsistently() {
        Set<Character> supported = LetterArt.supportedCharacters();
        assertFalse(supported.isEmpty(), "受支持字符集合不应为空");

        StringBuilder text = new StringBuilder();
        int expectedWidth = 0;
        for (Character ch : supported) {
            text.append(ch);
            expectedWidth += LetterArt.renderLines(String.valueOf(ch), 0).get(0).length();
        }
        int spacing = 2;
        int withGaps = expectedWidth + spacing * (supported.size() - 1);

        List<String> lines = LetterArt.renderLines(text.toString(), spacing);
        assertEquals(LetterArt.FONT_HEIGHT, lines.size(), "全部字符应渲染为 5 行");
        for (String line : lines) {
            assertEquals(withGaps, line.length(), "全部字符渲染后各行宽度应等于字模宽度之和加间距");
        }
    }

    @Test
    void supportedCharactersCoverLettersDigitsSpaceAndPunctuation() {
        Set<Character> supported = LetterArt.supportedCharacters();
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            assertTrue(supported.contains(ch), "应支持大写字母: " + ch);
        }
        for (char ch = '0'; ch <= '9'; ch++) {
            assertTrue(supported.contains(ch), "应支持数字: " + ch);
        }
        assertTrue(supported.contains(' '), "应支持空格");
        for (char ch : new char[] {'.', ',', '!', '?', '-', '_', '+', '=', ':', '(', ')', '[', ']', '/'}) {
            assertTrue(supported.contains(ch), "应支持标点: " + ch);
        }
        assertThrows(UnsupportedOperationException.class, () -> supported.add('#'),
                "受支持字符集合应为不可变集合");
    }

    @Test
    void renderAppendsTrailingNewlinePerLine() {
        String art = LetterArt.render("OK");
        assertTrue(art.endsWith("\n"), "渲染结果应以换行结尾");
        List<String> lines = LetterArt.renderLines("OK");
        StringBuilder expected = new StringBuilder();
        for (String line : lines) {
            expected.append(line).append('\n');
        }
        assertEquals(expected.toString(), art, "render 应是各行拼接并逐行换行");
    }

    @Test
    void renderLinesDoesNotContainNewlines() {
        for (String line : LetterArt.renderLines("A B")) {
            assertFalse(line.contains("\n"), "renderLines 的每个元素不应包含换行符");
        }
    }

    @Test
    void renderLinesReturnsUnmodifiableList() {
        List<String> lines = LetterArt.renderLines("A");
        assertThrows(UnsupportedOperationException.class, () -> lines.add("x"),
                "返回的列表应为不可变列表");
    }

    @Test
    void spacingBoundariesAreAccepted() {
        List<String> tight = LetterArt.renderLines("AA", 0);
        List<String> wide = LetterArt.renderLines("AA", 20);
        assertEquals(LetterArt.FONT_HEIGHT, tight.size(), "间距下界 0 应被接受");
        assertEquals(LetterArt.FONT_HEIGHT, wide.size(), "间距上界 20 应被接受");
        assertEquals(tight.get(0).length() + 20, wide.get(0).length(),
                "间距从 0 增至 20 时应恰好加宽 20 个字符");
    }

    @Test
    void rejectsNullText() {
        assertThrows(IllegalArgumentException.class, () -> LetterArt.render(null));
        assertThrows(IllegalArgumentException.class, () -> LetterArt.renderLines(null));
    }

    @Test
    void rejectsEmptyText() {
        assertThrows(IllegalArgumentException.class, () -> LetterArt.render(""));
        assertThrows(IllegalArgumentException.class, () -> LetterArt.renderLines(""));
    }

    @Test
    void rejectsInvalidSpacing() {
        assertThrows(IllegalArgumentException.class, () -> LetterArt.render("A", -1));
        assertThrows(IllegalArgumentException.class, () -> LetterArt.render("A", 21));
        assertThrows(IllegalArgumentException.class, () -> LetterArt.renderLines("A", -1));
        assertThrows(IllegalArgumentException.class, () -> LetterArt.renderLines("A", 21));
    }

    @Test
    void rejectsUnsupportedCharacterWithPosition() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LetterArt.render("A中B"));
        String message = error.getMessage();
        assertTrue(message.contains("不支持的字符"), "错误信息应说明字符不受支持");
        assertTrue(message.contains("中"), "错误信息应包含具体字符");
        assertTrue(message.contains("位置 2"), "错误信息应包含字符位置，实际为: " + message);
    }

    @Test
    void rejectsControlCharactersWithReadableName() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LetterArt.render("A\nB"));
        assertTrue(error.getMessage().contains("'\\n'"),
                "换行符应以可读形式 \\n 出现在错误信息中，实际为: " + error.getMessage());
    }
}
