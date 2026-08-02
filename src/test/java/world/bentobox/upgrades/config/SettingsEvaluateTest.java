package world.bentobox.upgrades.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import world.bentobox.upgrades.config.FormulaParseException;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.config.Settings.Expression;

/**
 * Characterization tests for the expression parser in Settings.
 * These tests pin down the CURRENT behavior of Settings.parse() and Settings.evaluate()
 * to detect any changes during refactoring.
 *
 * @author tastybento
 */
class SettingsEvaluateTest {

    // ============= Basic Arithmetic and Operator Precedence =============

    @Test
    void testSimpleAddition() {
        Expression expr = Settings.parse("2 + 3", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testSimpleSubtraction() {
        Expression expr = Settings.parse("5 - 3", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testSimpleMultiplication() {
        Expression expr = Settings.parse("3 * 4", Map.of());
        assertEquals(12.0, expr.eval(), 0.0001);
    }

    @Test
    void testSimpleDivision() {
        Expression expr = Settings.parse("12 / 3", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    @Test
    void testMultiplicationBeforeAddition() {
        // Multiplication should have higher precedence than addition
        Expression expr = Settings.parse("2 + 3 * 4", Map.of());
        assertEquals(14.0, expr.eval(), 0.0001);
    }

    @Test
    void testDivisionBeforeSubtraction() {
        // Division should have higher precedence than subtraction
        Expression expr = Settings.parse("10 - 6 / 2", Map.of());
        assertEquals(7.0, expr.eval(), 0.0001);
    }

    @Test
    void testMultipleOperations() {
        Expression expr = Settings.parse("1 + 2 * 3 + 4", Map.of());
        assertEquals(11.0, expr.eval(), 0.0001);
    }

    @Test
    void testLeftToRightForSamePrecedence() {
        // Addition and subtraction are left-associative
        Expression expr = Settings.parse("10 - 3 - 2", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testLeftToRightDivisionAndMultiplication() {
        // Multiplication and division are left-associative
        Expression expr = Settings.parse("12 / 3 * 2", Map.of());
        assertEquals(8.0, expr.eval(), 0.0001);
    }

    // ============= Parentheses and Nesting =============

    @Test
    void testSimpleParentheses() {
        Expression expr = Settings.parse("(2 + 3) * 4", Map.of());
        assertEquals(20.0, expr.eval(), 0.0001);
    }

    @Test
    void testNestedParentheses() {
        Expression expr = Settings.parse("((2 + 3) * 4) + 1", Map.of());
        assertEquals(21.0, expr.eval(), 0.0001);
    }

    @Test
    void testParenthesesWithSubtraction() {
        Expression expr = Settings.parse("(10 - 3) * 2", Map.of());
        assertEquals(14.0, expr.eval(), 0.0001);
    }

    @Test
    void testParenthesesWithDivision() {
        Expression expr = Settings.parse("20 / (2 + 3)", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    @Test
    void testDeeplyNestedParentheses() {
        Expression expr = Settings.parse("(((2 + 3)))", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    // ============= Exponentiation =============

    @Test
    void testSimpleExponentiation() {
        Expression expr = Settings.parse("2 ^ 3", Map.of());
        assertEquals(8.0, expr.eval(), 0.0001);
    }

    @Test
    void testSquaring() {
        Expression expr = Settings.parse("10 ^ 2", Map.of());
        assertEquals(100.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationRightAssociativity() {
        // 2 ^ 3 ^ 2 should be 2^(3^2) = 2^9 = 512, not (2^3)^2 = 8^2 = 64
        Expression expr = Settings.parse("2 ^ 3 ^ 2", Map.of());
        assertEquals(512.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationWithNegativeVariable() {
        // Exponentiation with negative value via variable
        Expression expr = Settings.parse("[x] ^ 3", Map.of("[x]", -2.0));
        assertEquals(-8.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationWithDecimal() {
        Expression expr = Settings.parse("4 ^ 0.5", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationInExpression() {
        Expression expr = Settings.parse("2 ^ 3 + 1", Map.of());
        assertEquals(9.0, expr.eval(), 0.0001);
    }

    // ============= Functions =============

    @Test
    void testSqrtFunction() {
        Expression expr = Settings.parse("sqrt(4)", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testSqrtOfNine() {
        Expression expr = Settings.parse("sqrt(9)", Map.of());
        assertEquals(3.0, expr.eval(), 0.0001);
    }

    @Test
    void testSinZero() {
        // sin(0 degrees) = 0
        Expression expr = Settings.parse("sin(0)", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testCosZero() {
        // cos(0 degrees) = 1
        Expression expr = Settings.parse("cos(0)", Map.of());
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testTan45() {
        // tan(45 degrees) = 1
        Expression expr = Settings.parse("tan(45)", Map.of());
        assertEquals(1.0, expr.eval(), 0.001); // Slightly wider tolerance for trig
    }

    @Test
    void testSin90() {
        // sin(90 degrees) = 1
        Expression expr = Settings.parse("sin(90)", Map.of());
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testFunctionWithExpression() {
        Expression expr = Settings.parse("sqrt(4 + 5)", Map.of());
        assertEquals(3.0, expr.eval(), 0.0001);
    }

    @Test
    void testSqrtWithMultiplication() {
        Expression expr = Settings.parse("sqrt(4) * 2", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    @Test
    void testNestedFunctions() {
        Expression expr = Settings.parse("sqrt(sqrt(16))", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    // ============= Variable Substitution =============

    @Test
    void testSimpleVariableSubstitution() {
        Expression expr = Settings.parse("[level]", Map.of("[level]", 5.0));
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableInExpression() {
        Expression expr = Settings.parse("100 * [level]", Map.of("[level]", 5.0));
        assertEquals(500.0, expr.eval(), 0.0001);
    }

    @Test
    void testMultipleVariables() {
        Expression expr = Settings.parse("[level] + [islandLevel]",
                Map.of("[level]", 3.0, "[islandLevel]", 7.0));
        assertEquals(10.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableWithMultiplication() {
        Expression expr = Settings.parse("2 * [level] * 3", Map.of("[level]", 5.0));
        assertEquals(30.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableWithExponentiation() {
        Expression expr = Settings.parse("[level] ^ 2", Map.of("[level]", 3.0));
        assertEquals(9.0, expr.eval(), 0.0001);
    }

    @Test
    void testProjectStyleVariableNumberPlayer() {
        Expression expr = Settings.parse("100 + [numberPlayer]", Map.of("[numberPlayer]", 5.0));
        assertEquals(105.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableComplexExpression() {
        Expression expr = Settings.parse("[level] * 10 + [islandLevel] * 5",
                Map.of("[level]", 2.0, "[islandLevel]", 3.0));
        assertEquals(35.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableWithFunction() {
        Expression expr = Settings.parse("sqrt([level])", Map.of("[level]", 16.0));
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableInParentheses() {
        Expression expr = Settings.parse("([level] + 3) * 2", Map.of("[level]", 5.0));
        assertEquals(16.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableLongerName() {
        Expression expr = Settings.parse("[levelMax]", Map.of("[levelMax]", 10.0));
        assertEquals(10.0, expr.eval(), 0.0001);
    }

    // ============= Whitespace Handling =============

    @Test
    void testWhitespaceAroundAddition() {
        Expression expr = Settings.parse("  2   +   3  ", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testWhitespaceAroundMultiplication() {
        Expression expr = Settings.parse("3 * 4", Map.of());
        assertEquals(12.0, expr.eval(), 0.0001);
    }

    @Test
    void testWhitespaceInsideParentheses() {
        Expression expr = Settings.parse("( 2 + 3 ) * 4", Map.of());
        assertEquals(20.0, expr.eval(), 0.0001);
    }

    @Test
    void testWhitespaceBeforeFunction() {
        Expression expr = Settings.parse("  sqrt(4)", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testLeadingWhitespace() {
        Expression expr = Settings.parse("   5 * 2", Map.of());
        assertEquals(10.0, expr.eval(), 0.0001);
    }

    // ============= Decimal Numbers =============

    @Test
    void testSimpleDecimal() {
        Expression expr = Settings.parse("1.5 + 2.5", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    @Test
    void testDecimalMultiplication() {
        Expression expr = Settings.parse("0.5 * 4", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testDecimalStartingWithDot() {
        Expression expr = Settings.parse(".5 + .5", Map.of());
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testDecimalDivision() {
        Expression expr = Settings.parse("5.0 / 2.0", Map.of());
        assertEquals(2.5, expr.eval(), 0.0001);
    }

    @Test
    void testMultipleDecimalPlaces() {
        Expression expr = Settings.parse("1.234 + 2.766", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    // ============= Notes on Unary Operators =============
    // The unary minus implementation has a bug: it returns a lambda without parsing the operand,
    // leaving unparsed content which causes parse() to throw "Unexpected: X" error.
    // Therefore, bare unary minus does NOT work in this parser for any operand type.
    // Workaround: use subtraction instead (e.g., "0 - 5" instead of "-5")

    @Test
    void testSubtractionFromZeroForNegative() {
        Expression expr = Settings.parse("0 - 5", Map.of());
        assertEquals(-5.0, expr.eval(), 0.0001);
    }

    // ============= Division by Zero and Special Cases =============

    @Test
    void testDivisionByZero() {
        Expression expr = Settings.parse("5 / 0", Map.of());
        double result = expr.eval();
        assertTrue(Double.isInfinite(result) && result > 0, "Expected positive infinity");
    }

    @Test
    void testNegativeDivisionByZero() {
        // Use variable for negative value since bare -5 doesn't parse
        Expression expr = Settings.parse("[x] / 0", Map.of("[x]", -5.0));
        double result = expr.eval();
        assertTrue(Double.isInfinite(result) && result < 0, "Expected negative infinity");
    }

    @Test
    void testZeroToZeroPower() {
        Expression expr = Settings.parse("0 ^ 0", Map.of());
        // Java Math.pow(0.0, 0.0) returns 1.0
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testZeroMultipliedByAnything() {
        Expression expr = Settings.parse("0 * 999999", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    // ============= Malformed Input =============

    @Test
    void testEmptyString() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("", Map.of());
        });
        // Parser will throw when trying to parse with ch == -1
        assertNotNull(ex.getMessage());
    }

    @Test
    void testUnbalancedLeftParen() {
        // Parser silently accepts missing closing paren - it doesn't throw
        Expression expr = Settings.parse("(2 + 3", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testUnbalancedRightParen() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("2 + 3)", Map.of());
        });
        assertTrue(ex.getMessage().contains("Unexpected"));
    }

    @Test
    void testUnknownCharacter() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("2 + 3 & 4", Map.of());
        });
        assertTrue(ex.getMessage().contains("Unexpected"));
    }

    @Test
    void testUnknownCharacterAtStart() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("@ 2 + 3", Map.of());
        });
        assertTrue(ex.getMessage().contains("Unexpected"));
    }

    @Test
    void testMissingOperand() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("2 +", Map.of());
        });
        assertNotNull(ex.getMessage());
    }

    // ============= Unknown Variables and Functions =============

    @Test
    void testUnknownVariableThrowsNPE() {
        Expression expr = Settings.parse("[unknown]", Map.of());
        // The parser treats it as a variable and calls variables.get("[unknown]")
        // which returns null. Auto-unboxing null to double throws NPE.
        assertThrows(NullPointerException.class, expr::eval);
    }

    @Test
    void testUnknownFunctionTreatedAsVariable() {
        // "unknown" is not in the funct list, so it's treated as a variable
        Expression expr = Settings.parse("unknown", Map.of("unknown", 42.0));
        assertEquals(42.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableSubstringPrefix() {
        // [level] and [levelMax] - ensure correct matching
        Map<String, Double> vars = Map.of("[level]", 5.0, "[levelMax]", 20.0);
        Expression expr1 = Settings.parse("[level]", vars);
        Expression expr2 = Settings.parse("[levelMax]", vars);
        assertEquals(5.0, expr1.eval(), 0.0001);
        assertEquals(20.0, expr2.eval(), 0.0001);
    }

    // ============= Complex Real-World Scenarios =============

    @Test
    void testFormulasFromProjectConfig() {
        // Example: 100*[level] (range upgrade cost)
        Expression expr = Settings.parse("100*[level]", Map.of("[level]", 3.0));
        assertEquals(300.0, expr.eval(), 0.0001);
    }

    @Test
    void testMultiplierWithPlayerCount() {
        // Example: 500 + [numberPlayer] * 50
        Expression expr = Settings.parse("500 + [numberPlayer] * 50",
                Map.of("[numberPlayer]", 4.0));
        assertEquals(700.0, expr.eval(), 0.0001);
    }

    @Test
    void testComplexFormula() {
        // Example with multiple operations and variables
        Expression expr = Settings.parse("([level] ^ 2) * 100 + [islandLevel] * 10",
                Map.of("[level]", 2.0, "[islandLevel]", 5.0));
        assertEquals(450.0, expr.eval(), 0.0001);
    }

    @Test
    void testFormulaWithSqrt() {
        Expression expr = Settings.parse("100 * sqrt([level])",
                Map.of("[level]", 4.0));
        assertEquals(200.0, expr.eval(), 0.0001);
    }

    @Test
    void testUsingEvaluateMethod() {
        double result = Settings.evaluate("2 + 3 * 4", Map.of());
        assertEquals(14.0, result, 0.0001);
    }

    @Test
    void testUsingEvaluateMethodWithVariables() {
        double result = Settings.evaluate("100 * [level]", Map.of("[level]", 5.0));
        assertEquals(500.0, result, 0.0001);
    }

    // ============= Edge Cases with Variable Names =============

    @Test
    void testVariableWithBracketsAtStart() {
        Expression expr = Settings.parse("[a]", Map.of("[a]", 1.0));
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableWithBracketsAtEnd() {
        Expression expr = Settings.parse("[z]", Map.of("[z]", 99.0));
        assertEquals(99.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableWithMultipleBracketsAndLetters() {
        // Variable names can only contain letters and brackets, not digits
        // [myLevel] will be parsed as a variable, "123]" will cause parse error
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("[myLevel123]", Map.of("[myLevel123]", 42.0));
        });
        assertTrue(ex.getMessage().contains("Unexpected"));
    }

    // ============= Additional Operator Precedence Tests =============

    @Test
    void testExponentiationHigherThanMultiplication() {
        // 2 * 3 ^ 2 should be 2 * 9 = 18, not (2*3) ^ 2 = 36
        Expression expr = Settings.parse("2 * 3 ^ 2", Map.of());
        assertEquals(18.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationHigherThanDivision() {
        // 8 / 2 ^ 2 should be 8 / 4 = 2, not (8/2) ^ 2 = 16
        Expression expr = Settings.parse("8 / 2 ^ 2", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testLongChainOfOperations() {
        Expression expr = Settings.parse("1 + 2 - 3 + 4 - 5", Map.of());
        assertEquals(-1.0, expr.eval(), 0.0001);
    }

    // ============= More Complex Parentheses Tests =============

    @Test
    void testParenthesesChangePrecedence() {
        Expression expr1 = Settings.parse("2 + 3 * 4", Map.of());
        Expression expr2 = Settings.parse("(2 + 3) * 4", Map.of());
        assertEquals(14.0, expr1.eval(), 0.0001);
        assertEquals(20.0, expr2.eval(), 0.0001);
    }

    @Test
    void testParenthesesReverseSubtraction() {
        // Cannot use unary minus, use subtraction instead for negative results
        Expression expr = Settings.parse("0 - (5 + 3) * 2", Map.of());
        assertEquals(-16.0, expr.eval(), 0.0001);
    }

    @Test
    void testFunctionPrecedenceWithOtherOperators() {
        // sqrt(4) * 3 should be 2 * 3 = 6
        Expression expr = Settings.parse("sqrt(4) * 3", Map.of());
        assertEquals(6.0, expr.eval(), 0.0001);
    }

    @Test
    void testFunctionWithComplexArgument() {
        Expression expr = Settings.parse("sqrt(2 * 8)", Map.of());
        assertEquals(4.0, expr.eval(), 0.0001);
    }

    // ============= Trailing/Leading Operators =============

    @Test
    void testExtraWhitespaceOnly() {
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("   ", Map.of());
        });
        assertNotNull(ex.getMessage());
    }

    // ============= Additional Edge Cases =============

    @Test
    void testFunctionCaseSensitivity() {
        // Function names are case-sensitive. "Sqrt" is not recognized as sqrt.
        // It will be treated as a variable instead.
        Expression expr = Settings.parse("Sqrt", Map.of("Sqrt", 99.0));
        assertEquals(99.0, expr.eval(), 0.0001);
    }

    @Test
    void testUnknownFunctionNameAsVariable() {
        // "power" is not in the function list, so it's treated as a variable
        Expression expr = Settings.parse("power", Map.of("power", 42.0));
        assertEquals(42.0, expr.eval(), 0.0001);
    }

    @Test
    void testLargeNumbers() {
        Expression expr = Settings.parse("1000000 + 2000000", Map.of());
        assertEquals(3000000.0, expr.eval(), 0.1);
    }

    @Test
    void testVerySmallNumbers() {
        Expression expr = Settings.parse("0.000001 + 0.000002", Map.of());
        assertEquals(0.000003, expr.eval(), 0.0000001);
    }

    @Test
    void testLargeExponent() {
        Expression expr = Settings.parse("2 ^ 10", Map.of());
        assertEquals(1024.0, expr.eval(), 0.0001);
    }

    @Test
    void testZeroRaisedToPositivePower() {
        Expression expr = Settings.parse("0 ^ 5", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testOneRaisedToAnyPower() {
        Expression expr = Settings.parse("1 ^ 999", Map.of());
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testVariableInComplexExpression() {
        // Demonstrates that variables work anywhere in expressions
        Expression expr = Settings.parse("[a] * [b] + [c] / [d]",
                Map.of("[a]", 2.0, "[b]", 3.0, "[c]", 10.0, "[d]", 2.0));
        assertEquals(11.0, expr.eval(), 0.0001);
    }

    @Test
    void testMultipleVariableReferences() {
        // Same variable referenced multiple times
        Expression expr = Settings.parse("[x] + [x] * [x]",
                Map.of("[x]", 3.0));
        assertEquals(12.0, expr.eval(), 0.0001); // 3 + 3*3 = 3 + 9 = 12
    }

    @Test
    void testSqrtOfZero() {
        Expression expr = Settings.parse("sqrt(0)", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testSqrtOfOne() {
        Expression expr = Settings.parse("sqrt(1)", Map.of());
        assertEquals(1.0, expr.eval(), 0.0001);
    }

    @Test
    void testSqrtOfDecimal() {
        Expression expr = Settings.parse("sqrt(0.25)", Map.of());
        assertEquals(0.5, expr.eval(), 0.0001);
    }

    @Test
    void testMultiplicationByZero() {
        Expression expr = Settings.parse("99999 * 0", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testDivisionOfZeroByNumber() {
        Expression expr = Settings.parse("0 / 5", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testParenthesesChangingPrecedenceMultiple() {
        Expression expr = Settings.parse("2 * 3 + 4 * 5", Map.of());
        Expression expr2 = Settings.parse("(2 * 3 + 4) * 5", Map.of());
        assertEquals(26.0, expr.eval(), 0.0001);
        assertEquals(50.0, expr2.eval(), 0.0001);
    }

    @Test
    void testFunctionWithDecimalResult() {
        Expression expr = Settings.parse("sqrt(2)", Map.of());
        double result = expr.eval();
        assertEquals(Math.sqrt(2), result, 0.0001);
    }

    @Test
    void testCosine90Degrees() {
        // cos(90 degrees) should be approximately 0
        Expression expr = Settings.parse("cos(90)", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testTangent0Degrees() {
        // tan(0 degrees) should be 0
        Expression expr = Settings.parse("tan(0)", Map.of());
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testFunctionWithVariableArgument() {
        Expression expr = Settings.parse("sin([angle])", Map.of("[angle]", 0.0));
        assertEquals(0.0, expr.eval(), 0.0001);
    }

    @Test
    void testComplexFormulaWithAllFeatures() {
        // Combines multiple features: parentheses, functions, variables, operators
        Expression expr = Settings.parse("sqrt([base] * [base] + [height] * [height])",
                Map.of("[base]", 3.0, "[height]", 4.0));
        // sqrt(3*3 + 4*4) = sqrt(9 + 16) = sqrt(25) = 5
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testDivisionPrecedenceOverAddition() {
        Expression expr = Settings.parse("10 + 20 / 5", Map.of());
        assertEquals(14.0, expr.eval(), 0.0001); // 10 + 4, not (10 + 20) / 5
    }

    @Test
    void testSubtractionLeftAssociative() {
        Expression expr = Settings.parse("20 - 5 - 3", Map.of());
        assertEquals(12.0, expr.eval(), 0.0001); // (20 - 5) - 3 = 15 - 3 = 12
    }

    @Test
    void testDivisionLeftAssociative() {
        Expression expr = Settings.parse("16 / 4 / 2", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001); // (16 / 4) / 2 = 4 / 2 = 2
    }

    @Test
    void testExponentiationAllowsDecimals() {
        Expression expr = Settings.parse("9 ^ 0.5", Map.of());
        assertEquals(3.0, expr.eval(), 0.0001);
    }

    @Test
    void testUnknownVariableInExpressionThrowsNPE() {
        Expression expr = Settings.parse("5 + [unknown]", Map.of());
        // The expression parses fine, but eval() throws NPE when trying to unbox null
        assertThrows(NullPointerException.class, expr::eval);
    }

    @Test
    void testWhitespaceBeforeFunctionArgument() {
        Expression expr = Settings.parse("sqrt( 4 )", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testDoubleDotInNumber() {
        // "1.2.3" would parse all dots as part of the number token,
        // then Double.parseDouble("1.2.3") throws a NumberFormatException
        assertThrows(NumberFormatException.class, () -> {
            Settings.parse("1.2.3", Map.of()).eval();
        });
    }

    @Test
    void testBracketNotInVariableName() {
        // A bracket outside a variable context should cause an error
        FormulaParseException ex = assertThrows(FormulaParseException.class, () -> {
            Settings.parse("] 2", Map.of());
        });
        assertTrue(ex.getMessage().contains("Unexpected"));
    }

    @Test
    void testMultipleParenthesesLevels() {
        Expression expr = Settings.parse("((((2 + 3))))", Map.of());
        assertEquals(5.0, expr.eval(), 0.0001);
    }

    @Test
    void testFunctionWithExpressionInsideParentheses() {
        Expression expr = Settings.parse("sqrt((2 + 2))", Map.of());
        assertEquals(2.0, expr.eval(), 0.0001);
    }

    @Test
    void testProjectConfigExampleRange() {
        // Real example from config: cost formula
        Expression expr = Settings.parse("100 * [level]", Map.of("[level]", 5.0));
        assertEquals(500.0, expr.eval(), 0.0001);
    }

    @Test
    void testProjectConfigExampleWithIslandLevel() {
        // Real example using both level and islandLevel
        Expression expr = Settings.parse("[level] * 10 + [islandLevel] * 5",
                Map.of("[level]", 2.0, "[islandLevel]", 10.0));
        assertEquals(70.0, expr.eval(), 0.0001);
    }

    @Test
    void testProjectConfigExampleWithNumberPlayer() {
        // Real example using numberPlayer
        Expression expr = Settings.parse("500 + [numberPlayer] * 50",
                Map.of("[numberPlayer]", 3.0));
        assertEquals(650.0, expr.eval(), 0.0001);
    }

    @Test
    void testNegativeNumberViaSubtraction() {
        // Demonstrates workaround for lack of unary minus
        Expression expr = Settings.parse("0 - 10", Map.of());
        assertEquals(-10.0, expr.eval(), 0.0001);
    }

    @Test
    void testExponentiationRightAssociativityProof() {
        // 2^3^2 should equal 512 (2^9), not 64 ((2^3)^2)
        Expression expr = Settings.parse("2 ^ 3 ^ 2", Map.of());
        assertEquals(512.0, expr.eval(), 0.0001);
    }

    @Test
    void testNegativeNumberViaVariable() {
        // Variables can hold negative values
        Expression expr = Settings.parse("[value] * 2", Map.of("[value]", -5.0));
        assertEquals(-10.0, expr.eval(), 0.0001);
    }
}
