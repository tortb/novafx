package com.novafx.ui.editor;

import java.util.regex.Pattern;

/**
 * LaTeX 与标准表达式之间的转换工具。
 * <p>
 * 纯静态方法，无 JavaFX 依赖，可独立测试。
 */
public final class LatexUtils {

    private static final Pattern LATEX_CMD = Pattern.compile("\\\\([a-zA-Z]+)");

    private LatexUtils() {}

    /**
     * 将 LaTeX 命令语法转换为标准表达式语法。
     * <pre>
     * \sin(t)       → sin(t)
     * \sqrt{x}      → sqrt(x)
     * \frac{a}{b}   → (a)/(b)
     * </pre>
     */
    public static String fromLatex(String latex) {
        if (latex == null || latex.isBlank()) return latex;
        String result = latex;
        result = result.replaceAll("\\\\frac\\{([^}]*)\\}\\{([^}]*)\\}", "($1)/($2)");
        result = result.replaceAll("\\\\sqrt\\{([^}]*)\\}", "sqrt($1)");
        result = LATEX_CMD.matcher(result).replaceAll(mr -> mr.group(1));
        result = result.replace("{", "").replace("}", "");
        return result;
    }

    /**
     * 将标准表达式转换为 LaTeX 风格（给函数名加反斜杠）。
     * <pre>
     * sin(t)   → \sin(t)
     * cos(t)   → \cos(t)
     * </pre>
     */
    public static String toLatex(String expr) {
        if (expr == null || expr.isBlank()) return expr;
        String[] funcs = {"sin", "cos", "tan", "sqrt", "log", "exp", "abs", "floor", "ceil"};
        String result = expr;
        for (String f : funcs) {
            result = result.replaceAll("(?<!\\\\)" + f + "\\(", "\\\\" + f + "(");
        }
        return result;
    }
}
