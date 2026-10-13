package com.zjx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字母转字符艺术字工具类。
 *
 * <p>把普通文本渲染成由字符拼成的大字（banner 风格），一行文本固定占用
 * {@value #FONT_HEIGHT} 行输出。基于内置的 5 行点阵字模实现，零外部依赖。</p>
 *
 * <p>支持的字符为 A-Z、0-9、空格与常用标点，完整清单见 {@link #supportedCharacters()}；
 * 小写字母自动转为大写，遇到不支持的字符时抛出 {@link IllegalArgumentException}。</p>
 *
 * <p>渲染示例：</p>
 * <pre>
 * LetterArt.render("HI");
 * </pre>
 * <p>输出：</p>
 * <pre>
 * #   # #####
 * #   #   #
 * #####   #
 * #   #   #
 * #   # #####
 * </pre>
 */
public final class LetterArt {

    /** 字模高度：每个字符固定占用 {@value} 行输出。 */
    public static final int FONT_HEIGHT = 5;

    /** 默认字母间距（以空格字符计）。 */
    private static final int DEFAULT_LETTER_SPACING = 1;

    /** 字母间距上限，防止过大间距导致输出体积失控。 */
    private static final int MAX_LETTER_SPACING = 20;

    /** 内置字模：键为字符，值为 {@value #FONT_HEIGHT} 行等宽点阵，{@code '#'} 表示填充。 */
    private static final Map<Character, String[]> FONT = buildFont();

    private LetterArt() {
        // 工具类，禁止实例化
    }

    /**
     * 将文本渲染为字符艺术字，使用默认字母间距。
     *
     * @param text 待渲染文本，不能为 {@code null} 或空串
     * @return 以换行结尾的多行艺术字
     * @throws IllegalArgumentException 当文本为 {@code null}、为空或含不支持的字符时
     */
    public static String render(String text) {
        return render(text, DEFAULT_LETTER_SPACING);
    }

    /**
     * 将文本渲染为字符艺术字。
     *
     * @param text          待渲染文本，不能为 {@code null} 或空串
     * @param letterSpacing 相邻字符之间的空格数，取值范围 [0, 20]
     * @return 以换行结尾的多行艺术字
     * @throws IllegalArgumentException 当文本非法或间距超出范围时
     */
    public static String render(String text, int letterSpacing) {
        List<String> lines = renderLines(text, letterSpacing);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * 将文本渲染为字符艺术字，使用默认字母间距。
     *
     * <p>与 {@link #render(String)} 的区别是返回值不含换行符，便于调用方自行排版。</p>
     *
     * @param text 待渲染文本，不能为 {@code null} 或空串
     * @return 恰好 {@value #FONT_HEIGHT} 个元素的不可变列表，每个元素为一行
     * @throws IllegalArgumentException 当文本为 {@code null}、为空或含不支持的字符时
     */
    public static List<String> renderLines(String text) {
        return renderLines(text, DEFAULT_LETTER_SPACING);
    }

    /**
     * 将文本渲染为字符艺术字，返回按行切分的结果。
     *
     * @param text          待渲染文本，不能为 {@code null} 或空串
     * @param letterSpacing 相邻字符之间的空格数，取值范围 [0, 20]
     * @return 恰好 {@value #FONT_HEIGHT} 个元素的不可变列表，每个元素为一行
     * @throws IllegalArgumentException 当文本非法或间距超出范围时
     */
    public static List<String> renderLines(String text, int letterSpacing) {
        String normalized = normalize(text, letterSpacing);
        String gap = spaces(letterSpacing);
        List<String> lines = new ArrayList<String>(FONT_HEIGHT);
        for (int row = 0; row < FONT_HEIGHT; row++) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < normalized.length(); i++) {
                if (i > 0) {
                    line.append(gap);
                }
                line.append(FONT.get(normalized.charAt(i))[row]);
            }
            lines.add(line.toString());
        }
        return Collections.unmodifiableList(lines);
    }

    /**
     * 返回全部受支持的字符（含空格）。
     *
     * @return 不可变字符集合，顺序与字模定义一致
     */
    public static Set<Character> supportedCharacters() {
        return FONT.keySet();
    }

    /** 校验入参并把小写字母归一化为大写。 */
    private static String normalize(String text, int letterSpacing) {
        if (text == null) {
            throw new IllegalArgumentException("文本不能为 null");
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("文本不能为空");
        }
        if (letterSpacing < 0) {
            throw new IllegalArgumentException("字母间距不能为负数，收到: " + letterSpacing);
        }
        if (letterSpacing > MAX_LETTER_SPACING) {
            throw new IllegalArgumentException(
                    "字母间距过大（上限 " + MAX_LETTER_SPACING + "），收到: " + letterSpacing);
        }
        String upper = text.toUpperCase();
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            if (!FONT.containsKey(ch)) {
                throw new IllegalArgumentException(
                        "不支持的字符: " + describe(ch) + "（位置 " + (i + 1) + "）");
            }
        }
        return upper;
    }

    /** 把字符渲染为便于阅读的报错片段。 */
    private static String describe(char ch) {
        switch (ch) {
            case '\n':
                return "'\\n'";
            case '\r':
                return "'\\r'";
            case '\t':
                return "'\\t'";
            default:
                return "'" + ch + "'";
        }
    }

    private static String spaces(int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    private static Map<Character, String[]> buildFont() {
        Map<Character, String[]> font = new LinkedHashMap<Character, String[]>();

        glyph(font, 'A', " ### ", "#   #", "#####", "#   #", "#   #");
        glyph(font, 'B', "#### ", "#   #", "#### ", "#   #", "#### ");
        glyph(font, 'C', " ####", "#    ", "#    ", "#    ", " ####");
        glyph(font, 'D', "#### ", "#   #", "#   #", "#   #", "#### ");
        glyph(font, 'E', "#####", "#    ", "#### ", "#    ", "#####");
        glyph(font, 'F', "#####", "#    ", "#### ", "#    ", "#    ");
        glyph(font, 'G', " ### ", "#   #", "# ###", "#   #", " ### ");
        glyph(font, 'H', "#   #", "#   #", "#####", "#   #", "#   #");
        glyph(font, 'I', "#####", "  #  ", "  #  ", "  #  ", "#####");
        glyph(font, 'J', "    #", "    #", "    #", "#   #", " ### ");
        glyph(font, 'K', "#   #", "#  # ", "###  ", "#  # ", "#   #");
        glyph(font, 'L', "#    ", "#    ", "#    ", "#    ", "#####");
        glyph(font, 'M', "#   #", "## ##", "# # #", "#   #", "#   #");
        glyph(font, 'N', "#   #", "##  #", "# # #", "#  ##", "#   #");
        glyph(font, 'O', " ### ", "#   #", "#   #", "#   #", " ### ");
        glyph(font, 'P', "#### ", "#   #", "#### ", "#    ", "#    ");
        glyph(font, 'Q', " ### ", "#   #", "# # #", "#  # ", " ## #");
        glyph(font, 'R', "#### ", "#   #", "#### ", "#  # ", "#   #");
        glyph(font, 'S', " ####", "#    ", " ### ", "    #", "#### ");
        glyph(font, 'T', "#####", "  #  ", "  #  ", "  #  ", "  #  ");
        glyph(font, 'U', "#   #", "#   #", "#   #", "#   #", " ### ");
        glyph(font, 'V', "#   #", "#   #", "#   #", " # # ", "  #  ");
        glyph(font, 'W', "#   #", "#   #", "# # #", "## ##", "#   #");
        glyph(font, 'X', "#   #", " # # ", "  #  ", " # # ", "#   #");
        glyph(font, 'Y', "#   #", " # # ", "  #  ", "  #  ", "  #  ");
        glyph(font, 'Z', "#####", "   # ", "  #  ", " #   ", "#####");

        glyph(font, '0', " ### ", "#  ##", "# # #", "##  #", " ### ");
        glyph(font, '1', "  #  ", " ##  ", "  #  ", "  #  ", " ### ");
        glyph(font, '2', " ### ", "#   #", "  ## ", " #   ", "#####");
        glyph(font, '3', "#### ", "    #", " ### ", "    #", "#### ");
        glyph(font, '4', "#   #", "#   #", "#####", "    #", "    #");
        glyph(font, '5', "#####", "#    ", "#### ", "    #", "#### ");
        glyph(font, '6', " ### ", "#    ", "#### ", "#   #", " ### ");
        glyph(font, '7', "#####", "   # ", "  #  ", " #   ", "#    ");
        glyph(font, '8', " ### ", "#   #", " ### ", "#   #", " ### ");
        glyph(font, '9', " ### ", "#   #", " ####", "    #", " ### ");

        glyph(font, ' ', "   ", "   ", "   ", "   ", "   ");
        glyph(font, '.', "  ", "  ", "  ", "  ", "# ");
        glyph(font, ',', "  ", "  ", "  ", " #", "# ");
        glyph(font, '!', "#", "#", "#", " ", "#");
        glyph(font, '?', " ### ", "#   #", "   # ", "     ", "  #  ");
        glyph(font, '\'', "#", "#", " ", " ", " ");
        glyph(font, '"', "# #", "# #", "   ", "   ", "   ");
        glyph(font, '-', "   ", "   ", "###", "   ", "   ");
        glyph(font, '_', "     ", "     ", "     ", "     ", "#####");
        glyph(font, '+', "     ", "  #  ", " ### ", "  #  ", "     ");
        glyph(font, '=', "     ", " ### ", "     ", " ### ", "     ");
        glyph(font, ':', " ", "#", " ", "#", " ");
        glyph(font, ';', "  ", " #", "  ", " #", "# ");
        glyph(font, '(', " #", "# ", "# ", "# ", " #");
        glyph(font, ')', "# ", " #", " #", " #", "# ");
        glyph(font, '[', "##", "# ", "# ", "# ", "##");
        glyph(font, ']', "##", " #", " #", " #", "##");
        glyph(font, '/', "    #", "   # ", "  #  ", " #   ", "#    ");

        return Collections.unmodifiableMap(font);
    }

    /** 注册一个字模，并校验其行数与各行等宽。 */
    private static void glyph(Map<Character, String[]> font, char ch, String... rows) {
        if (rows.length != FONT_HEIGHT) {
            throw new IllegalStateException(
                    "字模 '" + ch + "' 行数应为 " + FONT_HEIGHT + "，实际为 " + rows.length);
        }
        int width = rows[0].length();
        if (width == 0) {
            throw new IllegalStateException("字模 '" + ch + "' 宽度不能为 0");
        }
        for (String row : rows) {
            if (row.length() != width) {
                throw new IllegalStateException("字模 '" + ch + "' 各行宽度不一致");
            }
        }
        font.put(ch, rows);
    }
}
