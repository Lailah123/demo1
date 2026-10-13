package com.zjx;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * {@link TextTable} 的单元测试。
 */
class TextTableTest {

    @Test
    void displayWidthCountsFullWidthCharactersAsTwoColumns() {
        assertEquals(0, TextTable.displayWidth(null));
        assertEquals(0, TextTable.displayWidth(""));
        assertEquals(3, TextTable.displayWidth("abc"));
        assertEquals(4, TextTable.displayWidth("中文"));
        assertEquals(6, TextTable.displayWidth("中a文b"));
        assertEquals(4, TextTable.displayWidth("アイ"));       // 片假名
        assertEquals(4, TextTable.displayWidth("한글"));       // 谚文
        assertEquals(4, TextTable.displayWidth("ＡＢ"));       // 全角字母
        assertEquals(2, TextTable.displayWidth("　"));     // 全角空格
        assertEquals(4, TextTable.displayWidth("，。"));       // 全角标点
    }

    @Test
    void displayWidthTreatsNarrowCharactersAsOneColumn() {
        assertEquals(1, TextTable.displayWidth("·"));     // 中点
        assertEquals(1, TextTable.displayWidth("→"));     // 箭头
        // U+303F 汉字半填充空格按 UAX #11 属于窄字符，位于全角区段的边界之外
        assertEquals(1, TextTable.displayWidth("〿"));
    }

    @Test
    void displayWidthHandlesSurrogatePairsAsOneCodePoint() {
        assertEquals(2, TextTable.displayWidth("𠀀"));      // CJK 扩展 B
        assertEquals(3, TextTable.displayWidth("a𠀀"));
    }

    @Test
    void renderWithoutHeadersProducesBoxedRows() {
        List<String[]> rows = rows(new String[] {"a", "bb"}, new String[] {"ccc", "d"});

        String expected = String.join("\n",
                "+-----+----+",
                "| a   | bb |",
                "| ccc | d  |",
                "+-----+----+");

        assertEquals(expected, TextTable.render(rows));
    }

    @Test
    void renderAlignsMixedChineseAndAsciiColumns() {
        List<String[]> rows = rows(new String[] {"张三", "北京"}, new String[] {"Li Lei", "Shanghai"});

        String expected = String.join("\n",
                "+--------+----------+",
                "| 姓名   | 城市     |",
                "+--------+----------+",
                "| 张三   | 北京     |",
                "| Li Lei | Shanghai |",
                "+--------+----------+");

        assertEquals(expected, TextTable.render(new String[] {"姓名", "城市"}, rows));
    }

    @Test
    void rightAlignPadsOnTheLeft() {
        List<String[]> rows = rows(new String[] {"1"}, new String[] {"22"});

        String expected = String.join("\n",
                "+----+",
                "| 值 |",
                "+----+",
                "|  1 |",
                "| 22 |",
                "+----+");

        assertEquals(expected, TextTable.render(new String[] {"值"}, rows, TextTable.Align.RIGHT));
    }

    @Test
    void centerAlignPutsExtraSpaceOnTheRight() {
        List<String[]> rows = rows(new String[] {"a"}, new String[] {"ab"});

        String expected = String.join("\n",
                "+----+",
                "| a  |",
                "| ab |",
                "+----+");

        assertEquals(expected, TextTable.render(null, rows, TextTable.Align.CENTER));
    }

    @Test
    void missingAlignFallsBackToLeft() {
        List<String[]> rows = rows(new String[] {"aa", "bb"}, new String[] {"c", "d"});

        String expected = String.join("\n",
                "+----+----+",
                "| aa | bb |",
                "|  c | d  |",
                "+----+----+");

        assertEquals(expected, TextTable.render(null, rows, TextTable.Align.RIGHT));
    }

    @Test
    void raggedRowsArePaddedWithEmptyCells() {
        List<String[]> rows = rows(new String[] {"a"}, new String[] {"b", "c"});

        String expected = String.join("\n",
                "+---+---+",
                "| a |   |",
                "| b | c |",
                "+---+---+");

        assertEquals(expected, TextTable.render(rows));
    }

    @Test
    void nullCellsRenderAsEmpty() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {null, "x"});

        String expected = String.join("\n",
                "+--+---+",
                "|  | x |",
                "+--+---+");

        assertEquals(expected, TextTable.render(rows));
    }

    @Test
    void nullRowRendersAsEmptyRow() {
        List<String[]> rows = Arrays.asList(null, new String[] {"x"});

        String expected = String.join("\n",
                "+---+",
                "|   |",
                "| x |",
                "+---+");

        assertEquals(expected, TextTable.render(rows));
    }

    @Test
    void lineBreaksAndTabsInCellsAreReplacedWithSpaces() {
        List<String[]> rows = rows(new String[] {"a\nb"}, new String[] {"c\td"});

        String expected = String.join("\n",
                "+-----+",
                "| a b |",
                "| c d |",
                "+-----+");

        assertEquals(expected, TextTable.render(rows));
    }

    @Test
    void renderDoesNotModifyInput() {
        String[] headers = {"姓名", "城市"};
        String[] row = {"张三", "北京"};
        List<String[]> rows = rows(row);

        TextTable.render(headers, rows);

        assertArrayEquals(new String[] {"姓名", "城市"}, headers);
        assertArrayEquals(new String[] {"张三", "北京"}, row);
    }

    @Test
    void emptyInputRendersEmptyString() {
        assertEquals("", TextTable.render(new ArrayList<String[]>()));
        assertEquals("", TextTable.render(null, new ArrayList<String[]>()));
        assertEquals("", TextTable.render(new String[0], new ArrayList<String[]>()));
        assertEquals("", TextTable.render(Collections.<String[]>emptyList()));
    }

    @Test
    void headersOnlyRendersSingleBoxWithoutDuplicateSeparator() {
        String expected = String.join("\n",
                "+------+",
                "| 姓名 |",
                "+------+");

        assertEquals(expected, TextTable.render(new String[] {"姓名"}, new ArrayList<String[]>()));
    }

    @Test
    void renderRejectsNullRows() {
        assertThrows(IllegalArgumentException.class, () -> TextTable.render((List<String[]>) null));
        assertThrows(IllegalArgumentException.class, () -> TextTable.render(new String[] {"h"}, null));
        assertThrows(IllegalArgumentException.class,
                () -> TextTable.render(new String[] {"h"}, null, TextTable.Align.RIGHT));
    }

    @Test
    void renderHasNoTrailingNewline() {
        String table = TextTable.render(rows(new String[] {"a"}));

        assertFalse(table.endsWith("\n"), "渲染结果不应以换行结尾");
    }

    @Test
    void everyRenderedLineHasTheSameDisplayWidth() {
        List<String[]> rows = rows(
                new String[] {"张三", "北京", "28"},
                new String[] {"Li Lei", "Shanghai", "9"},
                new String[] {"王", "广州", ""});

        String table = TextTable.render(
                new String[] {"姓名", "城市", "年龄"},
                rows,
                TextTable.Align.LEFT, TextTable.Align.CENTER, TextTable.Align.RIGHT);

        String[] lines = table.split("\n", -1);
        assertEquals(7, lines.length, "3 行数据应渲染为 顶边+表头+分隔+3 数据行+底边");
        int expectedWidth = TextTable.displayWidth(lines[0]);
        assertTrue(expectedWidth > 0);
        for (String line : lines) {
            assertEquals(expectedWidth, TextTable.displayWidth(line), "行显示宽度不一致: " + line);
        }
    }

    private static List<String[]> rows(String[]... rows) {
        return new ArrayList<>(Arrays.asList(rows));
    }
}
