package com.novafx.function;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a mathematical expression string into a sequence of {@link Token}s.
 * <p>
 * Supports numbers (integer, decimal, scientific notation), identifiers
 * (letters, digits, underscores), and single-character operators.
 * Tracks character positions for error reporting.
 */
public final class Tokenizer {

    private final String input;
    private final List<Token> tokens;
    private int pos;

    /**
     * Creates a tokenizer for the given input string.
     *
     * @param input the expression to tokenize; must not be null
     */
    public Tokenizer(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        this.input = input;
        this.tokens = new ArrayList<>();
        this.pos = 0;
        tokenize();
    }

    /**
     * Returns an immutable view of all tokens produced.
     *
     * @return the token list
     */
    public List<Token> tokens() {
        return List.copyOf(tokens);
    }

    /**
     * Returns the token at the given index, or the EOF token if beyond the end.
     *
     * @param index the token index (0-based)
     * @return the token at that index
     */
    public Token get(int index) {
        if (index >= 0 && index < tokens.size()) {
            return tokens.get(index);
        }
        return new Token(TokenType.EOF, "", input.length());
    }

    /**
     * Returns the current (peeked) token without advancing.
     */
    public Token peek() {
        return get(pos);
    }

    /**
     * Returns the current token and advances to the next.
     */
    public Token advance() {
        Token t = get(pos);
        pos++;
        return t;
    }

    /**
     * Returns true if there are more tokens (the current token is not EOF).
     */
    public boolean hasMore() {
        return pos < tokens.size();
    }

    // ---------------------------------------------------------------
    // Tokenization logic
    // ---------------------------------------------------------------

    private void tokenize() {
        int i = 0;
        int len = input.length();

        while (i < len) {
            char c = input.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Number: starts with digit or decimal point followed by digit
            if (Character.isDigit(c) || (c == '.' && i + 1 < len && Character.isDigit(input.charAt(i + 1)))) {
                int start = i;
                while (i < len && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i++;
                }
                // Scientific notation: e.g. 1e10, 2.5e-3
                if (i < len && (input.charAt(i) == 'e' || input.charAt(i) == 'E')) {
                    i++;
                    if (i < len && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
                        i++;
                    }
                    while (i < len && Character.isDigit(input.charAt(i))) {
                        i++;
                    }
                }
                String numStr = input.substring(start, i);
                double value;
                try {
                    value = Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid number '" + numStr + "' at position " + start);
                }
                tokens.add(new Token(TokenType.NUMBER, numStr, value, start));
                continue;
            }

            // Identifier: letter or underscore
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    i++;
                }
                String name = input.substring(start, i);
                tokens.add(new Token(TokenType.IDENTIFIER, name, start));
                continue;
            }

            // Single-character tokens
            TokenType type = switch (c) {
                case '+' -> TokenType.PLUS;
                case '-' -> TokenType.MINUS;
                case '*' -> TokenType.STAR;
                case '/' -> TokenType.SLASH;
                case '^' -> TokenType.CARET;
                case '(' -> TokenType.LPAREN;
                case ')' -> TokenType.RPAREN;
                case ',' -> TokenType.COMMA;
                default -> null;
            };

            if (type != null) {
                tokens.add(new Token(type, String.valueOf(c), i));
                i++;
            } else {
                throw new IllegalArgumentException(
                        "Unexpected character '" + c + "' at position " + i);
            }
        }
    }
}
