# NovaFX Studio Changelog

## [1.0.8] - 2026-06-19

### 🚀 新功能

#### UI Kit v2 - 完整的桌面应用 UI 系统

**Dock 拖拽系统**
- 新增 `DraggableDockPane` - 支持拖拽的 DockPane 布局
- 新增 `DockDropZone` - 停靠区域指示器，提供视觉反馈

**面板自由布局**
- 新增 `FloatingPanel` - 可自由拖拽的浮动面板
- 新增 `DockManager` - 面板停靠管理器，统一管理面板状态

**Tab 系统**
- 新增 `TabbedPanel` - Tab 标签页面板，支持多标签页切换
- 新增 `TabHeader` - Tab 头部组件，支持选中和关闭

**多窗口支持**
- 新增 `DetachablePanel` - 可分离到独立窗口的面板
- 新增 `WindowManager` - 窗口管理器，管理所有应用窗口

**Workspace 系统**
- 新增 `Workspace` - 工作区，管理项目和布局配置
- 新增 `WorkspaceManager` - 工作区管理器，支持工作区的创建、保存、加载和切换

**Layout Persistence**
- 新增 `LayoutSerializer` - 布局序列化器，支持布局的持久化存储
- 新增 `LayoutConfig` - 布局配置，包含窗口状态和面板配置

#### UI Kit v1 - 基础组件库

**基础组件**
- 新增 `FXPanel` - 面板基类，统一的样式和行为
- 新增 `Card` - 卡片组件，用于 Explorer 等场景
- 新增 `FXButton` - 按钮组件，统一的按钮样式
- 新增 `FXInput` - 输入框组件，统一的输入框样式

**布局系统**
- 新增 `DockPane` - VSCode 风格的 Dock 式布局
- 新增 `CollapsiblePanel` - 可折叠面板组件

**动画系统**
- 新增 `FXAnim` - 统一的动画工具类（fadeIn, fadeOut, expand, collapse, scale）

**绑定系统**
- 新增 `UIBinder` - State → UI 的统一绑定机制

### 🎨 样式更新

- 新增 UI Kit v1 组件样式（.fx-panel, .fx-card, .fx-button, .fx-input）
- 新增 UI Kit v2 组件样式（.draggable-dock-pane, .floating-panel, .tabbed-panel 等）
- 统一圆角系统：Input/Panel 12px, Card 10px, Button 10px
- 统一间距系统：8px / 12px / 16px / 24px / 32px
- 统一颜色系统：背景 #0A0A0A, 面板 #111111, 边框 #2A2A2A, 强调色 #A855F7

### 🔧 重构

- 重构 `MainWindow` 使用 `DockPane` 替代 `BorderPane`
- 重构 `TopBar` 使用 `FXButton` 替代普通 `Button`
- 重构 `ExpressionPanel` 使用 `FXInput` 替代普通 `TextField`
- 重构 `ProjectExplorer` 继承 `CollapsiblePanel`
- 重构 `PropertyPanel` 使用 `FXPanel` 作为 Tab 内容
- 重构 `ParameterPanel` 继承 `FXPanel`

### 📁 新增文件

```
novafx-ui/src/main/java/com/novafx/ui/components/
├── Card.java
├── CollapsiblePanel.java
├── DetachablePanel.java
├── DockDropZone.java
├── DockManager.java
├── DockPane.java
├── DraggableDockPane.java
├── FloatingPanel.java
├── FXAnim.java
├── FXButton.java
├── FXInput.java
├── FXPanel.java
├── LayoutSerializer.java
├── TabbedPanel.java
├── TabHeader.java
├── UIBinder.java
├── WindowManager.java
├── Workspace.java
├── WorkspaceManager.java
└── README.md
```

### ✅ 验证

- 编译成功：`mvn clean compile` 无错误
- 测试通过：所有单元测试正常通过
- 组件完整：所有 v1 和 v2 功能都已实现
- 样式统一：所有样式都通过 CSS 类控制

---

## [1.0.7] - 2026-06-18

### 初始版本

- 基础项目结构
- 核心功能实现
- 基本 UI 组件
