# 界隙书房

这是一个基于 Android、Kotlin、Jetpack Compose 和 Material 3 的文字奇幻剧情冒险游戏原型。

## 当前体验

- 主入口是“界隙书房”大厅，而不是传统功能菜单。
- 大厅将“活着的书房”和“心智剧场”结合：中央记忆核心、雾中剧情入口、余响、行囊与誓言都可交互。
- 交互重点是动态呼吸、漂浮、展开、仪式化进入下一幕，以及移动端触控友好的大触控区域。
- 主题固定为深夜墨色、琥珀火光和雾白文字，避免系统动态颜色破坏叙事氛围。
- 第一阶段已完成：`GameState` 统一保存大厅数据，`GameAction` + reducer 统一处理遗物、余响和誓言操作。

## 代码结构

- `app/src/main/java/io/application/MainActivity.kt`：唯一 Android 入口，只负责生命周期、窗口设置和启动 `GameApp`。
- `app/src/main/java/io/application/game/`：游戏状态、领域模型和状态更新动作。
- `app/src/main/java/io/application/ui/GameApp.kt`：应用级 Compose 容器和当前游戏会话。
- `app/src/main/java/io/application/ui/hall/`：大厅页面、场景、组件和面板。
- `app/src/main/java/io/application/ui/theme/`：Material 3 颜色、主题和排版。
- `app/src/test/java/io/application/game/`：游戏状态 reducer 单元测试。

完整阶段计划保存在根目录的 `开发计划.md`。

## 运行

这是 Android Gradle 项目，使用项目内的 Gradle Wrapper：

```bash
./gradlew :app:assembleDebug
```

在 Android Studio 中打开根目录即可运行 `app` 模块。项目当前没有 Replit Web 工作流；它是原生 Android 应用，不是浏览器应用。

## 设计约定

- 优先让大厅表达世界状态，再暴露系统功能。
- 文字是场景的一部分：余响、誓言和副本入口都使用叙事化文案。
- 后续接入真实剧情数据时，保留 `HallPanel` 的“渐进展开”交互，不要退回卡片网格首页。