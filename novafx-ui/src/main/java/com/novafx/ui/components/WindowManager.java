package com.novafx.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 窗口管理器 — UI Kit v2 核心组件。
 * <p>
 * 管理应用程序的所有窗口，包括主窗口和分离的面板窗口。
 * 提供窗口的添加、移除、查找和焦点管理功能。
 */
public class WindowManager {

    /** 主窗口 */
    private Stage mainWindow;

    /** 所有窗口列表 */
    private final ObservableList<Stage> windows = FXCollections.observableArrayList();

    /** 窗口标题 → 窗口映射 */
    private final ObservableMap<String, Stage> windowMap = FXCollections.observableHashMap();

    /** 当前焦点窗口 */
    private Stage focusedWindow;

    /** 窗口焦点变更回调 */
    private java.util.function.Consumer<Stage> onWindowFocused;

    /**
     * 创建窗口管理器实例。
     */
    public WindowManager() {
        // 默认构造函数
    }

    /**
     * 设置主窗口。
     *
     * @param mainWindow 主窗口实例
     */
    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
        addWindow(mainWindow, "NovaFX Studio");
    }

    /**
     * 获取主窗口。
     *
     * @return 主窗口实例
     */
    public Stage getMainWindow() {
        return mainWindow;
    }

    /**
     * 添加窗口。
     *
     * @param window 窗口实例
     * @param title  窗口标题
     */
    public void addWindow(Stage window, String title) {
        if (!windows.contains(window)) {
            windows.add(window);
            windowMap.put(title, window);

            // 监听窗口关闭事件
            window.setOnHiding(e -> {
                removeWindow(window);
            });

            // 监听窗口焦点事件
            window.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    focusedWindow = window;
                    if (onWindowFocused != null) {
                        onWindowFocused.accept(window);
                    }
                }
            });
        }
    }

    /**
     * 移除窗口。
     *
     * @param window 窗口实例
     */
    public void removeWindow(Stage window) {
        windows.remove(window);
        windowMap.values().remove(window);

        if (focusedWindow == window) {
            focusedWindow = windows.isEmpty() ? null : windows.get(windows.size() - 1);
        }
    }

    /**
     * 根据标题查找窗口。
     *
     * @param title 窗口标题
     * @return 窗口实例，如果未找到则返回 null
     */
    public Stage findWindowByTitle(String title) {
        return windowMap.get(title);
    }

    /**
     * 获取所有窗口。
     *
     * @return 窗口列表
     */
    public ObservableList<Stage> getWindows() {
        return windows;
    }

    /**
     * 获取窗口数量。
     *
     * @return 窗口数量
     */
    public int getWindowCount() {
        return windows.size();
    }

    /**
     * 获取当前焦点窗口。
     *
     * @return 焦点窗口，如果没有则返回 null
     */
    public Stage getFocusedWindow() {
        return focusedWindow;
    }

    /**
     * 将焦点设置到指定窗口。
     *
     * @param window 窗口实例
     */
    public void focusWindow(Stage window) {
        if (windows.contains(window)) {
            window.requestFocus();
            focusedWindow = window;
        }
    }

    /**
     * 将焦点设置到主窗口。
     */
    public void focusMainWindow() {
        if (mainWindow != null) {
            focusWindow(mainWindow);
        }
    }

    /**
     * 关闭所有窗口（除了主窗口）。
     */
    public void closeAllWindows() {
        List<Stage> toClose = new ArrayList<>(windows);
        toClose.remove(mainWindow);

        for (Stage window : toClose) {
            window.close();
        }
    }

    /**
     * 最小化所有窗口。
     */
    public void minimizeAllWindows() {
        for (Stage window : windows) {
            window.setIconified(true);
        }
    }

    /**
     * 还原所有窗口。
     */
    public void restoreAllWindows() {
        for (Stage window : windows) {
            window.setIconified(false);
        }
    }

    /**
     * 设置窗口焦点变更回调。
     *
     * @param callback 回调函数
     */
    public void setOnWindowFocused(java.util.function.Consumer<Stage> callback) {
        this.onWindowFocused = callback;
    }

    /**
     * 检查窗口是否存在。
     *
     * @param title 窗口标题
     * @return true 表示窗口存在
     */
    public boolean hasWindow(String title) {
        return windowMap.containsKey(title);
    }

    /**
     * 获取窗口标题。
     *
     * @param window 窗口实例
     * @return 窗口标题，如果未找到则返回 null
     */
    public String getWindowTitle(Stage window) {
        for (Map.Entry<String, Stage> entry : windowMap.entrySet()) {
            if (entry.getValue() == window) {
                return entry.getKey();
            }
        }
        return null;
    }
}
