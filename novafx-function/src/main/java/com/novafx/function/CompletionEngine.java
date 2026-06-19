package com.novafx.function;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Suggests code completions for mathematical expressions.
 * <p>
 * Matches the input prefix against built-in functions, variables,
 * constants, and preset snippets. Results are sorted by relevance:
 * exact prefix match first, then case-insensitive prefix match.
 */
public final class CompletionEngine {

    private static final List<CompletionItem> ITEMS = buildItems();

    /**
     * Returns completion suggestions for the given prefix.
     *
     * @param prefix the text before the cursor (may be empty)
     * @return relevant completions, sorted by relevance (never null)
     */
    public List<CompletionItem> suggest(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return List.of();
        }

        String lower = prefix.toLowerCase();
        List<CompletionItem> result = new ArrayList<>();

        for (CompletionItem item : ITEMS) {
            if (item.label().toLowerCase().startsWith(lower)) {
                result.add(item);
            }
        }

        result.sort(Comparator
                .<CompletionItem, Boolean>comparing(c -> c.label().startsWith(prefix))
                .reversed()
                .thenComparing(CompletionItem::label));

        return List.copyOf(result);
    }

    private static List<CompletionItem> buildItems() {
        List<CompletionItem> items = new ArrayList<>();

        // Functions
        items.add(item("sin", CompletionKind.FUNCTION, "sin(x) — sine"));
        items.add(item("cos", CompletionKind.FUNCTION, "cos(x) — cosine"));
        items.add(item("tan", CompletionKind.FUNCTION, "tan(x) — tangent"));
        items.add(item("sqrt", CompletionKind.FUNCTION, "sqrt(x) — square root"));
        items.add(item("pow", CompletionKind.FUNCTION, "pow(x,y) — power"));
        items.add(item("abs", CompletionKind.FUNCTION, "abs(x) — absolute value"));
        items.add(item("log", CompletionKind.FUNCTION, "log(x) — natural log"));
        items.add(item("exp", CompletionKind.FUNCTION, "exp(x) — exponential"));
        items.add(item("floor", CompletionKind.FUNCTION, "floor(x) — round down"));
        items.add(item("ceil", CompletionKind.FUNCTION, "ceil(x) — round up"));
        items.add(item("min", CompletionKind.FUNCTION, "min(a,b) — minimum"));
        items.add(item("max", CompletionKind.FUNCTION, "max(a,b) — maximum"));
        items.add(item("fourier", CompletionKind.FUNCTION, "fourier(t,N,ω,d,a) — Fourier series"));

        // Variables
        items.add(new CompletionItem("t", CompletionKind.VARIABLE, "parameter variable"));
        items.add(new CompletionItem("x", CompletionKind.VARIABLE, "x-coordinate"));
        items.add(new CompletionItem("y", CompletionKind.VARIABLE, "y-coordinate"));
        items.add(new CompletionItem("z", CompletionKind.VARIABLE, "z-coordinate"));

        // Constants
        items.add(new CompletionItem("PI", CompletionKind.CONSTANT, "π = 3.14159…"));
        items.add(new CompletionItem("E", CompletionKind.CONSTANT, "e = 2.71828…"));

        // Snippets
        items.add(new CompletionItem("Circle", CompletionKind.SNIPPET, "x=cos(t), y=sin(t)"));
        items.add(new CompletionItem("Heart", CompletionKind.SNIPPET, "heart curve"));
        items.add(new CompletionItem("Spiral", CompletionKind.SNIPPET, "x=t*cos(t), y=t*sin(t)"));
        items.add(new CompletionItem("Helix", CompletionKind.SNIPPET, "3D helix"));
        items.add(new CompletionItem("Flower", CompletionKind.SNIPPET, "rose curve"));
        items.add(new CompletionItem("Infinity", CompletionKind.SNIPPET, "lemniscate"));
        items.add(new CompletionItem("Wave", CompletionKind.SNIPPET, "sine wave"));
        items.add(new CompletionItem("Star", CompletionKind.SNIPPET, "star shape"));

        return List.copyOf(items);
    }

    private static CompletionItem item(String name, CompletionKind kind, String detail) {
        return new CompletionItem(name, kind, detail);
    }
}
