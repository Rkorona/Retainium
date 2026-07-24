# 虚空编年史 — 文字奇幻剧情冒险游戏

## 项目概述

基于 **Material 3 Expressive + Jetpack Compose** 的 Android 文字奇幻冒险游戏。
核心 UI 理念：**活页手稿**（Living Manuscript）—— 整个界面是一本正在被书写的奇幻典籍，
玩家在「阅读」的同时即是在「体验」故事。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3 Expressive（BOM 2026.06.01）
- **最低 SDK**: 36（可无顾虑使用最新 API）
- **目标 SDK**: 37
- **构建工具**: Gradle 9.x with Kotlin DSL

## 项目结构

```
app/src/main/java/io/application/
├── MainActivity.kt               应用入口
├── model/
│   └── GameModels.kt            数据模型（玩家、副本、区域）
└── ui/
    ├── theme/
    │   ├── Color.kt             奇幻暗色调色板
    │   ├── Theme.kt             Material3 暗系主题
    │   └── Type.kt             衬线/无衬线混排字体系统
    └── lobby/
        ├── LobbyScreen.kt       大厅主屏幕（活页手稿布局）
        └── components/
            ├── InkWritingText.kt      墨水书写动画文字
            ├── ParchmentBackground.kt 多层 Canvas 羊皮纸背景
            ├── RuneParticles.kt       环境符文粒子效果
            ├── PlayerStatusCard.kt    玩家状态卡（弹簧动画进度条）
            ├── DungeonCard.kt         副本卡片（Canvas 插图 + 弹簧按压）
            └── SectionDivider.kt      金色分割线装饰
```

## 大厅设计理念

「活页手稿」—— 区别于所有千篇一律的竖排菜单布局：

- **进场动画**：世界标题和章节名以墨水书写方式逐字浮现
- **背景**：多层 Canvas 绘制的羊皮纸质感（暖光晕 + vignette + 纸纹）
- **环境**：18 个符文粒子在屏幕上缓慢漂浮旋转（古北欧符文 Unicode）
- **副本卡片**：每张卡片有独特的 Canvas 场景剪影（森林/深渊/神殿/废墟/冰峰），
  配场景微动画（树摇/触手/极光/飞雪/烛光）
- **按压反馈**：M3 Expressive 弹簧物理缩放（DampingRatioMediumBouncy）
- **进度条**：弹簧动画驱动，有高光层叠效果
- **底部导航**：手稿风格，选中项有顶部光条 + 金色发光

## Material 3 Expressive 特性使用清单

| 特性 | 使用位置 |
|------|---------|
| `Spring.DampingRatioMediumBouncy` | 副本卡片按压缩放、HP/MP 进度条 |
| `Spring.StiffnessVeryLow` | 章节进度条入场 |
| `infiniteRepeatable + EaseInOutSine` | 符印呼吸光晕、副本场景动画 |
| `animateFloatAsState` (spring) | 所有状态过渡 |
| `Canvas drawPath/drawCircle/Brush` | 羊皮纸背景、场景插图、进度条 |
| `systemBarsPadding / navigationBarsPadding` | 全屏 edge-to-edge |

## 如何构建

在 Android Studio 中打开项目，连接设备或启动模拟器（Android 16+），点击 Run。

Replit 环境不运行 Android 构建，代码编辑后需在本地 Android Studio 中编译预览。

## User preferences

- 游戏风格：文字奇幻剧情冒险
- UI 风格：活页手稿，拒绝千篇一律的竖排菜单
- 充分利用 M3 Expressive 的弹簧动画和 Canvas 能力
- 始终使用暗色主题（符合游戏沉浸感）
