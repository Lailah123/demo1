package com.zjx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link Expression} 的单元测试。
 */
class ExpressionTest {

    private static final double DELTA = 1e-9;

    @Test
    void parsesArithmeticWithOperatorPrecedence() {
        assertEquals(7.0, evaluate("1 + 2 * 3"), DELTA);
        assertEquals(9.0, evaluate("(1 + 2) * 3"), DELTA);
        assertEquals(1.0, evaluate("1 + 2 - 2"), DELTA);
        assertEquals(2.5, evaluate("10 / 4"), DELTA);
        assertEquals(1.0, evaluate("7 % 3"), DELTA);
    }

    @Test
    void powerIsRightAssociativeAndBindsTighterThanUnaryMinus() {
        assertEquals(512.0, evaluate("2^3^2"), DELTA);
        assertEquals(-4.0, evaluate("-2^2"), DELTA);
        assertEquals(0.25, evaluate("2^-2"), DELTA);
        assertEquals(4.0, evaluate("(-2)^2"), DELTA);
    }

    @Test
    void handlesUnarySigns() {
        assertEquals(-3.0, evaluate("-x", 3.0), DELTA);
        assertEquals(3.0, evaluate("--x", 3.0), DELTA);
        assertEquals(3.0, evaluate("+x", 3.0), DELTA);
        assertEquals(-6.0, evaluate("-(x + 3)", 3.0), DELTA);
    }

    @Test
    void substitutesVariableX() {
        assertEquals(5.0, evaluate("x^2 - 4", 3.0), DELTA);
        assertEquals(0.0, evaluate("x - 7", 7.0), DELTA);
        assertEquals(-2.5, evaluate("x", -2.5), DELTA);
    }

    @Test
    void resolvesConstants() {
        assertEquals(Math.PI, evaluate("pi"), DELTA);
        assertEquals(Math.E, evaluate("e"), DELTA);
        assertEquals(2 * Math.PI, evaluate("2 * pi"), DELTA);
    }

    @Test
    void evaluatesOneArgumentFunctions() {
        assertEquals(0.0, evaluate("sin(0)"), DELTA);
        assertEquals(1.0, evaluate("cos(0)"), DELTA);
        assertEquals(0.0, evaluate("tan(0)"), DELTA);
        assertEquals(Math.PI / 2, evaluate("asin(1)"), DELTA);
        assertEquals(0.0, evaluate("acos(1)"), DELTA);
        assertEquals(Math.PI / 4, evaluate("atan(1)"), DELTA);
        assertEquals(0.0, evaluate("sinh(0)"), DELTA);
        assertEquals(1.0, evaluate("cosh(0)"), DELTA);
        assertEquals(0.0, evaluate("tanh(0)"), DELTA);
        assertEquals(3.0, evaluate("sqrt(9)"), DELTA);
        assertEquals(3.0, evaluate("cbrt(27)"), DELTA);
        assertEquals(5.0, evaluate("abs(-5)"), DELTA);
        assertEquals(1.0, evaluate("exp(0)"), DELTA);
        assertEquals(1.0, evaluate("ln(e)"), DELTA);
        assertEquals(3.0, evaluate("lg(1000)"), DELTA);
        assertEquals(2.0, evaluate("floor(2.7)"), DELTA);
        assertEquals(3.0, evaluate("ceil(2.1)"), DELTA);
        assertEquals(3.0, evaluate("round(2.5)"), DELTA);
        assertEquals(-1.0, evaluate("signum(-3)"), DELTA);
    }

    @Test
    void evaluatesTwoArgumentFunctions() {
        assertEquals(1024.0, evaluate("pow(2, 10)"), DELTA);
        assertEquals(Math.PI / 4, evaluate("atan2(1, 1)"), DELTA);
        assertEquals(5.0, evaluate("hypot(3, 4)"), DELTA);
        assertEquals(3.0, evaluate("min(3, 7)"), DELTA);
        assertEquals(7.0, evaluate("max(3, 7)"), DELTA);
        assertEquals(5.0, evaluate("max(3, min(9, 5))"), DELTA);
    }

    @Test
    void composesFunctionsWithExpressions() {
        assertEquals(6.0, evaluate("sqrt(16) + lg(100)"), DELTA);
        assertEquals(1.0, evaluate("sin(x)^2 + cos(x)^2", 1.234), DELTA);
        assertEquals(-9.0, evaluate("-x^2", 3.0), DELTA);
    }

    @Test
    void acceptsScientificNotationAndLeadingDot() {
        assertEquals(1000.0, evaluate("1e3"), DELTA);
        assertEquals(0.025, evaluate("2.5e-2"), DELTA);
        assertEquals(1234.0, evaluate("1.234e3"), DELTA);
        assertEquals(0.5, evaluate(".5"), DELTA);
    }

    @Test
    void ignoresWhitespace() {
        assertEquals(4.0, evaluate("  x  +  1  ", 3.0), DELTA);
        assertEquals(6.0, evaluate("max( 1 , 6 )"), DELTA);
    }

    @Test
    void identifierAndFunctionNamesAreCaseInsensitive() {
        assertEquals(3.0, evaluate("X", 3.0), DELTA);
        assertEquals(Math.PI, evaluate("PI"), DELTA);
        assertEquals(0.0, evaluate("SIN(0)"), DELTA);
        assertEquals(2.0, evaluate("Sqrt(4)"), DELTA);
    }

    @Test
    void parsedExpressionCanBeReused() {
        Expression square = Expression.parse("x^2");

        assertEquals(0.0, square.evaluate(0.0), DELTA);
        assertEquals(4.0, square.evaluate(2.0), DELTA);
        assertEquals(9.0, square.evaluate(-3.0), DELTA);
    }

    @Test
    void exposesSourceText() {
        Expression expression = Expression.parse("x + 1");

        assertEquals("x + 1", expression.source());
        assertEquals("x + 1", expression.toString());
    }

    @Test
    void propagatesNonFiniteResultsInsteadOfThrowing() {
        assertTrue(Double.isNaN(evaluate("ln(x)", -1.0)));
        assertTrue(Double.isNaN(evaluate("sqrt(x)", -1.0)));
        assertTrue(Double.isInfinite(evaluate("1 / x", 0.0)));
    }

    @Test
    void rejectsMalformedExpressions() {
        assertThrows(IllegalArgumentException.class, () -> Expression.parse(null));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("1 +"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("(1 + 2"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("1 + 2)"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("* 2"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("2 & 3"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("."));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("2x"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("y + 1"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("sin x"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("sin(1, 2)"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("sin()"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("pow(2)"));
        assertThrows(IllegalArgumentException.class, () -> Expression.parse("nope(2)"));
    }

    @Test
    void errorMessageQuotesTheOffendingExpression() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Expression.parse("1 + y"));

        assertTrue(error.getMessage().contains("1 + y"), error.getMessage());
    }

    private static double evaluate(String source) {
        return Expression.parse(source).evaluate(0.0);
    }

    private static double evaluate(String source, double x) {
        return Expression.parse(source).evaluate(x);
    }
}
