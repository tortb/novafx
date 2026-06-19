

📘 NovaFX Studio — dev.md（开发总规范）

⸻

1. 项目概述

NovaFX Studio 是一个基于数学函数的三维粒子可视化编辑器。

核心能力：

函数（Function）→ 采样（Sampling）→ 点集（Point Cloud）→ GPU渲染

应用场景：

* 数学可视化
* Minecraft 粒子设计
* 动效生成
* 数学艺术创作

⸻

2. 核心设计原则

2.1 架构原则

必须遵守：

* DDD（领域驱动设计）
* Clean Architecture
* 依赖倒置（DIP）
* UI / Core 完全分离

⸻

2.2 禁止事项

❌ 禁止：

* UI逻辑写进 Core
* 字符串直接 eval
* 全局状态
* 平台路径硬编码
* 单体 God Class

⸻

2.3 核心理念

Function AST is the Source of Truth

⸻

3. 技术栈

Java: 25
Build: Maven Multi-Module
UI: JavaFX
Renderer: LWJGL + OpenGL 3.3+
Math Engine: exp4j (or custom AST)
Serialization: Jackson
Test: JUnit5
Logging: SLF4J

⸻

4. 模块结构

novafx/
├── novafx-core
├── novafx-math
├── novafx-function
├── novafx-sampling
├── novafx-renderer
├── novafx-project
├── novafx-export
├── novafx-ui
├── novafx-desktop

⸻

5. 核心数据模型

5.1 FunctionDefinition

FunctionDefinition {
    String x;
    String y;
    String z;
    double start;
    double end;
    double step;
}

⸻

5.2 Vector3D

record Vector3D(double x, double y, double z) {}

⸻

5.3 Project（.nfx）

NovaProject {
    UUID id;
    String name;
    FunctionDefinition function;
    ParticleSettings particle;
    RenderSettings render;
}

⸻

6. .nfx 文件规范

6.1 定义

.nfx = NovaFX Source Project File

⸻

6.2 格式（JSON）

包含：

* function
* particle
* render
* meta

⸻

6.3 示例

{
  "version": "1.0",
  "meta": {
    "name": "Spiral"
  },
  "function": {
    "x": "cos(t)",
    "y": "sin(t)",
    "z": "t/10"
  }
}

⸻

7. Function System 设计（核心）

7.1 三层函数模型

ExpressionFunction   → sin(t)
CompositeFunction    → sin(t)+sin(2t)
AdvancedFunction     → Fourier / Noise / Curve

⸻

7.2 核心接口

interface Function {
    double evaluate(double t);
}

⸻

7.3 AST原则

❗禁止：

String → eval()

✔必须：

AST → Function → Sampler

⸻

8. Fourier System（扩展）

8.1 定位

Function System 2.0

⸻

8.2 类型

* FourierSeriesFunction
* FFT Analyzer（V2）
* Harmonic Generator

⸻

8.3 模型

class FourierFunction implements Function {
    int harmonics;
    double baseFreq;
    double decay;
}

⸻

9. Sampling System

9.1 接口

List<Vector3D> sample(Function f);

⸻

9.2 策略

* Uniform Sampling
* Adaptive Sampling（未来）

⸻

9.3 性能目标

100k points → 60 FPS
1M points → 30 FPS

⸻

10. 输入系统（重点）

10.1 三种输入模式

1. Natural Input（自然语言）
2. Structured Input（GUI）
3. LaTeX Input

⸻

10.2 统一接口

FunctionParser.parse(FunctionInput input)

⸻

10.3 设计原则

所有输入 → AST

⸻

11. 自动补全系统（IntelliSense）

11.1 能力

* 函数补全（sin/cos）
* 变量补全（t,x,y,z）
* 常量补全（PI/E）
* Snippet补全（模板）

⸻

11.2 接口

CompletionEngine.suggest(context)

⸻

11.3 排序规则

1. 使用频率
2. 上下文匹配
3. 最近使用

⸻

12. 可视化调参系统（Parameter System）

12.1 核心能力

x = a * sin(t)
→ a slider

⸻

12.2 参数模型

Parameter {
    String name;
    double value;
}

⸻

12.3 自动提取

x = a * sin(b * t)
→ a, b 自动识别

⸻

12.4 UI行为

Slider → Recompute → GPU Update

⸻

12.5 性能要求

UI响应 < 16ms

⸻

13. Renderer System

13.1 技术

* OpenGL 3.3+
* VBO 重用
* GPU point cloud

⸻

13.2 渲染目标

10万点：60 FPS
100万点：30 FPS

⸻

14. Export System

支持：

* .nfx（源文件）
* MCFunction
* JSON
* CSV

⸻

15. 插件系统（预留）

15.1 格式

.nfxp

⸻

15.2 能力

* function扩展
* particle扩展
* exporter扩展

⸻

15.3 隔离

* ClassLoader隔离
* 生命周期管理

⸻

16. 性能与线程模型

16.1 线程分离

UI Thread
Render Thread
Compute Thread

⸻

16.2 异步计算

CompletableFuture<List<Vector3D>>

⸻

17. 架构总图

Input System
    ↓
Function Parser
    ↓
AST Function Engine
    ↓
Sampling Engine
    ↓
Particle Engine
    ↓
OpenGL Renderer

⸻

18. 核心哲学

所有复杂性都应停留在 Core 层
UI 永远只是“观察器”

⸻

19. V1开发范围（强制）

必须实现：

* Function AST
* Sampling Engine
* OpenGL渲染点云
* .nfx 保存/加载
* 基础UI编辑器

⸻

禁止实现：

* AI生成函数
* FFT分析UI
* 插件系统完整实现
* Android / Web

⸻

20. 一句话定义项目

NovaFX = 可扩展数学函数 → 三维粒子系统引擎

⸻
