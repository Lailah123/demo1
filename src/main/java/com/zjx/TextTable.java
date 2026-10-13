package com.zjx;

import java.util.Arrays;
import java.util.List;

/**
 * 文本表格工具类：把二维字符串数据渲染成对齐的 ASCII 表格。
 *
 * <p>列宽按<b>显示宽度</b>计算——东亚全角字符（汉字、假名、谚文、全角标点等）占 2 列，
 * 其余字符占 1 列，因此中英文混排也能对齐。渲染结果不含结尾换行。</p>
 *
 * <p>示例：</p>
 *
 * <pre>{@code
 * List<String[]> rows = new ArrayList<>();
 * rows.add(new String[] {"张三", "北京", "28"});
 * rows.add(new String[] {"Li Lei", "Shanghai", "9"});
 *
 * String table = TextTable.render(
 *         new String[] {"姓名", "城市", "年龄"},
 *         rows,
 *         TextTable.Align.LEFT, TextTable.Align.LEFT, TextTable.Align.RIGHT);
 * System.out.println(table);
 * }</pre>
 *
 * <pre>
 * +--------+----------+------+
 * | 姓名   | 城市     | 年龄 |
 * +--------+----------+------+
 * | 张三   | 北京     |   28 |
 * | Li Lei | Shanghai |    9 |
 * +--------+----------+------+
 * </pre>
 */
public final class TextTable {

    /** 单元格内容的水平对齐方式。 */
    public enum Align {
        /** 左对齐，多余空格补在右侧。 */
        LEFT,
        /** 居中，多余空格优先补在右侧。 */
        CENTER,
        /** 右对齐，多余空格补在左侧。 */
        RIGHT
    }

    private static final String EMPTY = "";

    private TextTable() {
        // 工具类，禁止实例化
    }

    /**
     * 计算字符串在等宽终端下占用的列数。
     *
     * <p>东亚全角字符计 2 列，其余字符计 1 列；控制字符也按 1 列计算。</p>
     *
     * @param text 待测量的字符串，可为 {@code null}
     * @return 显示宽度，{@code null} 返回 0
     */
    public static int displayWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            width += isFullWidth(codePoint) ? 2 : 1;
            i += Character.charCount(codePoint);
        }
        return width;
    }

    /**
     * 渲染无表头的表格，所有列左对齐。
     *
     * @param rows 数据行，不能为 {@code null}；行内元素可为 {@code null}（视为整行空单元格）
     * @return 渲染结果，不含结尾换行；无任何列时返回空串
     * @throws IllegalArgumentException 当 {@code rows} 为 {@code null} 时
     */
    public static String render(List<String[]> rows) {
        return render(null, rows, (Align[]) null);
    }

    /**
     * 渲染带表头的表格，所有列左对齐。
     *
     * @param headers 表头单元格，可为 {@code null} 表示无表头
     * @param rows    数据行，不能为 {@code null}；行内元素可为 {@code null}（视为整行空单元格）
     * @return 渲染结果，不含结尾换行；无任何列时返回空串
     * @throws IllegalArgumentException 当 {@code rows} 为 {@code null} 时
     */
    public static String render(String[] headers, List<String[]> rows) {
        return render(headers, rows, (Align[]) null);
    }

    /**
     * 渲染表格，逐列指定对齐方式。
     *
     * <p>{@code aligns} 比实际列数短时，缺失的列回退为 {@link Align#LEFT}；
     * 传入 {@code null} 时所有列左对齐。表头与数据行一起参与列宽计算，
     * 每列宽度取该列所有单元格显示宽度的最大值。</p>
     *
     * @param headers 表头单元格，可为 {@code null} 表示无表头
     * @param rows    数据行，不能为 {@code null}；行内元素可为 {@code null}（视为整行空单元格）
     * @param aligns  每列的对齐方式，可为 {@code null}
     * @return 渲染结果，不含结尾换行；无任何列时返回空串
     * @throws IllegalArgumentException 当 {@code rows} 为 {@code null} 时
     */
    public static String render(String[] headers, List<String[]> rows, Align... aligns) {
        if (rows == null) {
            throw new IllegalArgumentException("数据行不能为 null");
        }

        int columns = headers == null ? 0 : headers.length;
        for (String[] row : rows) {
            if (row != null && row.length > columns) {
                columns = row.length;
            }
        }
        if (columns == 0) {
            return EMPTY;
        }

        int[] widths = new int[columns];
        measure(headers, widths);
        for (String[] row : rows) {
            measure(row, widths);
        }

        String border = borderLine(widths);
        StringBuilder table = new StringBuilder();
        table.append(border).append('\n');
        if (headers != null) {
            appendRow(table, headers, widths, aligns);
            if (!rows.isEmpty()) {
                table.append(border).append('\n');
            }
        }
        for (String[] row : rows) {
            appendRow(table, row, widths, aligns);
        }
        table.append(border);
        return table.toString();
    }

    private static void measure(String[] cells, int[] widths) {
        for (int column = 0; column < widths.length; column++) {
            int width = displayWidth(cellAt(cells, column));
            if (width > widths[column]) {
                widths[column] = width;
            }
        }
    }

    private static void appendRow(StringBuilder table, String[] cells, int[] widths, Align[] aligns) {
        for (int column = 0; column < widths.length; column++) {
            table.append("| ")
                    .append(pad(cellAt(cells, column), widths[column], alignAt(aligns, column)))
                    .append(' ');
        }
        table.append("|\n");
    }

    private static String borderLine(int[] widths) {
        StringBuilder border = new StringBuilder();
        for (int width : widths) {
            border.append('+').append(repeat('-', width + 2));
        }
        return border.append('+').toString();
    }

    /**
     * 取出指定列的单元格内容：越界或为 {@code null} 时返回空串，并把会破坏表格结构的
     * 换行符、制表符替换为空格。
     */
    private static String cellAt(String[] cells, int column) {
        if (cells == null || column >= cells.length || cells[column] == null) {
            return EMPTY;
        }
        return normalize(cells[column]);
    }

    private static String normalize(String cell) {
        if (cell.indexOf('\n') < 0 && cell.indexOf('\r') < 0 && cell.indexOf('\t') < 0) {
            return cell;
        }
        char[] chars = cell.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n' || chars[i] == '\r' || chars[i] == '\t') {
                chars[i] = ' ';
            }
        }
        return new String(chars);
    }

    private static Align alignAt(Align[] aligns, int column) {
        if (aligns == null || column >= aligns.length || aligns[column] == null) {
            return Align.LEFT;
        }
        return aligns[column];
    }

    private static String pad(String cell, int width, Align align) {
        int padding = width - displayWidth(cell);
        switch (align) {
            case RIGHT:
                return repeat(' ', padding) + cell;
            case CENTER:
                int left = padding / 2;
                return repeat(' ', left) + cell + repeat(' ', padding - left);
            case LEFT:
            default:
                return cell + repeat(' ', padding);
        }
    }

    private static String repeat(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    /**
     * 判断码点是否属于东亚全角（Unicode East Asian Width = Wide / Fullwidth）区段。
     *
     * <p>取常用区段的近似集合，覆盖 CJK 汉字及其扩展区、假名、谚文、注音符号、
     * 全角标点与全角字母；窄区段（如 {@code U+303F} 汉字半填充空格）按 UAX #11
     * 的官方分类排除在外。</p>
     */
    private static boolean isFullWidth(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)       // 谚文字母
                || (codePoint >= 0x2E80 && codePoint <= 0x303E)   // CJK 部首、康熙部首、CJK 符号标点
                || (codePoint >= 0x3041 && codePoint <= 0x33FF)   // 平假名、片假名、注音、CJK 兼容
                || (codePoint >= 0x3400 && codePoint <= 0x4DBF)   // CJK 扩展 A
                || (codePoint >= 0x4E00 && codePoint <= 0x9FFF)   // CJK 统一表意文字
                || (codePoint >= 0xA000 && codePoint <= 0xA4CF)   // 彝文
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)   // 谚文音节
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)   // CJK 兼容表意文字
                || (codePoint >= 0xFE10 && codePoint <= 0xFE19)   // 竖排标点
                || (codePoint >= 0xFE30 && codePoint <= 0xFE6F)   // CJK 兼容形式、小写变体
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)   // 全角 ASCII、全角标点
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)   // 全角货币符号
                || (codePoint >= 0x20000 && codePoint <= 0x2FFFD) // CJK 扩展 B~F
                || (codePoint >= 0x30000 && codePoint <= 0x3FFFD); // CJK 扩展 G
    }
}
