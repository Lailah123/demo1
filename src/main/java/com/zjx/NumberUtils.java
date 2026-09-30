package com.zjx;

/**
 * 数字工具类：提供斐波那契数列、质数判断与最大公约数计算。
 */
public final class NumberUtils {

    /** 斐波那契结果使用 {@code long} 表示时，能安全支持的最大下标。 */
    private static final int MAX_FIBONACCI_INDEX = 92;

    private NumberUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 计算第 {@code n} 项斐波那契数（从 0 开始：F(0)=0, F(1)=1）。
     *
     * @param n 非负下标
     * @return 第 {@code n} 项斐波那契数
     * @throws IllegalArgumentException 当 {@code n} 为负数时
     * @throws ArithmeticException      当结果超出 {@code long} 可表示范围时（{@code n > 92}）
     */
    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("斐波那契下标必须非负，收到: " + n);
        }
        if (n > MAX_FIBONACCI_INDEX) {
            throw new ArithmeticException("斐波那契结果超出 long 范围: F(" + n + ")");
        }
        long prev = 0L;
        long curr = 1L;
        for (int i = 0; i < n; i++) {
            long next = prev + curr;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    /**
     * 判断一个数是否为质数（大于 1 且只能被 1 和自身整除）。
     *
     * @param n 待判断的数
     * @return 若为质数返回 {@code true}
     */
    public static boolean isPrime(long n) {
        if (n < 2L) {
            return false;
        }
        if (n == 2L || n == 3L) {
            return true;
        }
        if (n % 2L == 0L) {
            return false;
        }
        for (long i = 3L; i * i <= n; i += 2L) {
            if (n % i == 0L) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算两个整数的最大公约数（欧几里得算法，结果非负）。
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return {@code |gcd(a, b)|}
     */
    public static int gcd(int a, int b) {
        int x = Math.abs(a);
        int y = Math.abs(b);
        while (y != 0) {
            int remainder = x % y;
            x = y;
            y = remainder;
        }
        return x;
    }
}
