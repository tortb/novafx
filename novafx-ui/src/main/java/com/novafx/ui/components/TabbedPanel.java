package com.novafx.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab 面板 — UI Kit v2 核心组件。
 * <p>
 * 支持多个标签页的面板组件，类似于 VSCode 的编辑器标签页。
 * 提供标签页的添加、移除、切换和关闭功能。
 */
public class TabbedPanel extends FXPanel {

    /** 标签页列表 */
    private final ObservableList<Tab> tabs = FXCollections.observableArrayList();

    /** 当前选中的标签页 */
    private Tab activeTab;

    /** 标签页头部容器 */
    private HBox headerContainer;

    /** 内容容器 */
    private BorderPane contentContainer;

    /** 选择模型 */
    private final SingleSelectionModel<Tab> selectionModel = new SingleSelectionModel<>() {
        @Override
        protected Tab getModelItem(int index) {
            if (index >= 0 && index < tabs.size()) {
                return tabs.get(index);
            }
            return null;
        }

        @Override
        protected int getItemCount() {
            return tabs.size();
        }
    };

    /**
     * 创建 Tab 面板实例。
     */
    public TabbedPanel() {
        getStyleClass().add("tabbed-panel");
        initLayout();
    }

    /**
     * 初始化布局。
     */
    private void initLayout() {
        // 标签页头部
        headerContainer = new HBox();
        headerContainer.getStyleClass().add("tab-header-container");
        headerContainer.setSpacing(0);

        // 内容容器
        contentContainer = new BorderPane();
        contentContainer.getStyleClass().add("tab-content-container");

        // 布局
        getChildren().addAll(headerContainer, contentContainer);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
    }

    /**
     * 添加标签页。
     *
     * @param title   标签页标题
     * @param content 标签页内容
     * @return 创建的标签页
     */
    public Tab addTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        return addTab(tab);
    }

    /**
     * 添加标签页。
     *
     * @param tab 标签页
     * @return 添加的标签页
     */
    public Tab addTab(Tab tab) {
        tabs.add(tab);
        updateHeader();

        // 如果是第一个标签页，自动选中
        if (tabs.size() == 1) {
            selectionModel.select(0);
            updateContent();
        }

        return tab;
    }

    /**
     * 移除标签页。
     *
     * @param tab 要移除的标签页
     */
    public void removeTab(Tab tab) {
        int index = tabs.indexOf(tab);
        if (index >= 0) {
            tabs.remove(tab);
            updateHeader();

            // 如果移除的是当前选中的标签页
            if (activeTab == tab) {
                if (tabs.isEmpty()) {
                    activeTab = null;
                    contentContainer.setCenter(null);
                } else {
                    // 选择相邻的标签页
                    int newIndex = Math.min(index, tabs.size() - 1);
                    selectionModel.select(newIndex);
                    updateContent();
                }
            }
        }
    }

    /**
     * 根据索引移除标签页。
     *
     * @param index 标签页索引
     */
    public void removeTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            removeTab(tabs.get(index));
        }
    }

    /**
     * 选中指定标签页。
     *
     * @param tab 要选中的标签页
     */
    public void selectTab(Tab tab) {
        int index = tabs.indexOf(tab);
        if (index >= 0) {
            selectionModel.select(index);
            updateContent();
        }
    }

    /**
     * 根据索引选中标签页。
     *
     * @param index 标签页索引
     */
    public void selectTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            selectionModel.select(index);
            updateContent();
        }
    }

    /**
     * 获取当前选中的标签页。
     *
     * @return 当前选中的标签页，如果没有选中则返回 null
     */
    public Tab getActiveTab() {
        return activeTab;
    }

    /**
     * 获取所有标签页。
     *
     * @return 标签页列表
     */
    public ObservableList<Tab> getTabs() {
        return tabs;
    }

    /**
     * 获取标签页数量。
     *
     * @return 标签页数量
     */
    public int getTabCount() {
        return tabs.size();
    }

    /**
     * 更新标签页头部。
     */
    private void updateHeader() {
        headerContainer.getChildren().clear();

        for (Tab tab : tabs) {
            TabHeader header = new TabHeader(tab);
            header.setOnClose(() -> removeTab(tab));
            header.setOnSelect(() -> selectTab(tab));
            header.setActive(tab == activeTab);
            headerContainer.getChildren().add(header);
        }
    }

    /**
     * 更新内容区域。
     */
    private void updateContent() {
        Tab selected = selectionModel.getSelectedItem();
        if (selected != null) {
            activeTab = selected;
            contentContainer.setCenter(selected.getContent());
            updateHeader(); // 更新头部高亮状态
        }
    }

    /**
     * 获取选择模型。
     *
     * @return 选择模型
     */
    public SingleSelectionModel<Tab> getSelectionModel() {
        return selectionModel;
    }

    /**
     * Tab 标签页类。
     */
    public static class Tab {
        private String title;
        private Node content;
        private boolean closable = true;
        private Object userData;

        /**
         * 创建 Tab 实例。
         *
         * @param title   标签页标题
         * @param content 标签页内容
         */
        public Tab(String title, Node content) {
            this.title = title;
            this.content = content;
        }

        /**
         * 获取标签页标题。
         *
         * @return 标签页标题
         */
        public String getTitle() {
            return title;
        }

        /**
         * 设置标签页标题。
         *
         * @param title 标签页标题
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * 获取标签页内容。
         *
         * @return 标签页内容
         */
        public Node getContent() {
            return content;
        }

        /**
         * 设置标签页内容。
         *
         * @param content 标签页内容
         */
        public void setContent(Node content) {
            this.content = content;
        }

        /**
         * 检查是否可关闭。
         *
         * @return true 表示可关闭
         */
        public boolean isClosable() {
            return closable;
        }

        /**
         * 设置是否可关闭。
         *
         * @param closable true 表示可关闭
         */
        public void setClosable(boolean closable) {
            this.closable = closable;
        }

        /**
         * 获取用户数据。
         *
         * @return 用户数据
         */
        public Object getUserData() {
            return userData;
        }

        /**
         * 设置用户数据。
         *
         * @param userData 用户数据
         */
        public void setUserData(Object userData) {
            this.userData = userData;
        }
    }
}
