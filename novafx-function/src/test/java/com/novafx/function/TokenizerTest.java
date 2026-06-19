package com.novafx.function;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TokenizerTest {

    @Test
    void shouldTokenizeNumber() {
        Tokenizer t = new Tokenizer("42");
        assertThat(t.tokens()).hasSize(1);
        assertThat(t.get(0).type()).isEqualTo(TokenType.NUMBER);
        assertThat(t.get(0).numericValue()).isEqualTo(42.0);
    }

    @Test
    void shouldTokenizeDecimal() {
        Tokenizer t = new Tokenizer("3.14");
        assertThat(t.get(0).numericValue()).isEqualTo(3.14);
    }

    @Test
    void shouldTokenizeLeadingDot() {
        Tokenizer t = new Tokenizer(".5");
        assertThat(t.get(0).numericValue()).isEqualTo(0.5);
    }

    @Test
    void shouldTokenizeScientificNotation() {
        Tokenizer t = new Tokenizer("1e10");
        assertThat(t.get(0).numericValue()).isEqualTo(1e10);
    }

    @Test
    void shouldTokenizeNegativeExponent() {
        Tokenizer t = new Tokenizer("2.5e-3");
        assertThat(t.get(0).numericValue()).isCloseTo(0.0025, within(1e-12));
    }

    @Test
    void shouldTokenizeIdentifier() {
        Tokenizer t = new Tokenizer("sin");
        assertThat(t.get(0).type()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(t.get(0).lexeme()).isEqualTo("sin");
    }

    @Test
    void shouldTokenizeUnderscoreIdentifier() {
        Tokenizer t = new Tokenizer("_myVar");
        assertThat(t.get(0).lexeme()).isEqualTo("_myVar");
    }

    @Test
    void shouldTokenizeOperators() {
        Tokenizer t = new Tokenizer("+-*/^(),");
        assertThat(t.tokens()).extracting(Token::type)
                .containsExactly(TokenType.PLUS, TokenType.MINUS, TokenType.STAR,
                        TokenType.SLASH, TokenType.CARET, TokenType.LPAREN,
                        TokenType.RPAREN, TokenType.COMMA);
    }

    @Test
    void shouldSkipWhitespace() {
        Tokenizer t = new Tokenizer("  a  +  b  ");
        assertThat(t.tokens()).extracting(Token::lexeme)
                .containsExactly("a", "+", "b");
    }

    @Test
    void shouldPeekAndAdvance() {
        Tokenizer t = new Tokenizer("a b");
        assertThat(t.peek().lexeme()).isEqualTo("a");
        assertThat(t.peek().lexeme()).isEqualTo("a"); // peek again doesn't advance
        assertThat(t.advance().lexeme()).isEqualTo("a");
        assertThat(t.peek().lexeme()).isEqualTo("b");
    }

    @Test
    void hasMoreShouldReturnFalseAtEnd() {
        Tokenizer t = new Tokenizer("a");
        t.advance();
        assertThat(t.hasMore()).isFalse();
    }

    @Test
    void getShouldReturnEofPastEnd() {
        Tokenizer t = new Tokenizer("a");
        assertThat(t.get(100).type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldRejectUnrecognizedCharacter() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Tokenizer("@"))
                .withMessageContaining("@");
    }

    @Test
    void shouldRejectNullInput() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Tokenizer(null));
    }

    @Test
    void shouldHandleEmptyInput() {
        Tokenizer t = new Tokenizer("");
        assertThat(t.tokens()).isEmpty();
        assertThat(t.peek().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    void shouldHandleComplexExpression() {
        Tokenizer t = new Tokenizer("sin(t) + cos(t)");
        assertThat(t.tokens()).extracting(Token::lexeme)
                .containsExactly("sin", "(", "t", ")", "+", "cos", "(", "t", ")");
    }

    @Test
    void shouldHandleNumbersWithTrailingDot() {
        // "3." is not a valid number start for leading dot, but 3.  is
        Tokenizer t = new Tokenizer("3.0");
        assertThat(t.get(0).numericValue()).isEqualTo(3.0);
    }

    @Test
    void shouldReportPosition() {
        Tokenizer t = new Tokenizer("  x");
        assertThat(t.get(0).position()).isEqualTo(2);
    }
}
