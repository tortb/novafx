package com.novafx.function;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CompletionEngineTest {

    private final CompletionEngine engine = new CompletionEngine();

    @Test
    void shouldReturnEmptyForEmptyPrefix() {
        assertThat(engine.suggest("")).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullPrefix() {
        assertThat(engine.suggest(null)).isEmpty();
    }

    @Test
    void shouldSuggestSinForSi() {
        List<CompletionItem> results = engine.suggest("si");
        assertThat(results).extracting(CompletionItem::label)
                .contains("sin");
    }

    @Test
    void shouldSuggestCosCoTanForC() {
        List<CompletionItem> results = engine.suggest("c");
        assertThat(results).extracting(CompletionItem::label)
                .contains("cos", "ceil");
    }

    @Test
    void shouldSuggestForPartialUpper() {
        List<CompletionItem> results = engine.suggest("SQ");
        assertThat(results).extracting(CompletionItem::label)
                .contains("sqrt");
    }

    @Test
    void shouldSuggestNoneForNoMatch() {
        assertThat(engine.suggest("zzz")).isEmpty();
    }

    @Test
    void shouldPrioritizeExactMatch() {
        List<CompletionItem> results = engine.suggest("t");
        assertThat(results.get(0).label()).isEqualTo("t");
    }

    @Test
    void shouldIncludeVariables() {
        List<CompletionItem> results = engine.suggest("x");
        assertThat(results).extracting(CompletionItem::kind)
                .contains(CompletionKind.VARIABLE);
    }

    @Test
    void shouldIncludeConstants() {
        List<CompletionItem> results = engine.suggest("p");
        assertThat(results).extracting(CompletionItem::label)
                .contains("PI");
    }

    @Test
    void shouldIncludeSnippets() {
        List<CompletionItem> results = engine.suggest("He");
        assertThat(results).extracting(CompletionItem::kind)
                .contains(CompletionKind.SNIPPET);
    }

    @Test
    void shouldReturnAllItemsForSingleLetter() {
        List<CompletionItem> results = engine.suggest("s");
        assertThat(results).hasSizeGreaterThan(2);
    }

    @Test
    void shouldReturnImmutableList() {
        List<CompletionItem> results = engine.suggest("t");
        assertThatThrownBy(() -> results.add(new CompletionItem("x", CompletionKind.VARIABLE, "")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void completionItemShouldHaveConsistentToString() {
        CompletionItem item = new CompletionItem("sin", CompletionKind.FUNCTION, "sine");
        assertThat(item.toString()).contains("sin");
    }

    @Test
    void insertTextDefaultsToLabel() {
        CompletionItem item = new CompletionItem("PI", CompletionKind.CONSTANT, "pi");
        assertThat(item.insertText()).isEqualTo("PI");
    }
}
