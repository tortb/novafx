package com.novafx.function;

/**
 * All token types recognized by the expression tokenizer.
 */
public enum TokenType {

    /** A numeric literal (e.g. {@code 42}, {@code 3.14}, {@code .5}). */
    NUMBER,

    /** An identifier: variable name, function name, or constant (e.g. {@code t}, {@code sin}, {@code PI}). */
    IDENTIFIER,

    /** {@code +} */
    PLUS,

    /** {@code -} */
    MINUS,

    /** {@code *} */
    STAR,

    /** {@code /} */
    SLASH,

    /** {@code ^} */
    CARET,

    /** {@code (} */
    LPAREN,

    /** {@code )} */
    RPAREN,

    /** {@code ,} */
    COMMA,

    /** End of input. */
    EOF
}
