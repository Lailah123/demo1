package com.zjx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FizzBuzz 经典问题实现：能被 3 整除输出 Fizz，能被 5 整除输出 Buzz，
 * 同时被 3 和 5 整除输出 FizzBuzz，否则输出数字本身。
 */
public final class FizzBuzz {

    private FizzBuzz() {
        // 工具类，禁止实例化
    }

    /**
     * 返回单个数字对应的 FizzBuzz 结果。
     *
     * @param n 待转换的数字
     * @return {@code "FizzBuzz"}、{@code "Fizz"}、{@code "Buzz"} 或数字的字符串形式
     */
    public static String of(int n) {
        if (n % 15 == 0) {
            return "FizzBuzz";
        }
        if (n % 3 == 0) {
            return "Fizz";
        }
        if (n % 5 == 0) {
            return "Buzz";
        }
        return String.valueOf(n);
    }

    /**
     * 返回从 1 到 {@code n}（含）的 FizzBuzz 序列。
     *
     * @param n 序列上限，必须大于等于 1
     * @return 长度为 {@code n} 的不可变结果列表
     * @throws IllegalArgumentException 当 {@code n} 小于 1 时
     */
    public static List<String> upTo(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("序列上限必须大于等于 1，收到: " + n);
        }
        List<String> result = new ArrayList<String>(n);
        for (int i = 1; i <= n; i++) {
            result.add(of(i));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 返回从 1 到 {@code n}（含）的 FizzBuzz 序列，以逗号分隔。
     *
     * @param n 序列上限，必须大于等于 1
     * @return 形如 {@code "1, 2, Fizz, 4, Buzz, ..."} 的字符串
     * @throws IllegalArgumentException 当 {@code n} 小于 1 时
     */
    public static String print(int n) {
        List<String> sequence = upTo(n);
        StringBuilder result = new StringBuilder();
        for (String item : sequence) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(item);
        }
        return result.toString();
    }
}
