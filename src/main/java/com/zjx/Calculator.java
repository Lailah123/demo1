package com.zjx;

/**
 * 计算器工具类：提供四则运算、阶乘与快速幂运算。
 *
 * <p>对除零、负数阶乘等非法输入抛出明确的异常，便于调用方处理。</p>
 */
public final class Calculator {

    /** 阶乘结果使用 {@code long} 表示时，能安全支持的最大输入。 */
    private static final int MAX_FACTORIAL_INPUT = 20;

    private Calculator() {
        // 工具类，禁止实例化
    }

    /**
     * 加法。
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return {@code a + b}
     */
    public static double add(double a, double b) {
        return a + b;
    }

    /**
     * 减法。
     *
     * @param a 被减数
     * @param b 减数
     * @return {@code a - b}
     */
    public static double subtract(double a, double b) {
        return a - b;
    }

    /**
     * 乘法。
     *
     * @param a 左操作数
     * @param b 右操作数
     * @return {@code a * b}
     */
    public static double multiply(double a, double b) {
        return a * b;
    }

    /**
     * 除法。
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @return {@code dividend / divisor}
     * @throws ArithmeticException 当除数为 0 时
     */
    public static double divide(double dividend, double divisor) {
        if (divisor == 0.0d) {
            throw new ArithmeticException("除数不能为 0");
        }
        return dividend / divisor;
    }

    /**
     * 计算阶乘 {@code n!}。
     *
     * @param n 非负整数
     * @return {@code n} 的阶乘
     * @throws IllegalArgumentException 当 {@code n} 为负数时
     * @throws ArithmeticException      当结果超出 {@code long} 可表示范围时（{@code n > 20}）
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("阶乘只支持非负整数，收到: " + n);
        }
        if (n > MAX_FACTORIAL_INPUT) {
            throw new ArithmeticException("阶乘结果超出 long 范围: " + n + "! > 20!");
        }
        long result = 1L;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * 计算 {@code base} 的 {@code exponent} 次幂（快速幂，支持负指数）。
     *
     * @param base     底数
     * @param exponent 指数，可为负整数
     * @return {@code base ^ exponent}
     * @throws ArithmeticException 当底数为 0 且指数为负时
     */
    public static double power(double base, int exponent) {
        if (base == 0.0d && exponent < 0) {
            throw new ArithmeticException("0 的负次幂无意义");
        }
        double b = base;
        long e = exponent;
        if (e < 0) {
            b = 1.0d / b;
            e = -e;
        }
        double result = 1.0d;
        while (e > 0) {
            if ((e & 1L) == 1L) {
                result *= b;
            }
            b *= b;
            e >>= 1;
        }
        return result;
    }
}
