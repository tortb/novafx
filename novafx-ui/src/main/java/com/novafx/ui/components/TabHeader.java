package com.novafx.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Tab 头部 — UI Kit v2 辅助组件。
 * <p>
 * 显示标签页的标题和关闭按钮，提供选中和关闭功能。
 */
public class TabHeader extends HBox {

    /** 关联的标签页 */
    private final TabbedPanel.Tab tab;

    /** 标题标签 */
    private Label titleLabel;

    /** 关闭按钮 */
    private FXButton closeButton;

    /** 是否激活 */
    private boolean active = false;

    /** 选中回调 */
    private Runnable onSelect = () -> {};

    /** 关闭回调 */
    private Runnable onClose = () -> {};

    /**
     * 创建 Tab 头部实例。
     *
     * @param tab 关联的标签页
     */
    public TabHeader(TabbedPanel.Tab tab) {
        this.tab = tab;
        getStyleClass().add("tab-header");
        initLayout();
        initEventHandlers();
    }

    /**
     * 初始化布局。
     */
    private void initLayout() {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);

        // 标题标签
        titleLabel = new Label(tab.getTitle());
        titleLabel.getStyleClass().add("tab-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // 关闭按钮
        closeButton = new FXButton("×");
        closeButton.getStyleClass().add("tab-close-button");
        closeButton.setVisible(tab.isClosable());
        closeButton.setManaged(tab.isClosable());

        getChildren().addAll(titleLabel, closeButton);
    }

    /**
     * 初始化事件处理器。
     */
    private void initEventHandlers() {
        // 点击选中
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getTarget() != closeButton) {
                onSelect.run();
            }
        });

        // 关闭按钮点击
        closeButton.setOnAction(e -> {
            onClose.run();
        });

        // 悬停效果
        addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (!active) {
                getStyleClass().add("tab-header-hover");
            }
        });

        addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            getStyleClass().remove("tab-header-hover");
        });
    }

    /**
     * 设置激活状态。
     *
     * @param active true 表示激活
     */
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            getStyleClass().add("tab-header-active");
        } else {
            getStyleClass().remove("tab-header-active");
        }
    }

    /**
     * 检查是否激活。
     *
     * @return true 表示激活
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 设置选中回调。
     *
     * @param callback 选中回调
     */
    public void setOnSelect(Runnable callback) {
        this.onSelect = callback != null ? callback : () -> {};
    }

    /**
     * 设置关闭回调。
     *
     * @param callback 关闭回调
     */
    public void setOnClose(Runnable callback) {
        this.onClose = callback != null ? callback : () -> {};
    }

    /**
     * 更新标题。
     *
     * @param title 新标题
     */
    public void updateTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * 获取关联的标签页。
     *
     * @return 标签页
     */
    public TabbedPanel.Tab getTab() {
        return tab;
    }
}
