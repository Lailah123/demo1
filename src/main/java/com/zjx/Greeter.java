package com.zjx;

/**
 * 多语言问候器：根据指定的语言与名字生成问候语。
 *
 * <p>对 {@link Test1} 的单一问候做了扩展，使项目支持多种语言的个性化问候。</p>
 */
public final class Greeter {

    private Greeter() {
        // 工具类，禁止实例化
    }

    /**
     * 支持的问候语言及其问候模板。
     */
    public enum Language {
        /** 中文。 */
        CHINESE("你好，%s！", "你好！"),
        /** 英文。 */
        ENGLISH("Hello, %s!", "Hello!"),
        /** 日文。 */
        JAPANESE("こんにちは、%s！", "こんにちは！"),
        /** 韩文。 */
        KOREAN("안녕하세요, %s!", "안녕하세요!"),
        /** 西班牙文。 */
        SPANISH("¡Hola, %s!", "¡Hola!"),
        /** 法文。 */
        FRENCH("Bonjour, %s !", "Bonjour !");

        private final String withName;
        private final String withoutName;

        Language(String withName, String withoutName) {
            this.withName = withName;
            this.withoutName = withoutName;
        }

        /** 带名字的问候模板。 */
        public String withName() {
            return withName;
        }

        /** 不带名字的问候模板。 */
        public String withoutName() {
            return withoutName;
        }
    }

    /**
     * 按语言生成个性化问候语，名字为空时返回通用问候。
     *
     * @param name     被问候者的名字，可为 {@code null} 或空白
     * @param language 问候语言，不能为 {@code null}
     * @return 问候语字符串
     * @throws IllegalArgumentException 当 {@code language} 为 {@code null} 时
     */
    public static String greet(String name, Language language) {
        if (language == null) {
            throw new IllegalArgumentException("问候语言不能为 null");
        }
        String trimmed = (name == null) ? "" : name.trim();
        if (trimmed.isEmpty()) {
            return language.withoutName();
        }
        return String.format(language.withName(), trimmed);
    }

    /**
     * 生成不带名字的通用问候。
     *
     * @param language 问候语言，不能为 {@code null}
     * @return 问候语字符串
     * @throws IllegalArgumentException 当 {@code language} 为 {@code null} 时
     */
    public static String greet(Language language) {
        return greet(null, language);
    }
}
