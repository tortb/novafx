package com.novafx.function;

/**
 * A single token produced by the {@link Tokenizer}.
 *
 * @param type         the token type
 * @param lexeme       the raw text matched for this token (for error messages)
 * @param numericValue for {@link TokenType#NUMBER} tokens, the parsed value; 0 otherwise
 * @param position     character index in the original input (for error reporting)
 */
public record Token(TokenType type, String lexeme, double numericValue, int position) {

    /** Convenience constructor for non-numeric tokens. */
    public Token(TokenType type, String lexeme, int position) {
        this(type, lexeme, 0.0, position);
    }
}
