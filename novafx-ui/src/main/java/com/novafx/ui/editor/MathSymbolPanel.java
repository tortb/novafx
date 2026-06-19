package com.novafx.ui.editor;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Categorized math symbol button panel.
 * <p>
 * Groups: 基础函数 | 指数对数 | 常量 | 运算符 | 高级
 * Clicking a button inserts the expression into the active field.
 */
public final class MathSymbolPanel extends FlowPane {

    private Consumer<String> onInsert;

    private static final Map<String, List<SymbolDef>> CATEGORIES = new LinkedHashMap<>();

    static {
        CATEGORIES.put("基础函数", List.of(
                def("sin", "sin(|)"),
                def("cos", "cos(|)"),
                def("tan", "tan(|)"),
                def("asin", "asin(|)"),
                def("acos", "acos(|)"),
                def("atan", "atan(|)")
        ));
        CATEGORIES.put("指数对数", List.of(
                def("sqrt", "sqrt(|)"),
                def("pow", "pow(|,)"),
                def("exp", "exp(|)"),
                def("log", "log(|)"),
                def("ln", "log(|)")
        ));
        CATEGORIES.put("常量", List.of(
                def("π", "PI"),
                def("e", "E")
        ));
        CATEGORIES.put("运算符", List.of(
                def("+", "+"),
                def("−", "-"),
                def("×", "*"),
                def("÷", "/"),
                def("^", "^")
        ));
        CATEGORIES.put("高级", List.of(
                def("∑", "fourier(t,|,1,1,1)"),
                def("Fourier", "fourier(t,|,1,0.5,1)"),
                def("abs", "abs(|)"),
                def("floor", "floor(|)"),
                def("ceil", "ceil(|)")
        ));
    }

    /** Creates the math symbol panel. */
    public MathSymbolPanel() {
        setHgap(4);
        setVgap(4);
        setPadding(new Insets(4, 0, 4, 0));
        setStyle("-fx-background-color: #0D0D0D;");

        for (var entry : CATEGORIES.entrySet()) {
            String category = entry.getKey();
            List<SymbolDef> symbols = entry.getValue();

            // Category header
            javafx.scene.control.Label header = new javafx.scene.control.Label(category);
            header.setStyle("-fx-font-size: 10; -fx-text-fill: #555; -fx-padding: 2 4 0 4;");
            getChildren().add(header);

            for (SymbolDef sym : symbols) {
                Button btn = new Button(sym.label);
                btn.setStyle(
                        "-fx-background-color: #1A1A1A;"
                                + "-fx-border-color: #262626;"
                                + "-fx-border-radius: 3;"
                                + "-fx-background-radius: 3;"
                                + "-fx-text-fill: #CCC;"
                                + "-fx-font-size: 11;"
                                + "-fx-padding: 3 8;"
                                + "-fx-cursor: hand;"
                );
                btn.setOnAction(e -> {
                    if (onInsert != null) onInsert.accept(sym.insertText);
                });

                Tooltip tip = new Tooltip(sym.label + " — " + sym.insertText);
                Tooltip.install(btn, tip);

                getChildren().add(btn);
            }
        }
    }

    /** Sets the callback: receives text to insert at cursor. */
    public void setOnInsert(Consumer<String> callback) {
        this.onInsert = callback;
    }

    private static SymbolDef def(String label, String insertText) {
        return new SymbolDef(label, insertText);
    }

    private record SymbolDef(String label, String insertText) {
    }
}
