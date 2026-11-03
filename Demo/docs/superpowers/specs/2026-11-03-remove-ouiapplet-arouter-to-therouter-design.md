# 设计：移除 OUIApplet + ARouter 迁移到 TheRouter

日期：2026-11-03
项目：OpenIM Android Demo（`/Users/zj/work/IM/Demo`）

## 背景与目标

1. **崩溃背景**：`DemoApplication.initARouter()` 在加载 ARouter 生成类 `ARouter$$Providers$$OUIApplet` 时抛 `HandlerException`（OUIApplet 模块已从 `../OUIKit` 迁入 Demo 根目录且 app 依赖被注释，陈旧生成类与新依赖结构不一致）。
2. **目标**：
   - 彻底移除 OUIApplet 模块（目录 + 构建引用 + UI 入口）。
   - 用 TheRouter 1.3.2 完全替换 ARouter（不保留任何 ARouter 代码/依赖/生成类）。

## 技术选型（已确认）

| 项 | 决定 |
|---|---|
| 路由框架 | TheRouter `cn.therouter:{plugin,router,apt}:1.3.2`（Maven Central 稳定版） |
| 兼容性 | Gradle 7.5.1 / AGP 7.4.2 / Kotlin 1.8.20（无需 AGP8 变体 `cn.therouter.agp8`） |
| 注解处理 | kapt（全部 6 个模块，纯 Java 模块需补 `kotlin-android` 插件；TheRouter 官方要求） |
| 路径格式 | 保留现有 `Routes.java` 常量（`/Xxx/Yyy` 风格），TheRouter 支持任意路径字符串 |

## 一、移除 OUIApplet

| 位置 | 操作 |
|---|---|
| `OUIApplet/` 目录 | 整体删除 |
| `settings.gradle` | 删 `include ':OUIApplet'` |
| `config.gradle` | 删 `applicationId.OUIApplet` 条目 |
| `app/build.gradle:92` | 删注释行 `// implementation project(':OUIApplet')` |
| `MainActivity.java` | 删 `appletFragment` 字段、import；`initDate()` 中整块逻辑；底部菜单 `menus` 数组移除 `view.men3`（:152）、`v == view.men3` 分支（:156）、`view.men3.setOnClickListener`（:167） |
| `app/src/main/res/layout/activity_main.xml:72` | 删 `men3`（小程序入口）RadioButton，底部菜单由 4 tab 变 3 tab |
| `OUICore/.../utils/Routes.java` | 删 `Applet` 内部类（`HOME = "/Applet/AppletFragment"`） |

MainActivity 中 `contactFragment`/`conversationListFragment` 的空判断兜底保留原样（TheRouter `createFragment()` 返回可空，语义一致）。

## 二、TheRouter 迁移

### 2.1 Gradle 变更（6 模块：app / OUICore / OUIConversation / OUIContact / OUIGroup / OUICalling）

- 根 `build.gradle` `buildscript.dependencies`：`classpath 'cn.therouter:plugin:1.3.2'`
- 每个模块：
  - `apply plugin: 'therouter'`（library 模块同样支持）
  - 删 `implementation 'com.alibaba:arouter-api:1.5.2'`、`annotationProcessor 'com.alibaba:arouter-compiler:1.5.2'`、`AROUTER_MODULE_NAME` javaCompileOptions
  - 加 `implementation 'cn.therouter:router:1.3.2'`、`kapt 'cn.therouter:apt:1.3.2'`
  - 无 `kotlin-android` 插件的模块补上（kapt 前置条件）
- 全模块 `clean`，清除陈旧 `ARouter$$` 生成类（崩溃根因之一）。

### 2.2 代码替换规则（~45 个 Java 文件）

| ARouter 用法 | TheRouter 等价物 | 数量 |
|---|---|---|
| `import ...arouter.facade.annotation.Route` + `@Route(path=...)` | import 换为 `com.therouter.router.Route`，注解写法不变 | 23 处 |
| `ARouter.getInstance().build(path).with*(...).navigation(...)` | `TheRouter.build(path).with*(...).navigation(...)`（withString/withBoolean/withInt/navigation(ctx)/navigation(ctx,requestCode) 同名同签名） | ~40 处 |
| `(XxxFragment) ARouter...build(path).navigation()` | `TheRouter.build(path).createFragment()`（返回 `T?`，保留 null 兜底） | MainActivity 2 处 |
| `import ...arouter.launcher.ARouter` | `import com.therouter.TheRouter` | 随调用点 |

### 2.3 跨模块服务（IProvider → ServiceProvider）

- `OUICore/services/CallingService.java`、`IConversationBridge.java`：去掉 `extends IProvider` 与 import。
- `OUICalling/CallingServiceImp.java`、`OUIConversation/IBridgeImpl.java`：
  - `@Route(path = Routes.Service.XXX)` → `@ServiceProvider`（`com.therouter.inject.ServiceProvider`，默认单例，与 ARouter provider 单例语义一致）。
  - 若覆写了 `IProvider.init(Context)`：逻辑迁入自身构造函数或保留方法由注册方显式调用（实施时确认）。
- 6 处调用点：`(CallingService) ARouter...build(Routes.Service.CALLING).navigation()` → `TheRouter.get(CallingService.class)`。

### 2.4 ARouter 内部 API（9 处 LogisticsCenter/Postcard）

需 `Intent` 的 3 处 → `TheRouter.build(path).createIntent(ctx)`（自动填充 component/extras，非 Activity context 自动加 NEW_TASK）：
- `OUICore/im/IMUtil.java:442`（通知 PendingIntent）
- `OUICalling/CallingServiceImp.java:145`（来电 PendingIntent）
- `OUIConversation/ChatActivity.java:376`（personDetailLauncher.launch）

需 `Class` 的 6 处（`ActivityManager.finishActivity/getDestination()`）→ OUICore 新增小工具（读 RouteMap 公开 API 取 className → `Class.forName`），调用处一行替换：
- `Common.java:83,153`、`ForwardDialog.java:107`、`SelectTargetVM.java:140`、`CreateGroupActivity.java:139`、`GroupMaterialActivity.java:287`

### 2.5 初始化

- `DemoApplication.initARouter()`（:69 `ARouter.init`）删除；TheRouter 通过 ContentProvider 自动初始化，无需显式 init。
- debug 构建保留 `TheRouter.setDebug(true)`（onCreate 或 attachBaseContext）。

### 2.6 ProGuard

- 各模块 `proguard-rules.pro` 检查并删除 ARouter keep 规则。
- app 增加 TheRouter 官方 keep 规则（Fragment/注解保留）。

## 错误处理

- `createFragment()` 路由未命中返回 null：MainActivity 已有 null 判断，维持原语义。
- 服务未注册（`TheRouter.get` 返回 null）：与现状（ARouter 路由未注册时 navigation 返回 null）一致，不额外加防御。

## 验证

1. `./gradlew clean :app:assembleDebug` 编译通过。
2. `grep -r "com.alibaba.android.arouter" --include="*.java" --include="*.gradle"` 零残留。
3. 安装启动（用户侧）不再崩溃；首屏/会话/通讯录/群组/音视频通话跳转正常。

## 明确不做

- 不重构与路由无关的代码（如 LoginVM、gradle-mvn-push 等已修改文件保持原样）。
- 不引入 TheRouter 的高级特性（FlowTask/动态路由表/拦截器）。
