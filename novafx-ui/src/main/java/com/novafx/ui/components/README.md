# NovaFX UI Kit v2 组件库

## 概述

NovaFX UI Kit v2 是一个工业级桌面 UI 系统，提供统一的视觉、组件、布局、动画和工作区管理系统。

## 组件列表

### 基础组件 (v1)

| 组件 | 类名 | 说明 | CSS 类 |
|------|------|------|--------|
| 面板基类 | `FXPanel` | 所有面板组件的基础类 | `.fx-panel` |
| 卡片组件 | `Card` | Explorer 等场景的卡片式布局 | `.fx-card` |
| 按钮组件 | `FXButton` | 统一的按钮样式 | `.fx-button` |
| 输入框组件 | `FXInput` | 统一的输入框样式 | `.fx-input` |

### 布局系统 (v1)

| 组件 | 类名 | 说明 |
|------|------|------|
| DockPane | `DockPane` | VSCode 风格的 Dock 式布局 |
| CollapsiblePanel | `CollapsiblePanel` | 可折叠面板组件 |

### 动画系统 (v1)

| 组件 | 类名 | 说明 |
|------|------|------|
| FXAnim | `FXAnim` | 统一的动画工具类 |

### 绑定系统 (v1)

| 组件 | 类名 | 说明 |
|------|------|------|
| UIBinder | `UIBinder` | State → UI 的统一绑定机制 |

### Dock 拖拽系统 (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| DraggableDockPane | `DraggableDockPane` | 支持拖拽的 DockPane |
| DockDropZone | `DockDropZone` | 停靠区域指示器 |

### 面板自由布局 (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| FloatingPanel | `FloatingPanel` | 可自由拖拽的浮动面板 |
| DockManager | `DockManager` | 面板停靠管理器 |

### Tab 系统 (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| TabbedPanel | `TabbedPanel` | Tab 标签页面板 |
| TabHeader | `TabHeader` | Tab 头部组件 |

### 多窗口支持 (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| DetachablePanel | `DetachablePanel` | 可分离到独立窗口的面板 |
| WindowManager | `WindowManager` | 窗口管理器 |

### Workspace 系统 (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| Workspace | `Workspace` | 工作区 |
| WorkspaceManager | `WorkspaceManager` | 工作区管理器 |

### Layout Persistence (v2)

| 组件 | 类名 | 说明 |
|------|------|------|
| LayoutSerializer | `LayoutSerializer` | 布局序列化器 |
| LayoutConfig | `LayoutConfig` | 布局配置 |

## 使用示例

### DraggableDockPane

```java
DraggableDockPane dock = new DraggableDockPane();
dock.setDockLeft(explorer);
dock.setDockCenter(canvas);
dock.setDockRight(inspector);
dock.setDockBottom(inputPanel);

// 为面板启用拖拽
dock.enableDrag(explorer);
```

### FloatingPanel

```java
FloatingPanel panel = new FloatingPanel("My Panel");
panel.getChildren().add(new Label("Floating Content"));

// 停靠到左侧
panel.dock(DraggableDockPane.DockPosition.LEFT);

// 取消停靠
panel.undock();
```

### DockManager

```java
DockManager manager = new DockManager(dockPane);
manager.addPanel(explorer, DockPosition.LEFT);
manager.addPanel(inspector, DockPosition.RIGHT);
manager.addPanel(console, DockPosition.BOTTOM);
```

### TabbedPanel

```java
TabbedPanel tabbedPanel = new TabbedPanel();
tabbedPanel.addTab("Tab 1", new Label("Content 1"));
tabbedPanel.addTab("Tab 2", new Label("Content 2"));
tabbedPanel.selectTab(0);
```

### DetachablePanel

```java
DetachablePanel panel = new DetachablePanel("Detachable");
panel.getChildren().add(new Label("Detachable Content"));

// 分离到独立窗口
panel.detach();

// 重新附加
panel.attach();
```

### WindowManager

```java
WindowManager windowManager = new WindowManager();
windowManager.setMainWindow(primaryStage);
windowManager.addWindow(detachedStage, "Detached Panel");

// 获取焦点窗口
Stage focused = windowManager.getFocusedWindow();
```

### Workspace

```java
Workspace workspace = new Workspace("My Workspace");
workspace.setDescription("A workspace for my projects");
workspace.addProject(new ProjectReference("project1", "Project 1", "/path/to/project"));

// 添加面板布局
workspace.addPanelLayout("explorer", new Workspace.PanelLayout());
```

### WorkspaceManager

```java
WorkspaceManager workspaceManager = new WorkspaceManager();
Workspace workspace = workspaceManager.createWorkspace("My Workspace");
workspaceManager.switchWorkspace(workspace);

// 保存和加载
workspaceManager.saveWorkspace(workspace);
workspaceManager.loadWorkspace(workspace);
```

### LayoutSerializer

```java
LayoutSerializer serializer = new LayoutSerializer();
LayoutSerializer.LayoutConfig config = new LayoutSerializer.LayoutConfig();
config.setWindowWidth(1400);
config.setWindowHeight(850);

// 保存布局
serializer.saveToFile(config, "default");

// 加载布局
LayoutSerializer.LayoutConfig loaded = serializer.loadFromFile("default");
```

## 样式规范

### 圆角系统

- Input: 12px
- Panel: 12px
- Card: 10px
- Button: 10px

### 间距系统

- 8px / 12px / 16px / 24px / 32px

### 颜色系统

- 背景: #0A0A0A
- 面板: #111111
- 边框: #2A2A2A
- 强调色: #A855F7 (紫色)

## 设计原则

所有 UI 必须：
1. 可复用
2. 可动画
3. 可绑定 State
4. 不直接操作数据
5. 支持拖拽和停靠
6. 支持多窗口
7. 支持工作区持久化
