下面这份已经是可以直接放到仓库根目录 /docs/spec.md 的版本，偏向产品 + 架构 + 开发规范，适合后续让 AI Agent、Claude Code、Cursor、Copilot、OpenHands 等直接按规范开发。

⸻

NovaFX Studio Specification

Version: 1.0

Status: Draft

Author: tortb

Language: Java 25

Build System: Maven

License: MIT

⸻

1. Project Vision

NovaFX Studio 是一个跨平台数学函数粒子创作平台。

核心理念：

Mathematics → Function → Sampling → Particle → Visualization

用户可以通过：

* 数学表达式
* 参数方程
* Fourier 系统
* 曲线系统
* 自然语言
* LaTeX

生成三维粒子效果。

⸻

2. Product Positioning

NovaFX 不仅是：

Particle Editor

而是：

Interactive Mathematical DSL IDE

即：

数学表达统一编译器
+
实时可视化粒子系统
+
可扩展创作平台

⸻

3. Supported Platforms

Desktop

必须支持：

Windows 10+
Windows 11
Ubuntu 24+
Debian 12+
Arch Linux
Fedora
macOS 13+

⸻

Architecture

x86_64
ARM64

包括：

Apple Silicon
Raspberry Pi 5

⸻

4. Technology Stack

Java: 25
Build:
  - Maven
UI:
  - JavaFX
Renderer:
  - LWJGL 3
  - OpenGL 3.3+
Serialization:
  - Jackson
Logging:
  - SLF4J
Testing:
  - JUnit5

⸻

5. Architecture

UI Layer
    ↓
Parser Layer
    ↓
Function Engine
    ↓
Sampling Engine
    ↓
Particle Engine
    ↓
Renderer

⸻

6. Project Structure

novafx/
├─ novafx-core
├─ novafx-function
├─ novafx-function-advanced
├─ novafx-parser
├─ novafx-sampling
├─ novafx-renderer
├─ novafx-project
├─ novafx-export
├─ novafx-plugin
├─ novafx-ui
├─ novafx-desktop

⸻

7. File System

NovaFX 使用四种核心格式。

⸻

7.1 .nfx

工程源文件。

NovaFX Source Project

特点：

JSON
Git友好
AI友好
可读
可扩展

⸻

Example

{
  "version":"1.0",
  "meta":{
    "name":"Heart"
  }
}

⸻

7.2 .nfxz

压缩工程包。

本质：

.nfx + assets

内部：

project.nfx
thumbnail.png
preview.mp4
readme.md

实现：

ZipOutputStream

⸻

7.3 .nfxbin

高性能运行格式。

目标：

100k points instant load
1M points < 50ms

结构：

HEADER
VERSION
FUNCTION BYTECODE
SAMPLING DATA
PARTICLE DATA

技术：

ByteBuffer
FlatBuffers

⸻

7.4 .nfxp

插件包格式。

结构：

manifest.json
plugin.jar
assets/

⸻

8. Function System

Function System 是 NovaFX 核心。

⸻

8.1 Design Principle

禁止：

String -> eval()

必须：

AST
 ↓
Evaluator
 ↓
Function

⸻

8.2 Base Interface

public interface Function {
    double evaluate(double t);
}

⸻

8.3 Function Types

Expression Function

sin(t)
cos(t)
sqrt(t)

⸻

Composite Function

sin(t)+sin(2t)

⸻

Fourier Function

class FourierFunction

支持：

harmonics
baseFrequency
decay
phase

⸻

Noise Function

未来版本：

Perlin
Simplex
FBM

⸻

Curve Function

未来版本：

Bezier
Catmull-Rom
Spline

⸻

9. Fourier System

V1

支持：

Fourier Series Generator

⸻

公式：

Σ an sin(nωt + φn)

⸻

V2

支持：

FFT Analyzer
Spectrum Viewer

⸻

10. Sampling System

Responsibilities

负责：

Function -> PointCloud

⸻

Interface

List<Vector3D> sample();

⸻

Sampling Modes

Uniform

V1

⸻

Adaptive

V2

⸻

Performance Target

100000 points:
  60 FPS
1000000 points:
  30 FPS

⸻

11. Input System

NovaFX 必须支持三种输入模式。

⸻

Natural Input

示例：

螺旋上升
爱心曲线
波浪

流程：

Text
 ↓
Template
 ↓
AST

⸻

Structured Input

类似：

X = cos(t)
Y = sin(t)
Z = t

GUI编辑。

⸻

LaTeX Input

支持：

x(t)=\cos(t)
\sum_{n=1}^{10}\sin(nt)

流程：

LaTeX
 ↓
Parser
 ↓
AST

⸻

12. IntelliSense System

自动补全系统。

⸻

Support

Functions

sin
cos
tan
exp
log
sqrt

⸻

Variables

t
x
y
z

⸻

Constants

PI
E

⸻

Snippets

Spiral
Heart
Wave
Fourier

⸻

Completion Engine

CompletionEngine

⸻

Ranking

Usage History
Context Match
Recent Usage

⸻

13. Parameter System

Goal

自动识别参数。

输入：

a*sin(b*t)

自动生成：

a slider
b slider

⸻

Parameter Interface

interface Parameter

⸻

Supported Controls

Slider
Number Input
Color Picker
Checkbox
Dropdown

⸻

Real-time Update

Parameter Change
 ↓
Recompute
 ↓
GPU Update

目标：

<16ms

⸻

14. Renderer

Backend

OpenGL: 3.3+

⸻

Features

V1

Point Cloud
Grid
Axis
Camera
Selection

⸻

V2

Particle Animation
Post Processing
Bloom
HDR

⸻

Optimization

VBO Reuse
Instancing
Frustum Culling

⸻

15. Plugin System

NovaFX 必须支持平台化扩展。

⸻

Plugin API

public interface NovaFXPlugin {
    void onLoad(PluginContext ctx);
    void onEnable();
    void onDisable();
}

⸻

Plugin Context

支持注册：

Function
Particle
Renderer
Exporter
UI Panel

⸻

Isolation

必须：

ClassLoader Isolation

避免：

Dependency Conflict
Memory Leak

⸻

16. Export System

支持导出：

.nfx
.nfxz
.nfxbin
JSON
CSV
Minecraft Function
Minecraft Particle Command

⸻

17. Thread Model

UI Thread
Render Thread
Compute Thread

⸻

禁止：

Render Thread Block UI

⸻

18. Security

禁止：

Runtime Java Execution
Reflection Injection
Unsafe Plugin Loading

⸻

插件必须：

Manifest Validation
Signature Reserved
Sandbox Ready

⸻

19. V1 Scope

必须完成：

* Function Engine
* AST Parser
* Sampling Engine
* OpenGL Renderer
* .nfx Project
* IntelliSense
* Parameter System
* Linux Support
* Windows Support
* macOS Support

⸻

20. V2 Scope

计划实现：

* Fourier Analyzer
* Timeline Animation
* AI Function Generator
* Plugin Marketplace
* Web Preview
* Android Viewer

⸻

25. Final Definition

NovaFX Studio
An extensible mathematical particle creation platform
powered by a unified function compiler,
real-time visualization engine,
and plugin ecosystem.

这个版本已经足够作为仓库里的 SPEC.md 主文档，后续再拆分成：

docs/
├── SPEC.md
├── ARCHITECTURE.md
├── FILE_FORMAT.md
├── PLUGIN_API.md
├── UI_SPEC.md
├── ROADMAP.md

让 AI 按模块开发，而不是让所有内容都堆在一个文件里。