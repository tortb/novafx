package com.novafx.ui.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 布局序列化器 — UI Kit v2 核心组件。
 * <p>
 * 负责布局配置的序列化和反序列化。
 * 提供布局的持久化存储和恢复机制。
 */
public class LayoutSerializer {

    /** 布局文件扩展名 */
    private static final String LAYOUT_EXT = ".nfxlayout";

    /** 布局存储目录 */
    private static final String LAYOUT_DIR = ".novafx/layouts";

    /**
     * 创建布局序列化器实例。
     */
    public LayoutSerializer() {
        ensureLayoutDirectory();
    }

    /**
     * 确保布局目录存在。
     */
    private void ensureLayoutDirectory() {
        try {
            Path layoutDir = Paths.get(LAYOUT_DIR);
            if (!Files.exists(layoutDir)) {
                Files.createDirectories(layoutDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 序列化布局配置。
     *
     * @param config 布局配置
     * @return 序列化后的字符串
     */
    public String serialize(LayoutConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // 窗口状态
        sb.append("  \"windowState\": {\n");
        sb.append("    \"x\": ").append(config.getWindowX()).append(",\n");
        sb.append("    \"y\": ").append(config.getWindowY()).append(",\n");
        sb.append("    \"width\": ").append(config.getWindowWidth()).append(",\n");
        sb.append("    \"height\": ").append(config.getWindowHeight()).append(",\n");
        sb.append("    \"maximized\": ").append(config.isMaximized()).append("\n");
        sb.append("  },\n");

        // 面板配置
        sb.append("  \"panels\": {\n");
        boolean first = true;
        for (Map.Entry<String, PanelConfig> entry : config.getPanels().entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("    \"").append(entry.getKey()).append("\": {");
            PanelConfig panel = entry.getValue();
            sb.append("\"x\": ").append(panel.getX());
            sb.append(", \"y\": ").append(panel.getY());
            sb.append(", \"width\": ").append(panel.getWidth());
            sb.append(", \"height\": ").append(panel.getHeight());
            sb.append(", \"visible\": ").append(panel.isVisible());
            sb.append(", \"floating\": ").append(panel.isFloating());
            if (panel.getDockPosition() != null) {
                sb.append(", \"dockPosition\": \"").append(panel.getDockPosition()).append("\"");
            }
            sb.append("}");
        }
        sb.append("\n  },\n");

        // Dock 布局
        sb.append("  \"dockLayout\": {\n");
        sb.append("    \"leftWidth\": ").append(config.getLeftWidth()).append(",\n");
        sb.append("    \"rightWidth\": ").append(config.getRightWidth()).append(",\n");
        sb.append("    \"bottomHeight\": ").append(config.getBottomHeight()).append(",\n");
        sb.append("    \"dividerPositions\": [");
        double[] dividers = config.getDividerPositions();
        for (int i = 0; i < dividers.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(dividers[i]);
        }
        sb.append("]\n");
        sb.append("  }\n");

        sb.append("}");
        return sb.toString();
    }

    /**
     * 反序列化布局配置。
     *
     * @param json JSON 字符串
     * @return 布局配置
     */
    public LayoutConfig deserialize(String json) {
        // 简化实现：实际项目中应使用 JSON 解析库
        // 这里只是示例，不实现完整的 JSON 解析
        return new LayoutConfig();
    }

    /**
     * 保存布局配置到文件。
     *
     * @param config   布局配置
     * @param layoutId 布局 ID
     */
    public void saveToFile(LayoutConfig config, String layoutId) {
        try {
            Path layoutPath = getLayoutPath(layoutId);
            Files.writeString(layoutPath, serialize(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件加载布局配置。
     *
     * @param layoutId 布局 ID
     * @return 布局配置，如果文件不存在则返回默认配置
     */
    public LayoutConfig loadFromFile(String layoutId) {
        try {
            Path layoutPath = getLayoutPath(layoutId);
            if (Files.exists(layoutPath)) {
                String content = Files.readString(layoutPath);
                return deserialize(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LayoutConfig();
    }

    /**
     * 删除布局文件。
     *
     * @param layoutId 布局 ID
     */
    public void deleteFile(String layoutId) {
        try {
            Path layoutPath = getLayoutPath(layoutId);
            if (Files.exists(layoutPath)) {
                Files.delete(layoutPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查布局文件是否存在。
     *
     * @param layoutId 布局 ID
     * @return true 表示存在
     */
    public boolean hasLayout(String layoutId) {
        Path layoutPath = getLayoutPath(layoutId);
        return Files.exists(layoutPath);
    }

    /**
     * 获取布局文件路径。
     *
     * @param layoutId 布局 ID
     * @return 文件路径
     */
    private Path getLayoutPath(String layoutId) {
        return Paths.get(LAYOUT_DIR, layoutId + LAYOUT_EXT);
    }

    /**
     * 布局配置。
     */
    public static class LayoutConfig {
        private double windowX;
        private double windowY;
        private double windowWidth = 1400;
        private double windowHeight = 850;
        private boolean maximized;
        private Map<String, PanelConfig> panels = new HashMap<>();
        private double leftWidth = 220;
        private double rightWidth = 220;
        private double bottomHeight = 200;
        private double[] dividerPositions = {0.7};

        /**
         * 获取窗口 X 坐标。
         *
         * @return X 坐标
         */
        public double getWindowX() {
            return windowX;
        }

        /**
         * 设置窗口 X 坐标。
         *
         * @param windowX X 坐标
         */
        public void setWindowX(double windowX) {
            this.windowX = windowX;
        }

        /**
         * 获取窗口 Y 坐标。
         *
         * @return Y 坐标
         */
        public double getWindowY() {
            return windowY;
        }

        /**
         * 设置窗口 Y 坐标。
         *
         * @param windowY Y 坐标
         */
        public void setWindowY(double windowY) {
            this.windowY = windowY;
        }

        /**
         * 获取窗口宽度。
         *
         * @return 窗口宽度
         */
        public double getWindowWidth() {
            return windowWidth;
        }

        /**
         * 设置窗口宽度。
         *
         * @param windowWidth 窗口宽度
         */
        public void setWindowWidth(double windowWidth) {
            this.windowWidth = windowWidth;
        }

        /**
         * 获取窗口高度。
         *
         * @return 窗口高度
         */
        public double getWindowHeight() {
            return windowHeight;
        }

        /**
         * 设置窗口高度。
         *
         * @param windowHeight 窗口高度
         */
        public void setWindowHeight(double windowHeight) {
            this.windowHeight = windowHeight;
        }

        /**
         * 检查是否最大化。
         *
         * @return true 表示最大化
         */
        public boolean isMaximized() {
            return maximized;
        }

        /**
         * 设置是否最大化。
         *
         * @param maximized true 表示最大化
         */
        public void setMaximized(boolean maximized) {
            this.maximized = maximized;
        }

        /**
         * 获取面板配置。
         *
         * @return 面板配置映射
         */
        public Map<String, PanelConfig> getPanels() {
            return panels;
        }

        /**
         * 添加面板配置。
         *
         * @param panelId 面板 ID
         * @param config  面板配置
         */
        public void addPanel(String panelId, PanelConfig config) {
            panels.put(panelId, config);
        }

        /**
         * 获取面板配置。
         *
         * @param panelId 面板 ID
         * @return 面板配置，如果不存在则返回 null
         */
        public PanelConfig getPanel(String panelId) {
            return panels.get(panelId);
        }

        /**
         * 获取左侧面板宽度。
         *
         * @return 左侧面板宽度
         */
        public double getLeftWidth() {
            return leftWidth;
        }

        /**
         * 设置左侧面板宽度。
         *
         * @param leftWidth 左侧面板宽度
         */
        public void setLeftWidth(double leftWidth) {
            this.leftWidth = leftWidth;
        }

        /**
         * 获取右侧面板宽度。
         *
         * @return 右侧面板宽度
         */
        public double getRightWidth() {
            return rightWidth;
        }

        /**
         * 设置右侧面板宽度。
         *
         * @param rightWidth 右侧面板宽度
         */
        public void setRightWidth(double rightWidth) {
            this.rightWidth = rightWidth;
        }

        /**
         * 获取底部面板高度。
         *
         * @return 底部面板高度
         */
        public double getBottomHeight() {
            return bottomHeight;
        }

        /**
         * 设置底部面板高度。
         *
         * @param bottomHeight 底部面板高度
         */
        public void setBottomHeight(double bottomHeight) {
            this.bottomHeight = bottomHeight;
        }

        /**
         * 获取分割器位置。
         *
         * @return 分割器位置数组
         */
        public double[] getDividerPositions() {
            return dividerPositions;
        }

        /**
         * 设置分割器位置。
         *
         * @param positions 分割器位置数组
         */
        public void setDividerPositions(double[] positions) {
            this.dividerPositions = positions;
        }
    }

    /**
     * 面板配置。
     */
    public static class PanelConfig {
        private double x;
        private double y;
        private double width;
        private double height;
        private boolean visible = true;
        private boolean floating;
        private DraggableDockPane.DockPosition dockPosition;

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
    }
}
