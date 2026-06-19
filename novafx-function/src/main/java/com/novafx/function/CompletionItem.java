package com.novafx.function;

/**
 * A single completion suggestion produced by the {@link CompletionEngine}.
 *
 * @param label      the display text shown in the completion popup
 * @param kind       the type of completion
 * @param insertText the text inserted when this completion is accepted
 * @param detail     optional description shown next to the label
 */
public record CompletionItem(
        String label,
        CompletionKind kind,
        String insertText,
        String detail
) {
    /**
     * Creates a completion item where insertText defaults to the label.
     */
    public CompletionItem(String label, CompletionKind kind, String detail) {
        this(label, kind, label, detail);
    }
}
