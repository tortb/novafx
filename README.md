# NovaFX Studio

**数学粒子编辑器 — 适用于 Minecraft**

NovaFX Studio 是一款面向 Minecraft 粒子的参数化数学建模 IDE。  
它让你用数学表达式生成 3D 点云，预览、调整并导出为 Minecraft 可用的数据格式。

```
       ┌────────────────────────────────────────────┐
       │  菜单栏                                      │
  ┌────┼──────────────────────┬──────────────────────┤
  │ 工  │                      │  属性                │
  │ 程  │     3D 视口          │  参数                │
  │ 探  │                      │                      │
  │ 索  ├──────────────────────┴──────────────────────┤
  │     │  函数编辑器（简易 / 专业 / LaTeX）            │
  └─────┴─────────────────────────────────────────────┘
```

---

##  ✨ 特性

### Project-based Math IDE
- **工程资源管理器** — 类 VSCode 的左侧面板，自动从 `.nfx` 文件推导树结构
- **多项目管理** — 同时打开多个 `.nfx` 工程，快速切换
- **节点导航** — 单击选中、双击编辑、右键菜单、Ctrl+P 快速搜索

### 参数化 3D 建模
- 三轴独立表达式：`x(t)` `y(t)` `z(t)`
- 自定义参数滑块 — 表达式中的变量（如 `a*sin(b*t)` 中的 `a`, `b`）自动提取为可调参数
- 即时预览 — 修改表达式或参数后立即重采样
- 三种编辑模式：简易（符号面板）、专业（代码风格 + 自动补全）、LaTeX

### 3D 视口
- 基于 LWJGL 的 OpenGL 渲染
- 自由视角摄像机（拖拽旋转 / 滚轮缩放）
- 网格 / 轴线 / 粒子大小 / 颜色 可调

### 导出格式
| 格式 | 用途 |
|------|------|
| CSV | 通用点云数据 |
| JSON | Web 可视化 / 外部工具 |
| MCFunction | Minecraft 函数（`particle` 命令序列） |
| NFXC | 编译二进制（快速加载） |

---

##  🚀 快速开始

### 构建

```bash
mvn install -DskipTests
```

### 运行

```bash
mvn package -pl novafx-desktop -DskipTests -q
java -jar novafx-desktop/target/novafx-desktop-1.0.6.jar
```

### 系统要求

- **Java** 21+
- **Maven** 3.9+
- **GPU** 支持 OpenGL 3.3+

---

##  📂 .nfx 文件格式

NovaFX 使用 TOML 格式存储工程。示例：

```toml
version = "1.0"

[project]
id = "a1b2c3d4-e5f6-..."

[meta]
name = "Heart"
author = ""

[function]
x = "16*sin(t)^3"
y = "13*cos(t) - 5*cos(2*t) - 2*cos(3*t) - cos(4*t)"
z = "0"
start = 0
end = 6.283
step = 0.05

[particle]
size = 2.0
density = 1.0

[render]
grid = true
axis = true
```

工程树由文件数据 **自动推导**，无需手写 `[structure]` 节。

---

##  🧭 用户指南

### 左侧 — 工程资源管理器

| 操作 | 效果 |
|------|------|
| 单击节点 | 选中高亮（左侧橙色边框） |
| 双击 x(t)/y(t)/z(t) | 切换到专业编辑器并聚焦对应表达式 |
| 双击参数 | 跳转到参数面板并高亮对应滑块 |
| 右键工程 | 保存 / 关闭 / 重命名 |
| 右键表达式 | 复制表达式 |
| 右键参数 | 编辑 / 删除 |
| 右键预设 | 浏览预设 |
| 拖拽节点 | 触发重排（预留） |
| `Ctrl+P` | 打开命令面板（搜索节点 / 命令） |

### 右侧 — 属性 + 参数

- **属性面板** — 粒子大小、颜色、网格显隐
- **参数面板** — 表达式中提取的可调变量，拖动滑块实时重采样

### 菜单栏

| 菜单 | 快捷键 | 功能 |
|------|--------|------|
| 文件 > 新建工程 | `Ctrl+N` | 从模板创建新工程 |
| 文件 > 打开工程 | `Ctrl+O` | 加载 `.nfx` 文件 |
| 文件 > 保存 | `Ctrl+S` | 保存当前工程 |
| 文件 > 另存为 | `Ctrl+Shift+S` | 另存为... |
| 文件 > 编译工程 | `Ctrl+B` | 编译为 `.nfxc` 二进制格式 |
| 视图 > 重置视角 | — | 重置 3D 摄像机 |

---

##  🏗️ 项目架构

```
novafx/
├── pom.xml                    # 父 POM (多模块)
├── novafx-core/               # 领域层
│   └── workspace/             #   ├─ 工程模型 AST
│   └── domain/                #   └─ Project, Preset, PlatformService
├── novafx-math/               # 数学引擎
│   └── FunctionDefinition     #   └─ 参数化函数定义 + 编译
├── novafx-function/           # 函数解析
│   └── CompiledFunction       #   └─ AST 解析 / 求值 / 自动补全
├── novafx-project/            # 基础设施
│   ├── io/                    #   ├─ NfxReader / NfxWriter (.nfx)
│   ├── repository/            #   └─ ProjectRepository
│   └── workspace/             #   └─ WorkspaceLoader
├── novafx-renderer/           # 3D 渲染
│   └── RenderEngine           #   └─ LWJGL OpenGL 渲染器
├── novafx-export/             # 导出
│   └── CsvExporter / JsonExporter / McFunctionExporter
├── novafx-ui/                 # JavaFX UI
│   ├── view/                  #   ├─ MainWindow, ProjectExplorer
│   ├── editor/                #   ├─ SimpleMathEditor, ExpressionEditor
│   ├── controller/            #   └─ MainController
│   └── i18n/                  #   └─ 国际化 (zh_CN / en)
├── novafx-desktop/            # 入口 + fat JAR 打包
```

### 数据流

```
.nfx 文件 (TOML)
    ↓
NfxReader
    ↓
Project (文件格式模型)
    ↓
ProjectRepositoryImpl
    ↓
Project (领域聚合根)
    ↓
ProjectTreeModel.from()   ← 自动推导树结构
    ↓
ProjectExplorer (UI 树)
```

---

##  🌐 国际化

默认语言为简体中文（`zh_CN`）。  
在配置目录下创建 `locale.conf` 文件，内容为 `en_US` 可切换到英文。

配置文件位置（取决于操作系统）：
- Linux: `~/.config/NovaFX/locale.conf`
- Windows: `%APPDATA%/NovaFX/locale.conf`
- macOS: `~/Library/Application Support/NovaFX/locale.conf`

---

##  ⚙️ 开发

### 构建全部模块

```bash
mvn clean install
```

### 运行测试

```bash
mvn test
# 输出示例: Tests run: 312, Failures: 0, Errors: 0
```

### 项目结构约定

| 模块 | 职责 | 依赖 |
|------|------|------|
| `novafx-function` | 表达式解析 / 求值 / 补全 | 无 |
| `novafx-math` | FunctionDefinition / 预设 | function |
| `novafx-core` | 领域模型 (Project, Workspace) | math |
| `novafx-project` | 文件 I/O / 仓库 / 编译 | core |
| `novafx-renderer` | 3D 渲染 | math |
| `novafx-export` | 导出工具 | core |
| `novafx-ui` | JavaFX 界面 | 所有模块 |
| `novafx-desktop` | 启动器 + 打包 | ui |

---

##  📄 许可证

MIT License

---

*NovaFX Studio — 把数学变成粒子*
