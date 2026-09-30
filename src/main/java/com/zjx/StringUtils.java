package com.zjx;

/**
 * 字符串工具类：提供反转、回文判断、单词计数与标题化等常见文本操作。
 */
public final class StringUtils {

    private StringUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 反转字符串。
     *
     * @param input 原始字符串，不能为 {@code null}
     * @return 反转后的字符串
     * @throws IllegalArgumentException 当 {@code input} 为 {@code null} 时
     */
    public static String reverse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("输入字符串不能为 null");
        }
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * 判断字符串是否为回文（忽略大小写及所有非字母、非数字字符）。
     *
     * <p>例如 {@code "A man, a plan, a canal: Panama"} 与 {@code "上海自来水来自海上"} 均视为回文。</p>
     *
     * @param input 待判断的字符串，不能为 {@code null}
     * @return 若为回文返回 {@code true}
     * @throws IllegalArgumentException 当 {@code input} 为 {@code null} 时
     */
    public static boolean isPalindrome(String input) {
        if (input == null) {
            throw new IllegalArgumentException("输入字符串不能为 null");
        }
        int left = 0;
        int right = input.length() - 1;
        while (left < right) {
            char lc = input.charAt(left);
            char rc = input.charAt(right);
            if (!isAlphanumeric(lc)) {
                left++;
                continue;
            }
            if (!isAlphanumeric(rc)) {
                right--;
                continue;
            }
            if (Character.toLowerCase(lc) != Character.toLowerCase(rc)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /**
     * 统计以空白分隔的单词数量。
     *
     * @param input 原始字符串，可为 {@code null} 或空白
     * @return 单词数量，空输入返回 0
     */
    public static int wordCount(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }
        String[] words = input.trim().split("\\s+");
        return words.length;
    }

    /**
     * 将每个单词的首字母大写（标题化）。
     *
     * @param input 原始字符串，可为 {@code null}
     * @return 标题化后的字符串；空输入返回空串
     */
    public static String titleCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        String[] words = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    private static boolean isAlphanumeric(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9');
    }
}
