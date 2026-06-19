package com.novafx.ui.view;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.editor.ExpressionEditor;
import com.novafx.ui.editor.LatexEditor;
import com.novafx.ui.editor.ModeSwitcher;
import com.novafx.ui.editor.ParserHub;
import com.novafx.ui.editor.SimpleMathEditor;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 函数编辑器容器，支持三种编辑模式。
 * <p>
 * 模式：
 * <ul>
 *   <li><b>简易</b> — MathSymbolPanel + 表达式输入框</li>
 *   <li><b>专业</b> — 代码编辑器风格，语法高亮 + 自动补全</li>
 *   <li><b>LaTeX</b> — LaTeX 源码编辑 + 预览</li>
 * </ul>
 */
public final class FunctionEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(FunctionEditor.class);

    private final ModeSwitcher modeSwitcher = new ModeSwitcher();
    private final SimpleMathEditor simpleEditor = new SimpleMathEditor();
    private final ExpressionEditor expressionEditor = new ExpressionEditor();
    private final LatexEditor latexEditor = new LatexEditor();

    private VBox activeContainer;
    private Consumer<FunctionDefinition> onFunctionChanged;
    private Consumer<String> onError;

    /** 创建函数编辑器。 */
    public FunctionEditor() {
        setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 1 0 0 0;");

        getChildren().add(modeSwitcher);

        // 默认显示简易编辑器
        activeContainer = wrap(simpleEditor);
        getChildren().add(activeContainer);

        // 模式切换
        modeSwitcher.setOnModeChanged(mode -> switchEditor(mode));

        // 共享事件转发
        var forward = (Consumer<FunctionDefinition>) def -> {
            if (onFunctionChanged != null) onFunctionChanged.accept(def);
        };
        var errorForward = (Consumer<String>) err -> {
            if (onError != null) onError.accept(err);
        };

        simpleEditor.setOnFunctionChanged(forward);
        simpleEditor.setOnError(errorForward);
        expressionEditor.setOnFunctionChanged(forward);
        expressionEditor.setOnError(errorForward);
        latexEditor.setOnFunctionChanged(forward);
        latexEditor.setOnError(errorForward);
    }

    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** 设置错误回调（用于显示即时错误信息）。 */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        simpleEditor.loadDefinition(def);
        expressionEditor.loadDefinition(def);
        latexEditor.loadDefinition(def);
    }

    public FunctionDefinition getDefinition() {
        return switch (modeSwitcher.getCurrentMode()) {
            case EXPRESSION -> expressionEditor.getDefinition();
            case LATEX -> latexEditor.getDefinition();
            default -> simpleEditor.getDefinition();
        };
    }

    /** 清除所有编辑器的错误提示。 */
    public void clearErrors() {
        simpleEditor.clearError();
        expressionEditor.clearError();
        latexEditor.clearError();
    }

    private void switchEditor(ParserHub.InputMode mode) {
        getChildren().remove(activeContainer);

        VBox newContainer = switch (mode) {
            case EXPRESSION -> wrap(expressionEditor);
            case LATEX -> wrap(latexEditor);
            default -> wrap(simpleEditor);
        };

        activeContainer = newContainer;
        getChildren().add(activeContainer);
    }

    private static VBox wrap(javafx.scene.Node editor) {
        VBox box = new VBox(editor);
        VBox.setVgrow(editor, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }
}
