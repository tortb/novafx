package com.novafx.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * DockPane 布局系统 — UI Kit v1 核心布局组件。
 * <p>
 * 提供 VSCode 风格的 Dock 式布局，支持四个停靠区域：
 * <ul>
 *   <li>left — 左侧停靠（如 Explorer）</li>
 *   <li>center — 中心区域（如 Canvas）</li>
 *   <li>right — 右侧停靠（如 Inspector）</li>
 *   <li>bottom — 底部停靠（如 Input Panel）</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * DockPane dock = new DockPane();
 * dock.setDockLeft(explorer);
 * dock.setDockCenter(canvas);
 * dock.setDockRight(inspector);
 * dock.setDockBottom(inputPanel);
 * </pre>
 */
public class DockPane extends BorderPane {

    private Node left;
    private Node center;
    private Node right;
    private Node bottom;

    /**
     * 创建 DockPane 实例。
     */
    public DockPane() {
        getStyleClass().add("dock-pane");
    }

    /**
     * 设置左侧停靠区域。
     *
     * @param node 左侧节点
     */
    public void setDockLeft(Node node) {
        this.left = node;
        setLeft(node);
    }

    /**
     * 获取左侧停靠区域节点。
     *
     * @return 左侧节点，可能为 null
     */
    public Node getDockLeft() {
        return left;
    }

    /**
     * 设置中心区域。
     *
     * @param node 中心节点
     */
    public void setDockCenter(Node node) {
        this.center = node;
        setCenter(node);
    }

    /**
     * 获取中心区域节点。
     *
     * @return 中心节点，可能为 null
     */
    public Node getDockCenter() {
        return center;
    }

    /**
     * 设置右侧停靠区域。
     *
     * @param node 右侧节点
     */
    public void setDockRight(Node node) {
        this.right = node;
        setRight(node);
    }

    /**
     * 获取右侧停靠区域节点。
     *
     * @return 右侧节点，可能为 null
     */
    public Node getDockRight() {
        return right;
    }

    /**
     * 设置底部停靠区域。
     *
     * @param node 底部节点
     */
    public void setDockBottom(Node node) {
        this.bottom = node;
        setBottom(node);
    }

    /**
     * 获取底部停靠区域节点。
     *
     * @return 底部节点，可能为 null
     */
    public Node getDockBottom() {
        return bottom;
    }
}
