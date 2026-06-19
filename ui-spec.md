对于 NovaFX，我不建议做传统的“工具软件 UI”（一堆按钮和面板），而应该做：

GitHub + Figma + Blender + Desmos 的混合风格

这样既符合开发者审美，又适合数学可视化创作。

⸻

NovaFX Studio UI SPEC v1.0

设计关键词

Professional
Developer-first
Minimal
Dark
Creative
Realtime

参考：

* Visual Studio Code
* Blender
* Figma
* Desmos

⸻

1. 设计系统

主题

默认：

Theme: Dark

⸻

色板

Background

#0A0A0A

主背景

⸻

Surface

#111111

面板

⸻

Surface Hover

#1A1A1A

⸻

Border

#262626

⸻

Text Primary

#FFFFFF

⸻

Text Secondary

#A1A1AA

⸻

Brand Color

Nova Orange

#F97316

⸻

Success

#22C55E

⸻

Warning

#EAB308

⸻

Error

#EF4444

⸻

2. 应用布局

整体：

┌────────────────────────────────────────────┐
│ Top Bar                                    │
├───────┬──────────────────────┬─────────────┤
│       │                      │             │
│       │                      │             │
│ Left  │      Viewport        │ Right       │
│ Dock  │      OpenGL          │ Inspector   │
│       │                      │             │
│       │                      │             │
├───────┴──────────────────────┴─────────────┤
│ Bottom Console / Timeline                 │
└────────────────────────────────────────────┘

类似：

Blender
+
VSCode

⸻

3. 顶部导航栏(默认使用中文)

高度：

48px

⸻

布局：

NovaFX
File
Edit
View
Insert
Function
Tools
Plugin
Help

⸻

右侧：

FPS
Points
GPU
Theme
Settings

示例：

FPS 144
Points 100,000
GPU RTX 5070

⸻

4. 左侧 Dock

宽度：

280px

可折叠

⸻

Tab 1

Explorer

📁 Project

显示：

Heart.nfx
Functions
Particles
Materials
Presets
Exports

⸻

Tab 2

Presets

分类：

Basic
Geometry
Fourier
Fractal
Minecraft
Scientific

⸻

Tab 3

Assets

未来：

Thumbnail
Texture
Icons

⸻

5. 中央 Viewport

这是核心。

占：

60%-70%

⸻

默认显示

Grid
Axis
Camera Gizmo

⸻

Camera

支持：

Rotate
Pan
Zoom

Blender风格：

MMB Rotate
Shift+MMB Pan
Wheel Zoom

⸻

右上角

Perspective
Top
Front
Left

⸻

FPS Overlay

FPS
Points
Frame Time

⸻

6. 函数编辑器（最核心）

位置：

Viewport 左下角浮动

类似：

Desmos

⸻

布局：

X(t) [________________]
Y(t) [________________]
Z(t) [________________]

示例：

cos(t)
sin(t)
t / 10

⸻

按钮：

Preview
Compile
Sample
Reset

⸻

7. 输入模式切换

顶部：

Natural
Structured
LaTeX

切换器：

[ Natural | Structured | LaTeX ]

⸻

8. IntelliSense UI

输入：

si

弹出：

sin(t)
sinh(t)
sign(x)

⸻

样式：

VSCode 风格

支持：

键盘导航
Tab补全
Enter补全

⸻

9. 参数面板

右侧：

Parameters

自动生成：

a [-----●----]
b [--●-------]
freq [----●---]

⸻

数值实时显示：

a = 2.35

⸻

支持：

Slider
Number
Toggle
Color

⸻

10. Inspector

右侧主区域

宽度：

320px

⸻

Tab：

Particle
Render
Function
Project

⸻

Particle

Size
Density
Lifetime
Color

⸻

Render

Grid
Axis
Bloom
Background

⸻

Function

Range
Step
Precision

⸻

11. 底部面板

高度：

220px

⸻

Tabs：

Console
Compile
Performance
Export

⸻

Console

[INFO]
Sample Complete
100000 Points

⸻

Compile

AST Generated

⸻

Performance

FPS
VRAM
CPU
RAM

⸻

12. 时间轴（V2）

底部切换：

Timeline

类似：

Blender

⸻

支持：

Keyframe
Animation
Parameter Curves

⸻

13. 插件面板

右侧新增：

Plugins

显示：

Installed
Marketplace
Updates

⸻

14. Command Palette

快捷键：

Ctrl+Shift+P

弹出：

Create Spiral
Create Heart
Export NFX
Import Preset

类似 VSCode。

⸻

15. 欢迎页（首次启动）

顶部 Logo：

NovaFX

中央 Hero：

Mathematics Meets Particles

按钮：

New Project
Open Project
Examples
Documentation

⸻

16. UI 动效规范

持续时间：

150ms

⸻

Hover：

scale(1.02)

⸻

Panel：

fade
slide

⸻

禁止：

花里胡哨动画
玻璃拟态
重阴影

⸻

17. 最终视觉定位

NovaFX 应该看起来像：

40% Blender
30% VSCode
20% Figma
10% Desmos

而不是：

Minecraft Mod

或者：

传统 Java Swing 工具

最终用户打开 NovaFX 的第一感觉应该是：

“这是一个专业级创作软件，而不是一个数学实验项目。”