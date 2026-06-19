package com.novafx.function;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser that converts a token stream into an
 * {@link AstNode} tree.
 * <p>
 * Grammar (precedence climbing, lowest to highest):
 * <pre>
 * expression  → term (('+' | '-') term)*
 * term        → power (('*' | '/') power)*
 * power       → unary ('^' power)?
 * unary       → ('-' | '+') unary | call
 * call        → primary ('(' arguments ')')?
 * primary     → NUMBER | IDENTIFIER | '(' expression ')'
 * arguments   → expression (',' expression)*
 * </pre>
 */
public final class AstParser {

    private final Tokenizer tokenizer;
    private int pos;
    private Token current;

    /**
     * Creates a parser for the given tokenizer.
     *
     * @param tokenizer a fully tokenized input
     */
    public AstParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.pos = 0;
        this.current = tokenizer.get(0);
    }

    /**
     * Parses the full expression and consumes all input.
     *
     * @return the root AST node
     * @throws IllegalArgumentException on parse errors (with position info)
     */
    public AstNode parse() {
        AstNode node = expression();
        if (current.type() != TokenType.EOF) {
            throw error("Expected end of expression but found '" + current.lexeme() + "'");
        }
        return node;
    }

    // ---------------------------------------------------------------
    // Grammar rules
    // ---------------------------------------------------------------

    /**
     * expression → term (('+' | '-') term)*
     */
    private AstNode expression() {
        AstNode node = term();
        while (current.type() == TokenType.PLUS || current.type() == TokenType.MINUS) {
            Token op = advance();
            AstNode right = term();
            node = new AstNode.BinaryNode(node, right,
                    op.type() == TokenType.PLUS ? AstNode.BinaryOp.ADD : AstNode.BinaryOp.SUB);
        }
        return node;
    }

    /**
     * term → power (('*' | '/') power)*
     */
    private AstNode term() {
        AstNode node = power();
        while (current.type() == TokenType.STAR || current.type() == TokenType.SLASH) {
            Token op = advance();
            AstNode right = power();
            node = new AstNode.BinaryNode(node, right,
                    op.type() == TokenType.STAR ? AstNode.BinaryOp.MUL : AstNode.BinaryOp.DIV);
        }
        return node;
    }

    /**
     * power → unary ('^' power)?
     * <p>
     * Right-associative: {@code a^b^c} → {@code a^(b^c)}
     */
    private AstNode power() {
        AstNode node = unary();
        if (current.type() == TokenType.CARET) {
            advance();
            AstNode right = power(); // recursive call for right-associativity
            node = new AstNode.BinaryNode(node, right, AstNode.BinaryOp.POW);
        }
        return node;
    }

    /**
     * unary → ('-' | '+') unary | call
     */
    private AstNode unary() {
        if (current.type() == TokenType.MINUS) {
            advance();
            AstNode operand = unary();
            return new AstNode.UnaryNode(operand, AstNode.UnaryOp.NEG);
        }
        if (current.type() == TokenType.PLUS) {
            advance();
            return unary(); // unary + is a no-op
        }
        return call();
    }

    /**
     * call → primary ('(' arguments ')')?
     */
    private AstNode call() {
        AstNode node = primary();

        // Function call: identifier followed by '('
        if (current.type() == TokenType.LPAREN) {
            // The primary must have been an IDENTIFIER
            if (node instanceof AstNode.VariableNode var) {
                advance(); // consume '('
                List<AstNode> args = arguments();
                consume(TokenType.RPAREN, "Expected ')' after arguments");
                return new AstNode.FunctionNode(var.name(), args);
            }
            throw error("Expected function name before '('");
        }

        return node;
    }

    /**
     * primary → NUMBER | IDENTIFIER | '(' expression ')'
     */
    private AstNode primary() {
        if (current.type() == TokenType.NUMBER) {
            Token t = advance();
            return new AstNode.ConstantNode(t.numericValue());
        }

        if (current.type() == TokenType.IDENTIFIER) {
            Token t = advance();
            return new AstNode.VariableNode(t.lexeme());
        }

        if (current.type() == TokenType.LPAREN) {
            advance(); // consume '('
            AstNode node = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return node;
        }

        throw error("Expected expression but found '" + current.lexeme() + "'");
    }

    /**
     * arguments → expression (',' expression)*
     */
    private List<AstNode> arguments() {
        List<AstNode> args = new ArrayList<>();
        if (current.type() == TokenType.RPAREN) {
            return args; // zero-argument function call
        }
        args.add(expression());
        while (current.type() == TokenType.COMMA) {
            advance(); // consume ','
            args.add(expression());
        }
        return args;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Token advance() {
        Token t = current;
        pos++;
        current = tokenizer.get(pos);
        return t;
    }

    private void consume(TokenType expected, String message) {
        if (current.type() == expected) {
            advance();
        } else {
            throw error(message);
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Parse error at position " + current.position()
                + ": " + message);
    }
}
