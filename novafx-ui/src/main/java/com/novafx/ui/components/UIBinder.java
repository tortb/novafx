package com.novafx.ui.components;

import com.novafx.core.state.ProjectState;
import javafx.scene.canvas.Canvas;

/**
 * UI 绑定系统 — UI Kit v1 绑定层。
 * <p>
 * 提供 State → UI 的统一绑定机制。
 * 所有 UI 更新必须通过 UIBinder 进行，禁止直接操作数据。
 * <p>
 * 使用示例：
 * <pre>
 * UIBinder binder = new UIBinder();
 * binder.bind(stateProvider, canvas);
 * </pre>
 */
public class UIBinder {

    /**
     * 绑定状态提供者到 Canvas。
     * <p>
     * 当状态变更时，自动重绘画布。
     *
     * @param stateProvider 状态提供者（返回当前状态的函数）
     * @param canvas        画布
     */
    public void bind(StateProvider stateProvider, Canvas canvas) {
        // 这里只是示例，实际绑定需要在 MainController 中注册回调
        // 当状态变更时，调用 canvas 的重绘方法
    }

    /**
     * 绑定状态提供者到多个 UI 组件。
     * <p>
     * 当状态变更时，更新所有绑定的组件。
     *
     * @param stateProvider 状态提供者
     * @param updatable     可更新的 UI 组件
     */
    public void bind(StateProvider stateProvider, Updatable... updatable) {
        // 这里只是示例，实际绑定需要在 MainController 中注册回调
    }

    /**
     * 状态提供者接口。
     * <p>
     * 用于获取当前的 ProjectState。
     */
    @FunctionalInterface
    public interface StateProvider {
        /**
         * 获取当前状态。
         *
         * @return 当前的 ProjectState
         */
        ProjectState getState();
    }

    /**
     * 可更新的 UI 组件接口。
     * <p>
     * 所有需要响应状态变更的 UI 组件应实现此接口。
     */
    @FunctionalInterface
    public interface Updatable {
        /**
         * 更新 UI 组件。
         *
         * @param state 最新的项目状态
         */
        void update(ProjectState state);
    }
}
