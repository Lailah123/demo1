package com.zjx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

/**
 * 数学表达式求值器：把形如 {@code "sin(x) * exp(-x / 5)"} 的文本编译成可反复调用的函数。
 *
 * <p>支持的语法：</p>
 * <ul>
 *   <li>运算符：{@code +}、{@code -}、{@code *}、{@code /}、{@code %}（取余）、
 *       {@code ^}（乘方，右结合）与圆括号，一元正负号</li>
 *   <li>常量：{@code pi}（圆周率）、{@code e}（自然常数）</li>
 *   <li>变量：{@code x}，取值由 {@link #evaluate(double)} 传入</li>
 *   <li>单参数函数：{@code sin cos tan asin acos atan sinh cosh tanh sqrt cbrt abs
 *       exp ln lg floor ceil round signum}</li>
 *   <li>双参数函数：{@code pow atan2 hypot min max}</li>
 * </ul>
 *
 * <p>标识符与函数名不区分大小写，{@code X}、{@code PI}、{@code SIN(0)} 均可正常解析；
 * 数字支持小数与科学计数法（如 {@code 1.5e-3}）。乘号不可省略，{@code 2x} 需要写成
 * {@code 2 * x}。</p>
 *
 * <p>乘方 {@code ^} 的优先级高于一元负号，因此 {@code -2^2} 等于 {@code -(2^2)} = {@code -4}。</p>
 *
 * <pre>{@code
 * Expression f = Expression.parse("x^2 - 4");
 * double y = f.evaluate(3.0);   // 5.0
 * }</pre>
 */
public final class Expression {

    /** 编译后的表达式树。 */
    private final DoubleUnaryOperator root;

    /** 原始表达式文本，用于报错与 {@link #toString()}。 */
    private final String source;

    private Expression(String source, DoubleUnaryOperator root) {
        this.source = source;
        this.root = root;
    }

    /**
     * 解析表达式文本。
     *
     * @param source 表达式文本，不能为 {@code null}
     * @return 解析后的表达式，可反复调用 {@link #evaluate(double)}
     * @throws IllegalArgumentException 当 {@code source} 为 {@code null} 或语法有误时
     */
    public static Expression parse(String source) {
        if (source == null) {
            throw new IllegalArgumentException("表达式不能为 null");
        }
        return new Expression(source, new Parser(source).parse());
    }

    /**
     * 计算表达式在给定 {@code x} 处的值。
     *
     * <p>结果可能是 {@code NaN} 或无穷大（例如 {@code ln(-1)}、{@code 1/x} 在 {@code x = 0} 处），
     * 调用方需要自行判断。</p>
     *
     * @param x 变量 {@code x} 的取值
     * @return 计算结果
     */
    public double evaluate(double x) {
        return root.applyAsDouble(x);
    }

    /**
     * 返回原始表达式文本。
     *
     * @return 解析时传入的表达式字符串
     */
    public String source() {
        return source;
    }

    @Override
    public String toString() {
        return source;
    }

    /**
     * 递归下降解析器：把表达式文本翻译成由 {@link DoubleUnaryOperator} 组成的表达式树。
     *
     * <p>文法（优先级由低到高）：</p>
     * <pre>
     * expression := term (('+' | '-') term)*
     * term       := unary (('*' | '/' | '%') unary)*
     * unary      := ('+' | '-') unary | power
     * power      := atom ('^' unary)?
     * atom       := number | identifier | identifier '(' arguments ')' | '(' expression ')'
     * </pre>
     */
    private static final class Parser {

        private final String source;
        private int pos;

        Parser(String source) {
            this.source = source;
        }

        DoubleUnaryOperator parse() {
            DoubleUnaryOperator result = expression();
            skipBlanks();
            if (pos < source.length()) {
                throw error("多余的字符 '" + source.charAt(pos) + "'");
            }
            return result;
        }

        private DoubleUnaryOperator expression() {
            DoubleUnaryOperator left = term();
            while (true) {
                skipBlanks();
                if (accept('+')) {
                    DoubleUnaryOperator a = left;
                    DoubleUnaryOperator b = term();
                    left = x -> a.applyAsDouble(x) + b.applyAsDouble(x);
                } else if (accept('-')) {
                    DoubleUnaryOperator a = left;
                    DoubleUnaryOperator b = term();
                    left = x -> a.applyAsDouble(x) - b.applyAsDouble(x);
                } else {
                    return left;
                }
            }
        }

        private DoubleUnaryOperator term() {
            DoubleUnaryOperator left = unary();
            while (true) {
                skipBlanks();
                if (accept('*')) {
                    DoubleUnaryOperator a = left;
                    DoubleUnaryOperator b = unary();
                    left = x -> a.applyAsDouble(x) * b.applyAsDouble(x);
                } else if (accept('/')) {
                    DoubleUnaryOperator a = left;
                    DoubleUnaryOperator b = unary();
                    left = x -> a.applyAsDouble(x) / b.applyAsDouble(x);
                } else if (accept('%')) {
                    DoubleUnaryOperator a = left;
                    DoubleUnaryOperator b = unary();
                    left = x -> a.applyAsDouble(x) % b.applyAsDouble(x);
                } else {
                    return left;
                }
            }
        }

        private DoubleUnaryOperator unary() {
            skipBlanks();
            if (accept('-')) {
                DoubleUnaryOperator operand = unary();
                return x -> -operand.applyAsDouble(x);
            }
            if (accept('+')) {
                return unary();
            }
            return power();
        }

        private DoubleUnaryOperator power() {
            DoubleUnaryOperator base = atom();
            skipBlanks();
            if (accept('^')) {
                DoubleUnaryOperator exponent = unary();
                return x -> Math.pow(base.applyAsDouble(x), exponent.applyAsDouble(x));
            }
            return base;
        }

        private DoubleUnaryOperator atom() {
            skipBlanks();
            if (pos >= source.length()) {
                throw error("表达式意外结束");
            }
            char c = source.charAt(pos);
            if (c == '(') {
                pos++;
                DoubleUnaryOperator inner = expression();
                skipBlanks();
                expect(')');
                return inner;
            }
            if (isDigit(c) || c == '.') {
                double value = number();
                return x -> value;
            }
            if (isLetter(c)) {
                String name = identifier();
                skipBlanks();
                if (pos < source.length() && source.charAt(pos) == '(') {
                    pos++;
                    return function(name, arguments());
                }
                return named(name);
            }
            throw error("无法识别的字符 '" + c + "'");
        }

        /** 解析圆括号内的参数列表，调用方已消费左括号。 */
        private List<DoubleUnaryOperator> arguments() {
            List<DoubleUnaryOperator> args = new ArrayList<DoubleUnaryOperator>();
            skipBlanks();
            if (accept(')')) {
                return args;
            }
            args.add(expression());
            skipBlanks();
            while (accept(',')) {
                args.add(expression());
                skipBlanks();
            }
            expect(')');
            return args;
        }

        private double number() {
            int start = pos;
            while (pos < source.length() && isDigit(source.charAt(pos))) {
                pos++;
            }
            if (pos < source.length() && source.charAt(pos) == '.') {
                pos++;
                while (pos < source.length() && isDigit(source.charAt(pos))) {
                    pos++;
                }
            }
            if (pos < source.length() && (source.charAt(pos) == 'e' || source.charAt(pos) == 'E')) {
                int mark = pos;
                pos++;
                if (pos < source.length() && (source.charAt(pos) == '+' || source.charAt(pos) == '-')) {
                    pos++;
                }
                if (pos < source.length() && isDigit(source.charAt(pos))) {
                    while (pos < source.length() && isDigit(source.charAt(pos))) {
                        pos++;
                    }
                } else {
                    // 指数后没有数字，说明这里的 e 不是科学计数法，回退交由后续逻辑处理
                    pos = mark;
                }
            }
            String text = source.substring(start, pos);
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                throw error("无法解析的数字 '" + text + "'");
            }
        }

        private String identifier() {
            int start = pos;
            while (pos < source.length() && isIdentifierPart(source.charAt(pos))) {
                pos++;
            }
            return source.substring(start, pos);
        }

        private DoubleUnaryOperator named(String name) {
            String key = name.toLowerCase(Locale.ROOT);
            if ("x".equals(key)) {
                return x -> x;
            }
            if ("pi".equals(key)) {
                double value = Math.PI;
                return x -> value;
            }
            if ("e".equals(key)) {
                double value = Math.E;
                return x -> value;
            }
            throw error("未知的标识符 '" + name + "'");
        }

        private DoubleUnaryOperator function(String name, List<DoubleUnaryOperator> args) {
            String key = name.toLowerCase(Locale.ROOT);
            int arity = args.size();
            DoubleUnaryOperator a = arity > 0 ? args.get(0) : null;
            DoubleUnaryOperator b = arity > 1 ? args.get(1) : null;
            switch (key) {
                case "sin":
                    require(key, arity, 1);
                    return x -> Math.sin(a.applyAsDouble(x));
                case "cos":
                    require(key, arity, 1);
                    return x -> Math.cos(a.applyAsDouble(x));
                case "tan":
                    require(key, arity, 1);
                    return x -> Math.tan(a.applyAsDouble(x));
                case "asin":
                    require(key, arity, 1);
                    return x -> Math.asin(a.applyAsDouble(x));
                case "acos":
                    require(key, arity, 1);
                    return x -> Math.acos(a.applyAsDouble(x));
                case "atan":
                    require(key, arity, 1);
                    return x -> Math.atan(a.applyAsDouble(x));
                case "sinh":
                    require(key, arity, 1);
                    return x -> Math.sinh(a.applyAsDouble(x));
                case "cosh":
                    require(key, arity, 1);
                    return x -> Math.cosh(a.applyAsDouble(x));
                case "tanh":
                    require(key, arity, 1);
                    return x -> Math.tanh(a.applyAsDouble(x));
                case "sqrt":
                    require(key, arity, 1);
                    return x -> Math.sqrt(a.applyAsDouble(x));
                case "cbrt":
                    require(key, arity, 1);
                    return x -> Math.cbrt(a.applyAsDouble(x));
                case "abs":
                    require(key, arity, 1);
                    return x -> Math.abs(a.applyAsDouble(x));
                case "exp":
                    require(key, arity, 1);
                    return x -> Math.exp(a.applyAsDouble(x));
                case "ln":
                    require(key, arity, 1);
                    return x -> Math.log(a.applyAsDouble(x));
                case "lg":
                    require(key, arity, 1);
                    return x -> Math.log10(a.applyAsDouble(x));
                case "floor":
                    require(key, arity, 1);
                    return x -> Math.floor(a.applyAsDouble(x));
                case "ceil":
                    require(key, arity, 1);
                    return x -> Math.ceil(a.applyAsDouble(x));
                case "round":
                    require(key, arity, 1);
                    return x -> (double) Math.round(a.applyAsDouble(x));
                case "signum":
                    require(key, arity, 1);
                    return x -> Math.signum(a.applyAsDouble(x));
                case "pow":
                    require(key, arity, 2);
                    return x -> Math.pow(a.applyAsDouble(x), b.applyAsDouble(x));
                case "atan2":
                    require(key, arity, 2);
                    return x -> Math.atan2(a.applyAsDouble(x), b.applyAsDouble(x));
                case "hypot":
                    require(key, arity, 2);
                    return x -> Math.hypot(a.applyAsDouble(x), b.applyAsDouble(x));
                case "min":
                    require(key, arity, 2);
                    return x -> Math.min(a.applyAsDouble(x), b.applyAsDouble(x));
                case "max":
                    require(key, arity, 2);
                    return x -> Math.max(a.applyAsDouble(x), b.applyAsDouble(x));
                default:
                    throw error("未知的函数 '" + name + "'");
            }
        }

        private void require(String name, int actual, int expected) {
            if (actual != expected) {
                throw error("函数 " + name + " 需要 " + expected + " 个参数，实际收到 " + actual + " 个");
            }
        }

        private boolean accept(char expected) {
            skipBlanks();
            if (pos < source.length() && source.charAt(pos) == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (pos >= source.length() || source.charAt(pos) != expected) {
                throw error("缺少 '" + expected + "'");
            }
            pos++;
        }

        private void skipBlanks() {
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
                pos++;
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(
                    "表达式解析失败（位置 " + pos + "）：" + message + "；表达式为 \"" + source + "\"");
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private static boolean isLetter(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
        }

        private static boolean isIdentifierPart(char c) {
            return isLetter(c) || isDigit(c);
        }
    }
}
