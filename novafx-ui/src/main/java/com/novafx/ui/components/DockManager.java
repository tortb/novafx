package com.novafx.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停靠管理器 — UI Kit v2 核心组件。
 * <p>
 * 管理面板的停靠和浮动状态，提供统一的面板管理接口。
 * 支持面板的添加、移除、停靠和取消停靠操作。
 */
public class DockManager {

    /** 停靠的面板映射（位置 → 面板列表） */
    private final ObservableMap<DraggableDockPane.DockPosition, ObservableList<FloatingPanel>> dockedPanels;

    /** 浮动的面板列表 */
    private final ObservableList<FloatingPanel> floatingPanels;

    /** 关联的 DockPane */
    private DraggableDockPane dockPane;

    /**
     * 创建停靠管理器实例。
     */
    public DockManager() {
        dockedPanels = FXCollections.observableHashMap();
        floatingPanels = FXCollections.observableArrayList();

        // 初始化各个位置的面板列表
        for (DraggableDockPane.DockPosition position : DraggableDockPane.DockPosition.values()) {
            dockedPanels.put(position, FXCollections.observableArrayList());
        }
    }

    /**
     * 创建停靠管理器实例并关联 DockPane。
     *
     * @param dockPane 关联的 DockPane
     */
    public DockManager(DraggableDockPane dockPane) {
        this();
        this.dockPane = dockPane;
    }

    /**
     * 添加面板到指定位置。
     *
     * @param panel    要添加的面板
     * @param position 停靠位置
     */
    public void addPanel(FloatingPanel panel, DraggableDockPane.DockPosition position) {
        // 从其他位置移除
        removePanel(panel);

        // 添加到指定位置
        ObservableList<FloatingPanel> panels = dockedPanels.get(position);
        if (panels != null) {
            panels.add(panel);
            panel.dock(position);
        }

        // 更新 DockPane
        updateDockPane();
    }

    /**
     * 添加浮动面板。
     *
     * @param panel 要添加的面板
     */
    public void addFloatingPanel(FloatingPanel panel) {
        // 从其他位置移除
        removePanel(panel);

        // 添加到浮动列表
        floatingPanels.add(panel);
        panel.undock();
    }

    /**
     * 移除面板。
     *
     * @param panel 要移除的面板
     */
    public void removePanel(FloatingPanel panel) {
        // 从停靠位置移除
        for (ObservableList<FloatingPanel> panels : dockedPanels.values()) {
            panels.remove(panel);
        }

        // 从浮动列表移除
        floatingPanels.remove(panel);
    }

    /**
     * 将面板移动到新位置。
     *
     * @param panel    要移动的面板
     * @param position 新的停靠位置
     */
    public void movePanel(FloatingPanel panel, DraggableDockPane.DockPosition position) {
        addPanel(panel, position);
    }

    /**
     * 将面板变为浮动状态。
     *
     * @param panel 要浮动的面板
     */
    public void floatPanel(FloatingPanel panel) {
        addFloatingPanel(panel);
    }

    /**
     * 获取指定位置的面板列表。
     *
     * @param position 停靠位置
     * @return 面板列表
     */
    public ObservableList<FloatingPanel> getDockedPanels(DraggableDockPane.DockPosition position) {
        return dockedPanels.getOrDefault(position, FXCollections.observableArrayList());
    }

    /**
     * 获取所有浮动面板。
     *
     * @return 浮动面板列表
     */
    public ObservableList<FloatingPanel> getFloatingPanels() {
        return floatingPanels;
    }

    /**
     * 获取所有面板（包括停靠和浮动）。
     *
     * @return 所有面板列表
     */
    public List<FloatingPanel> getAllPanels() {
        List<FloatingPanel> allPanels = new ArrayList<>();
        for (ObservableList<FloatingPanel> panels : dockedPanels.values()) {
            allPanels.addAll(panels);
        }
        allPanels.addAll(floatingPanels);
        return allPanels;
    }

    /**
     * 根据标题查找面板。
     *
     * @param title 面板标题
     * @return 找到的面板，如果未找到则返回 null
     */
    public FloatingPanel findPanelByTitle(String title) {
        for (FloatingPanel panel : getAllPanels()) {
            if (panel.getTitle().equals(title)) {
                return panel;
            }
        }
        return null;
    }

    /**
     * 更新 DockPane 的布局。
     */
    private void updateDockPane() {
        if (dockPane == null) {
            return;
        }

        // 更新左侧
        ObservableList<FloatingPanel> leftPanels = dockedPanels.get(DraggableDockPane.DockPosition.LEFT);
        if (leftPanels != null && !leftPanels.isEmpty()) {
            // 如果有多个面板，可以创建 TabbedPanel
            if (leftPanels.size() == 1) {
                dockPane.setDockLeft(leftPanels.get(0));
            } else {
                // 创建 TabbedPanel
                TabbedPanel tabbedPanel = new TabbedPanel();
                for (FloatingPanel panel : leftPanels) {
                    tabbedPanel.addTab(panel.getTitle(), panel);
                }
                dockPane.setDockLeft(tabbedPanel);
            }
        } else {
            dockPane.setDockLeft(null);
        }

        // 更新右侧
        ObservableList<FloatingPanel> rightPanels = dockedPanels.get(DraggableDockPane.DockPosition.RIGHT);
        if (rightPanels != null && !rightPanels.isEmpty()) {
            if (rightPanels.size() == 1) {
                dockPane.setDockRight(rightPanels.get(0));
            } else {
                TabbedPanel tabbedPanel = new TabbedPanel();
                for (FloatingPanel panel : rightPanels) {
                    tabbedPanel.addTab(panel.getTitle(), panel);
                }
                dockPane.setDockRight(tabbedPanel);
            }
        } else {
            dockPane.setDockRight(null);
        }

        // 更新底部
        ObservableList<FloatingPanel> bottomPanels = dockedPanels.get(DraggableDockPane.DockPosition.BOTTOM);
        if (bottomPanels != null && !bottomPanels.isEmpty()) {
            if (bottomPanels.size() == 1) {
                dockPane.setDockBottom(bottomPanels.get(0));
            } else {
                TabbedPanel tabbedPanel = new TabbedPanel();
                for (FloatingPanel panel : bottomPanels) {
                    tabbedPanel.addTab(panel.getTitle(), panel);
                }
                dockPane.setDockBottom(tabbedPanel);
            }
        } else {
            dockPane.setDockBottom(null);
        }
    }

    /**
     * 设置关联的 DockPane。
     *
     * @param dockPane DockPane 实例
     */
    public void setDockPane(DraggableDockPane dockPane) {
        this.dockPane = dockPane;
        updateDockPane();
    }

    /**
     * 获取关联的 DockPane。
     *
     * @return DockPane 实例
     */
    public DraggableDockPane getDockPane() {
        return dockPane;
    }
}
