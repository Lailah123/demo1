package com.zjx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 摩尔斯电码工具类：提供文本与摩尔斯电码的双向转换。
 *
 * <p>字母之间用单个空格分隔，单词之间用 {@code /} 分隔；
 * 小写字母自动转为大写，连续空格视为一个单词分隔。</p>
 *
 * <p>支持的字符为 A-Z、0-9 与常用标点，完整清单见 {@link #supportedCharacters()}；
 * 传入不支持的字符或无法识别的电码时抛出 {@link IllegalArgumentException}。</p>
 */
public final class MorseCode {

    /** 字符到摩尔斯电码的映射（A-Z、0-9 与常用标点）。 */
    private static final Map<Character, String> ENCODE_TABLE;
    /** 摩尔斯电码到字符的反向映射。 */
    private static final Map<String, Character> DECODE_TABLE;

    static {
        Map<Character, String> table = new HashMap<Character, String>();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String[] letterCodes = {
            ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
            "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
            "..-", "...-", ".--", "-..-", "-.--", "--.."
        };
        for (int i = 0; i < letters.length(); i++) {
            table.put(letters.charAt(i), letterCodes[i]);
        }
        String digits = "0123456789";
        String[] digitCodes = {
            "-----", ".----", "..---", "...--", "....-", ".....",
            "-....", "--...", "---..", "----."
        };
        for (int i = 0; i < digits.length(); i++) {
            table.put(digits.charAt(i), digitCodes[i]);
        }
        table.put('.', ".-.-.-");
        table.put(',', "--..--");
        table.put('?', "..--..");
        table.put('!', "-.-.--");
        table.put('\'', ".----.");
        table.put('\"', ".-..-.");
        table.put('-', "-....-");
        table.put('/', "-..-.");
        table.put('(', "-.--.");
        table.put(')', "-.--.-");
        table.put(':', "---...");
        table.put(';', "-.-.-.");
        table.put('=', "-...-");
        table.put('+', ".-.-.");
        table.put('_', "..--.-");
        ENCODE_TABLE = Collections.unmodifiableMap(table);

        Map<String, Character> reverse = new HashMap<String, Character>();
        for (Map.Entry<Character, String> entry : table.entrySet()) {
            reverse.put(entry.getValue(), entry.getKey());
        }
        DECODE_TABLE = Collections.unmodifiableMap(reverse);
    }

    private MorseCode() {
        // 工具类，禁止实例化
    }

    /**
     * 将文本编码为摩尔斯电码。
     *
     * @param text 待编码文本，支持 A-Z、0-9 与常用标点，小写自动转大写
     * @return 摩尔斯电码，字母间以单个空格分隔，单词间以 {@code /} 分隔
     * @throws IllegalArgumentException 当 {@code text} 为 {@code null} 或包含不支持的字符时
     */
    public static String encode(String text) {
        if (text == null) {
            throw new IllegalArgumentException("待编码文本不能为 null");
        }
        StringBuilder result = new StringBuilder();
        boolean wordStarted = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == ' ') {
                wordStarted = false;
                continue;
            }
            String code = ENCODE_TABLE.get(Character.toUpperCase(ch));
            if (code == null) {
                throw new IllegalArgumentException("不支持的字符: '" + ch + "'");
            }
            if (result.length() > 0) {
                result.append(wordStarted ? ' ' : " / ");
            }
            result.append(code);
            wordStarted = true;
        }
        return result.toString();
    }

    /**
     * 将摩尔斯电码解码为文本（大写输出）。
     *
     * <p>容忍字母间的多余空格与 {@code /} 前后的空白；对
     * {@link #encode(String)} 的输出做解码可还原原文（空格规范化为单个）。</p>
     *
     * @param morse 摩尔斯电码，字母间以空格分隔，单词间以 {@code /} 分隔
     * @return 解码后的文本（大写）
     * @throws IllegalArgumentException 当 {@code morse} 为 {@code null} 或包含无法识别的电码时
     */
    public static String decode(String morse) {
        if (morse == null) {
            throw new IllegalArgumentException("待解码电码不能为 null");
        }
        String trimmed = morse.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String word : trimmed.split("/")) {
            StringBuilder decoded = new StringBuilder();
            for (String code : word.trim().split("\\s+")) {
                if (code.isEmpty()) {
                    continue;
                }
                Character ch = DECODE_TABLE.get(code);
                if (ch == null) {
                    throw new IllegalArgumentException("无法识别的电码: \"" + code + "\"");
                }
                decoded.append(ch.charValue());
            }
            if (decoded.length() == 0) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(decoded);
        }
        return result.toString();
    }

    /**
     * 返回受支持的字符集合（A-Z、0-9 与常用标点，不含空格）。
     *
     * @return 受支持字符的不可变视图
     */
    public static Set<Character> supportedCharacters() {
        return Collections.unmodifiableSet(ENCODE_TABLE.keySet());
    }
}
