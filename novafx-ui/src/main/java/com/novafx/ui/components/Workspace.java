package com.novafx.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区 — UI Kit v2 核心组件。
 * <p>
 * 管理一组相关的项目和布局配置。
 * 支持工作区的保存、加载和切换功能。
 */
public class Workspace {

    /** 工作区名称 */
    private String name;

    /** 工作区描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后修改时间 */
    private LocalDateTime lastModifiedAt;

    /** 面板布局配置 */
    private final ObservableMap<String, PanelLayout> panelLayouts = FXCollections.observableHashMap();

    /** 项目引用列表 */
    private final ObservableList<ProjectReference> projects = FXCollections.observableArrayList();

    /** 活动项目 */
    private ProjectReference activeProject;

    /** 工作区配置 */
    private final Map<String, Object> config = new HashMap<>();

    /**
     * 创建工作区实例。
     *
     * @param name 工作区名称
     */
    public Workspace(String name) {
        this.name = name;
        this.description = "";
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取工作区名称。
     *
     * @return 工作区名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置工作区名称。
     *
     * @param name 工作区名称
     */
    public void setName(String name) {
        this.name = name;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取工作区描述。
     *
     * @return 工作区描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置工作区描述。
     *
     * @param description 工作区描述
     */
    public void setDescription(String description) {
        this.description = description;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取最后修改时间。
     *
     * @return 最后修改时间
     */
    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    /**
     * 添加面板布局配置。
     *
     * @param panelId 面板 ID
     * @param layout  布局配置
     */
    public void addPanelLayout(String panelId, PanelLayout layout) {
        panelLayouts.put(panelId, layout);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 移除面板布局配置。
     *
     * @param panelId 面板 ID
     */
    public void removePanelLayout(String panelId) {
        panelLayouts.remove(panelId);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取面板布局配置。
     *
     * @param panelId 面板 ID
     * @return 布局配置，如果不存在则返回 null
     */
    public PanelLayout getPanelLayout(String panelId) {
        return panelLayouts.get(panelId);
    }

    /**
     * 获取所有面板布局配置。
     *
     * @return 面板布局配置映射
     */
    public ObservableMap<String, PanelLayout> getPanelLayouts() {
        return panelLayouts;
    }

    /**
     * 添加项目引用。
     *
     * @param project 项目引用
     */
    public void addProject(ProjectReference project) {
        projects.add(project);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 移除项目引用。
     *
     * @param project 项目引用
     */
    public void removeProject(ProjectReference project) {
        projects.remove(project);
        if (activeProject == project) {
            activeProject = projects.isEmpty() ? null : projects.get(0);
        }
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取所有项目引用。
     *
     * @return 项目引用列表
     */
    public ObservableList<ProjectReference> getProjects() {
        return projects;
    }

    /**
     * 设置活动项目。
     *
     * @param project 项目引用
     */
    public void setActiveProject(ProjectReference project) {
        this.activeProject = project;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取活动项目。
     *
     * @return 活动项目，如果没有则返回 null
     */
    public ProjectReference getActiveProject() {
        return activeProject;
    }

    /**
     * 设置配置项。
     *
     * @param key   配置键
     * @param value 配置值
     */
    public void setConfig(String key, Object value) {
        config.put(key, value);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * 获取配置项。
     *
     * @param key 配置键
     * @return 配置值，如果不存在则返回 null
     */
    public Object getConfig(String key) {
        return config.get(key);
    }

    /**
     * 获取所有配置。
     *
     * @return 配置映射
     */
    public Map<String, Object> getConfigs() {
        return config;
    }

    /**
     * 面板布局配置。
     */
    public static class PanelLayout {
        private double x;
        private double y;
        private double width;
        private double height;
        private DraggableDockPane.DockPosition dockPosition;
        private boolean visible;
        private boolean floating;

        /**
         * 创建面板布局配置实例。
         */
        public PanelLayout() {
            this.visible = true;
            this.floating = false;
        }

        /**
         * 获取 X 坐标。
         *
         * @return X 坐标
         */
        public double getX() {
            return x;
        }

        /**
         * 设置 X 坐标。
         *
         * @param x X 坐标
         */
        public void setX(double x) {
            this.x = x;
        }

        /**
         * 获取 Y 坐标。
         *
         * @return Y 坐标
         */
        public double getY() {
            return y;
        }

        /**
         * 设置 Y 坐标。
         *
         * @param y Y 坐标
         */
        public void setY(double y) {
            this.y = y;
        }

        /**
         * 获取宽度。
         *
         * @return 宽度
         */
        public double getWidth() {
            return width;
        }

        /**
         * 设置宽度。
         *
         * @param width 宽度
         */
        public void setWidth(double width) {
            this.width = width;
        }

        /**
         * 获取高度。
         *
         * @return 高度
         */
        public double getHeight() {
            return height;
        }

        /**
         * 设置高度。
         *
         * @param height 高度
         */
        public void setHeight(double height) {
            this.height = height;
        }

        /**
         * 获取停靠位置。
         *
         * @return 停靠位置
         */
        public DraggableDockPane.DockPosition getDockPosition() {
            return dockPosition;
        }

        /**
         * 设置停靠位置。
         *
         * @param dockPosition 停靠位置
         */
        public void setDockPosition(DraggableDockPane.DockPosition dockPosition) {
            this.dockPosition = dockPosition;
        }

        /**
         * 检查是否可见。
         *
         * @return true 表示可见
         */
        public boolean isVisible() {
            return visible;
        }

        /**
         * 设置是否可见。
         *
         * @param visible true 表示可见
         */
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        /**
         * 检查是否浮动。
         *
         * @return true 表示浮动
         */
        public boolean isFloating() {
            return floating;
        }

        /**
         * 设置是否浮动。
         *
         * @param floating true 表示浮动
         */
        public void setFloating(boolean floating) {
            this.floating = floating;
        }
    }

    /**
     * 项目引用。
     */
    public static class ProjectReference {
        private String projectId;
        private String projectName;
        private String filePath;
        private LocalDateTime lastOpenedAt;

        /**
         * 创建项目引用实例。
         *
         * @param projectId   项目 ID
         * @param projectName 项目名称
         * @param filePath    文件路径
         */
        public ProjectReference(String projectId, String projectName, String filePath) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.filePath = filePath;
            this.lastOpenedAt = LocalDateTime.now();
        }

        /**
         * 获取项目 ID。
         *
         * @return 项目 ID
         */
        public String getProjectId() {
            return projectId;
        }

        /**
         * 获取项目名称。
         *
         * @return 项目名称
         */
        public String getProjectName() {
            return projectName;
        }

        /**
         * 设置项目名称。
         *
         * @param projectName 项目名称
         */
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        /**
         * 获取文件路径。
         *
         * @return 文件路径
         */
        public String getFilePath() {
            return filePath;
        }

        /**
         * 设置文件路径。
         *
         * @param filePath 文件路径
         */
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        /**
         * 获取最后打开时间。
         *
         * @return 最后打开时间
         */
        public LocalDateTime getLastOpenedAt() {
            return lastOpenedAt;
        }

        /**
         * 更新最后打开时间。
         */
        public void updateLastOpenedAt() {
            this.lastOpenedAt = LocalDateTime.now();
        }
    }
}
