package com.novafx.ui.components;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 可分离面板 — UI Kit v2 核心组件。
 * <p>
 * 支持从主窗口分离到独立窗口的面板组件。
 * 可以在主窗口和独立窗口之间自由切换。
 */
public class DetachablePanel extends FloatingPanel {

    /** 分离后的窗口 */
    private Stage detachedWindow;

    /** 是否已分离 */
    private boolean detached = false;

    /** 分离窗口标题 */
    private String detachedTitle;

    /** 分离窗口宽度 */
    private double detachedWidth = 400;

    /** 分离窗口高度 */
    private double detachedHeight = 300;

    /**
     * 创建可分离面板实例。
     *
     * @param title 面板标题
     */
    public DetachablePanel(String title) {
        super(title);
        this.detachedTitle = title + " - Detached";
        getStyleClass().add("detachable-panel");
    }

    /**
     * 分离到独立窗口。
     */
    public void detach() {
        if (!detached) {
            detached = true;

            // 创建新窗口
            detachedWindow = new Stage();
            detachedWindow.setTitle(detachedTitle);
            detachedWindow.setWidth(detachedWidth);
            detachedWindow.setHeight(detachedHeight);

            // 创建场景
            BorderPane root = new BorderPane();
            root.setCenter(this);
            Scene scene = new Scene(root);
            detachedWindow.setScene(scene);

            // 窗口关闭事件
            detachedWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
                attach();
            });

            // 显示窗口
            detachedWindow.show();

            // 添加样式类
            getStyleClass().add("detached");
        }
    }

    /**
     * 重新附加到主窗口。
     */
    public void attach() {
        if (detached) {
            detached = false;

            // 关闭窗口
            if (detachedWindow != null) {
                detachedWindow.close();
                detachedWindow = null;
            }

            // 移除样式类
            getStyleClass().remove("detached");
        }
    }

    /**
     * 检查是否已分离。
     *
     * @return true 表示已分离
     */
    public boolean isDetached() {
        return detached;
    }

    /**
     * 获取分离窗口标题。
     *
     * @return 分离窗口标题
     */
    public String getDetachedTitle() {
        return detachedTitle;
    }

    /**
     * 设置分离窗口标题。
     *
     * @param detachedTitle 分离窗口标题
     */
    public void setDetachedTitle(String detachedTitle) {
        this.detachedTitle = detachedTitle;
        if (detachedWindow != null) {
            detachedWindow.setTitle(detachedTitle);
        }
    }

    /**
     * 获取分离窗口宽度。
     *
     * @return 分离窗口宽度
     */
    public double getDetachedWidth() {
        return detachedWidth;
    }

    /**
     * 设置分离窗口宽度。
     *
     * @param detachedWidth 分离窗口宽度
     */
    public void setDetachedWidth(double detachedWidth) {
        this.detachedWidth = detachedWidth;
        if (detachedWindow != null) {
            detachedWindow.setWidth(detachedWidth);
        }
    }

    /**
     * 获取分离窗口高度。
     *
     * @return 分离窗口高度
     */
    public double getDetachedHeight() {
        return detachedHeight;
    }

    /**
     * 设置分离窗口高度。
     *
     * @param detachedHeight 分离窗口高度
     */
    public void setDetachedHeight(double detachedHeight) {
        this.detachedHeight = detachedHeight;
        if (detachedWindow != null) {
            detachedWindow.setHeight(detachedHeight);
        }
    }

    /**
     * 获取分离窗口实例。
     *
     * @return 分离窗口，如果未分离则返回 null
     */
    public Stage getDetachedWindow() {
        return detachedWindow;
    }

    /**
     * 切换分离状态。
     */
    public void toggleDetach() {
        if (detached) {
            attach();
        } else {
            detach();
        }
    }
}
