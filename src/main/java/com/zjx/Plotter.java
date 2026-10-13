package com.zjx;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * ASCII 函数绘图仪：把一元函数画成终端里的字符曲线。
 *
 * <p>画布是 {@code width × height} 的字符网格，列对应 x 轴（左小右大），行对应 y 轴（上大下小）。
 * 曲线按相邻两列的落点方向选用 {@code -}（水平）、{@code /}（上升）、{@code \}（下降）、
 * {@code |}（跨行）绘制；单独出现的点用 {@code *} 标记。若坐标轴落在画布范围内，
 * 会用 {@code -}、{@code |} 画出 x 轴与 y 轴，交点处标 {@code +}；坐标轴只填充空白单元格，
 * 因此曲线穿过坐标轴时不会被覆盖。</p>
 *
 * <p>函数值超出 y 轴范围、或为 {@code NaN} / 无穷大（如 {@code 1/x} 在 {@code x = 0} 处、
 * {@code sqrt(x)} 在负数区间）的位置会断开，曲线自然出现缺口。</p>
 *
 * <p>绘制 {@code sin(x)}：</p>
 * <pre>{@code
 * System.out.println(Plotter.plot("sin(x)", -6.5, 6.5, 50, 13));
 * }</pre>
 * <pre>
 *                          |
 *       /--                |   /---
 *     /-   \               |  /    \
 *    /      \              | /      \
 *   /        \             |/        \
 *   |         \            /          |            /
 * -/-----------\-----------|----------\-----------/-
 * *             |         /|           \          |
 *               \        / |            \        /
 *                \      /  |             \      /
 *                 \    /   |              \   /-
 *                  \---    |               \--
 *                          |
 * </pre>
 */
public final class Plotter {

    /** 画布空白格。 */
    private static final char BLANK = ' ';

    /** 曲线水平段。 */
    private static final char FLAT = '-';

    /** 曲线上升段。 */
    private static final char RISING = '/';

    /** 曲线下降段。 */
    private static final char FALLING = '\\';

    /** 曲线跨行段。 */
    private static final char STEEP = '|';

    /** 单独出现的曲线点。 */
    private static final char POINT = '*';

    /** x 轴。 */
    private static final char X_AXIS = '-';

    /** y 轴。 */
    private static final char Y_AXIS = '|';

    /** 坐标原点。 */
    private static final char ORIGIN = '+';

    /** y 轴自动取值时留出的上下边距比例。 */
    private static final double AUTO_RANGE_PADDING = 0.1;

    private Plotter() {
        // 工具类，禁止实例化
    }

    /**
     * 绘制表达式曲线，y 轴范围由区间内的函数值自动推导。
     *
     * <p>先按列采样出最小/最大值，再各留出 {@value #AUTO_RANGE_PADDING} 比例的边距；
     * 函数为常数时上下各展开该常数绝对值的 {@value #AUTO_RANGE_PADDING}（常数为 0 时展开 1）。</p>
     *
     * @param expression 表达式文本，语法见 {@link Expression#parse(String)}
     * @param xMin       x 轴最小值
     * @param xMax       x 轴最大值，必须大于 {@code xMin}
     * @param width      画布宽度（列数），必须大于等于 2
     * @param height     画布高度（行数），必须大于等于 2
     * @return 绘制结果，共 {@code height} 行、每行 {@code width} 个字符，不含结尾换行
     * @throws IllegalArgumentException 当表达式非法或区间、尺寸参数不合法时
     */
    public static String plot(String expression, double xMin, double xMax, int width, int height) {
        Expression parsed = Expression.parse(expression);
        return plot(parsed::evaluate, xMin, xMax, width, height);
    }

    /**
     * 绘制表达式曲线，并自动推导 y 轴范围。
     *
     * @param function 被绘制的函数，不能为 {@code null}
     * @param xMin     x 轴最小值
     * @param xMax     x 轴最大值，必须大于 {@code xMin}
     * @param width    画布宽度（列数），必须大于等于 2
     * @param height   画布高度（行数），必须大于等于 2
     * @return 绘制结果，共 {@code height} 行、每行 {@code width} 个字符，不含结尾换行
     * @throws IllegalArgumentException 当 {@code function} 为 {@code null}、区间内没有可绘制的有限值或尺寸参数不合法时
     */
    public static String plot(DoubleUnaryOperator function, double xMin, double xMax, int width, int height) {
        if (function == null) {
            throw new IllegalArgumentException("被绘制的函数不能为 null");
        }
        checkRange("x", xMin, xMax);
        checkSize(width, height);
        double[] yRange = autoRange(function, xMin, xMax, width);
        return plot(function, xMin, xMax, yRange[0], yRange[1], width, height);
    }

    /**
     * 绘制表达式曲线，x、y 轴范围都由调用方指定。
     *
     * @param expression 表达式文本，语法见 {@link Expression#parse(String)}
     * @param xMin       x 轴最小值
     * @param xMax       x 轴最大值，必须大于 {@code xMin}
     * @param yMin       y 轴最小值
     * @param yMax       y 轴最大值，必须大于 {@code yMin}
     * @param width      画布宽度（列数），必须大于等于 2
     * @param height     画布高度（行数），必须大于等于 2
     * @return 绘制结果，共 {@code height} 行、每行 {@code width} 个字符，不含结尾换行
     * @throws IllegalArgumentException 当表达式非法或区间、尺寸参数不合法时
     */
    public static String plot(String expression, double xMin, double xMax,
                              double yMin, double yMax, int width, int height) {
        Expression parsed = Expression.parse(expression);
        return plot(parsed::evaluate, xMin, xMax, yMin, yMax, width, height);
    }

    /**
     * 绘制曲线，x、y 轴范围都由调用方指定。
     *
     * @param function 被绘制的函数，不能为 {@code null}
     * @param xMin     x 轴最小值
     * @param xMax     x 轴最大值，必须大于 {@code xMin}
     * @param yMin     y 轴最小值
     * @param yMax     y 轴最大值，必须大于 {@code yMin}
     * @param width    画布宽度（列数），必须大于等于 2
     * @param height   画布高度（行数），必须大于等于 2
     * @return 绘制结果，共 {@code height} 行、每行 {@code width} 个字符，不含结尾换行
     * @throws IllegalArgumentException 当 {@code function} 为 {@code null} 或区间、尺寸参数不合法时
     */
    public static String plot(DoubleUnaryOperator function, double xMin, double xMax,
                              double yMin, double yMax, int width, int height) {
        if (function == null) {
            throw new IllegalArgumentException("被绘制的函数不能为 null");
        }
        checkRange("x", xMin, xMax);
        checkRange("y", yMin, yMax);
        checkSize(width, height);

        char[][] canvas = blankCanvas(width, height);
        drawCurve(canvas, function, xMin, xMax, yMin, yMax);
        drawAxes(canvas, xMin, xMax, yMin, yMax);
        return join(canvas);
    }

    private static void checkRange(String axis, double min, double max) {
        if (!(min < max)) {
            throw new IllegalArgumentException(
                    axis + " 轴区间必须满足 min < max，收到: [" + min + ", " + max + "]");
        }
    }

    private static void checkSize(int width, int height) {
        if (width < 2) {
            throw new IllegalArgumentException("画布宽度必须大于等于 2，收到: " + width);
        }
        if (height < 2) {
            throw new IllegalArgumentException("画布高度必须大于等于 2，收到: " + height);
        }
    }

    private static char[][] blankCanvas(int width, int height) {
        char[][] canvas = new char[height][width];
        for (char[] row : canvas) {
            Arrays.fill(row, BLANK);
        }
        return canvas;
    }

    /**
     * 按列采样函数值推导 y 轴范围，无任何有限值时抛出异常。
     */
    private static double[] autoRange(DoubleUnaryOperator function, double xMin, double xMax, int width) {
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        for (int column = 0; column < width; column++) {
            double y = function.applyAsDouble(xAt(column, xMin, xMax, width));
            if (Double.isFinite(y)) {
                low = Math.min(low, y);
                high = Math.max(high, y);
            }
        }
        if (low > high) {
            throw new IllegalArgumentException(
                    "x 轴区间 [" + xMin + ", " + xMax + "] 内函数没有任何可绘制的有限值");
        }
        if (low == high) {
            double padding = (low == 0.0) ? 1.0 : Math.abs(low) * AUTO_RANGE_PADDING;
            return new double[] {low - padding, high + padding};
        }
        double padding = (high - low) * AUTO_RANGE_PADDING;
        return new double[] {low - padding, high + padding};
    }

    private static void drawCurve(char[][] canvas, DoubleUnaryOperator function,
                                  double xMin, double xMax, double yMin, double yMax) {
        int width = canvas[0].length;
        int previousRow = -1;
        for (int column = 0; column < width; column++) {
            double y = function.applyAsDouble(xAt(column, xMin, xMax, width));
            if (!Double.isFinite(y) || y < yMin || y > yMax) {
                // 断线：无定义或超出画布的点不绘制，也不与下一个点相连
                previousRow = -1;
                continue;
            }
            int row = rowAt(y, yMin, yMax, canvas.length);
            if (previousRow < 0) {
                canvas[row][column] = POINT;
            } else if (row == previousRow) {
                canvas[row][column] = FLAT;
            } else {
                int step = row > previousRow ? 1 : -1;
                for (int fill = previousRow + step; fill != row; fill += step) {
                    canvas[fill][column] = STEEP;
                }
                canvas[row][column] = row < previousRow ? RISING : FALLING;
            }
            previousRow = row;
        }
    }

    private static void drawAxes(char[][] canvas, double xMin, double xMax, double yMin, double yMax) {
        int height = canvas.length;
        int width = canvas[0].length;
        boolean horizontal = yMin <= 0.0 && 0.0 <= yMax;
        boolean vertical = xMin <= 0.0 && 0.0 <= xMax;
        if (!horizontal && !vertical) {
            return;
        }
        int axisRow = horizontal ? rowAt(0.0, yMin, yMax, height) : 0;
        int axisColumn = vertical ? columnAt(0.0, xMin, xMax, width) : 0;
        // 先判断原点格是否空闲：坐标轴填充会占用它，必须在填充前记录
        boolean freeOrigin = horizontal && vertical && canvas[axisRow][axisColumn] == BLANK;

        if (horizontal) {
            for (int column = 0; column < width; column++) {
                if (canvas[axisRow][column] == BLANK) {
                    canvas[axisRow][column] = X_AXIS;
                }
            }
        }
        if (vertical) {
            for (int row = 0; row < height; row++) {
                if (canvas[row][axisColumn] == BLANK) {
                    canvas[row][axisColumn] = Y_AXIS;
                }
            }
        }
        if (freeOrigin) {
            canvas[axisRow][axisColumn] = ORIGIN;
        }
    }

    private static String join(char[][] canvas) {
        StringBuilder plot = new StringBuilder();
        for (int row = 0; row < canvas.length; row++) {
            if (row > 0) {
                plot.append('\n');
            }
            plot.append(canvas[row]);
        }
        return plot.toString();
    }

    /** 第 {@code column} 列对应的 x 取值，列首为 {@code xMin}、列尾为 {@code xMax}。 */
    private static double xAt(int column, double xMin, double xMax, int width) {
        return xMin + (xMax - xMin) * column / (width - 1);
    }

    /** 把 y 值映射到行号，结果落在 {@code [0, height - 1]} 内。 */
    private static int rowAt(double y, double yMin, double yMax, int height) {
        double ratio = (yMax - y) / (yMax - yMin);
        return (int) Math.round(ratio * (height - 1));
    }

    /** 把 x 值映射到列号，结果落在 {@code [0, width - 1]} 内。 */
    private static int columnAt(double x, double xMin, double xMax, int width) {
        double ratio = (x - xMin) / (xMax - xMin);
        return (int) Math.round(ratio * (width - 1));
    }
}
