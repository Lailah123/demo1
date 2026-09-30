package com.zjx;

/**
 * 示例类：提供可测试的问候逻辑。
 *
 * @author zhaojx
 * @version 1.0
 */
public class Test1 {

    /** 默认问候语。 */
    private static final String GREETING = "hello world!";

    private Test1() {
        // 工具类，禁止实例化
    }

    /**
     * 返回问候语文本。
     *
     * @return 问候语字符串
     */
    public static String message() {
        return GREETING;
    }

    public static void main(String[] args) {
        System.out.println(message());
    }
}
