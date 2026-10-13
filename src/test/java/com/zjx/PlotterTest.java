package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/**
 * {@link Plotter} 的单元测试。
 */
class PlotterTest {

    @Test
    void rendersRequestedCanvasSize() {
        String plot = Plotter.plot("sin(x)", -6.5, 6.5, 40, 12);
        List<String> lines = lines(plot);

        assertEquals(12, lines.size());
        for (String line : lines) {
            assertEquals(40, line.length(), line);
        }
    }

    @Test
    void resultHasNoTrailingNewline() {
        String plot = Plotter.plot("x", -1, 1, -1, 1, 11, 11);

        assertFalse(plot.endsWith("\n"), plot);
        assertEquals(11, lines(plot).size());
    }

    @Test
    void drawsStraightLineWithAxesAndOrigin() {
        String expected = String.join("\n",
                "     |    /",
                "     |   / ",
                "     |  /  ",
                "     | /   ",
                "     |/    ",
                "-----/-----",
                "    /|     ",
                "   / |     ",
                "  /  |     ",
                " /   |     ",
                "*    |     ");

        assertEquals(expected, Plotter.plot("x", -1, 1, -1, 1, 11, 11));
    }

    @Test
    void curveIsNotOverwrittenByAxes() {
        // y = x 正好穿过原点，该位置应保留曲线字符而不是原点标记
        assertFalse(Plotter.plot("x", -1, 1, -1, 1, 11, 11).contains("+"));
    }

    @Test
    void usesSlashForRisingAndBackslashForFalling() {
        String rising = Plotter.plot("x", -1, 1, -1, 1, 21, 11);
        String falling = Plotter.plot("-x", -1, 1, -1, 1, 21, 11);

        assertTrue(rising.contains("/"), rising);
        assertFalse(rising.contains("\\"), rising);
        assertTrue(falling.contains("\\"), falling);
        assertFalse(falling.contains("/"), falling);
    }

    @Test
    void drawsFlatCurveWithHyphens() {
        // 常函数自动范围上下各展开 0.3，曲线落在一行上
        List<String> lines = lines(Plotter.plot("3", -1, 1, 21, 11));

        assertEquals(11, lines.size());
        assertEquals("*--------------------", lines.get(5));
        assertEquals("          |          ", lines.get(0));
    }

    @Test
    void autoRangeExpandsConstantZero() {
        List<String> lines = lines(Plotter.plot("0", -1, 1, 21, 11));

        assertEquals("*--------------------", lines.get(5));
        assertEquals("          |          ", lines.get(10));
    }

    @Test
    void autoRangeKeepsBothExtremesInsideCanvas() {
        String plot = Plotter.plot("x^2", -2, 2, 41, 15);
        List<String> lines = lines(plot);

        assertEquals(15, lines.size());
        for (String line : lines) {
            assertEquals(41, line.length(), line);
        }
        // 两端 x = ±2 处是最大值，自动范围必须容纳它们，首尾两列都要画出曲线
        assertTrue(columnHasInk(lines, 0), plot);
        assertTrue(columnHasInk(lines, 40), plot);
    }

    @Test
    void autoRangeRejectsIntervalsWithoutFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> Plotter.plot("sqrt(x)", -4, -1, 20, 10));
        assertThrows(IllegalArgumentException.class, () -> Plotter.plot("ln(x)", -4, -1, 20, 10));
    }

    @Test
    void leavesGapWhereFunctionIsUndefined() {
        // 1 / x 在 x = 0 处为无穷大，该列不画曲线，只剩坐标轴与原点
        List<String> lines = lines(Plotter.plot("1 / x", -3, 3, -3, 3, 21, 11));

        assertEquals('+', lines.get(5).charAt(10));
        assertEquals('|', lines.get(0).charAt(10));
        assertEquals('|', lines.get(10).charAt(10));
    }

    @Test
    void clipsValuesOutsideYRange() {
        // 给定 y 范围 [-1, 1] 后，x * 100 只有 x = 0 一列可见
        String expected = String.join("\n",
                "     |     ",
                "     |     ",
                "     |     ",
                "     |     ",
                "     |     ",
                "-----*-----",
                "     |     ",
                "     |     ",
                "     |     ",
                "     |     ",
                "     |     ");

        assertEquals(expected, Plotter.plot("x * 100", -1, 1, -1, 1, 11, 11));
    }

    @Test
    void omitsAxesWhenZeroIsOutsideTheRange() {
        String expected = String.join("\n",
                "           ",
                "        /--",
                "   /----   ",
                "*--        ",
                "           ");

        assertEquals(expected, Plotter.plot("x + 10", 1, 3, 10, 14, 11, 5));
    }

    @Test
    void plotsCallerSuppliedFunction() {
        String plot = Plotter.plot(x -> x * x, -2, 2, 0, 4, 21, 11);
        List<String> lines = lines(plot);

        assertEquals(11, lines.size());
        for (String line : lines) {
            assertEquals(21, line.length(), line);
        }
        assertEquals('*', lines.get(0).charAt(0));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot((String) null, -1, 1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot((DoubleUnaryOperator) null, -1, 1, -1, 1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("x", 1, 1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("x", 1, -1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("x", -1, 1, 1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("x", -1, 1, 10, 1));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("x", -1, 1, 1, -1, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> Plotter.plot("nope(y)", -1, 1, 10, 10));
    }

    private static List<String> lines(String plot) {
        return Arrays.asList(plot.split("\n", -1));
    }

    private static boolean columnHasInk(List<String> lines, int column) {
        for (String line : lines) {
            if (line.charAt(column) != ' ') {
                return true;
            }
        }
        return false;
    }
}
