# 奥丁 3 手柄适配

上游：[xiaojieonly/Ehviewer_CN_SXJ](https://github.com/xiaojieonly/Ehviewer_CN_SXJ)，基于 2.0.2.5 / `e820f1b`。

本分支修复首页浏览列表整体获得焦点、无法选择内部卡片的问题。首页、搜索结果、订阅和热门页面共用同一套画廊卡片，因此这些入口同时支持逐卡导航。

## 操作

- 左摇杆或十字键：在卡片之间移动，移动至可视区域边缘时继续滚动。
- A、十字键确认或 Enter：打开当前卡片对应的画廊。
- 长按确认键：调用卡片原有的长按菜单。
- 当前卡片显示主题强调色边框，列表模式和缩略图网格模式都适用。
- 触屏点击卡片、点击列表封面预览标签以及滑动浏览仍可使用。

方向移动使用 Android 的焦点导航和摇杆兼容处理，无需为掌机设置按键映射。每张卡片只有一个焦点入口，封面和内部文字不会抢走卡片焦点。

## 安装和构建

测试 APK 的包名为 `com.xjs.ehviewer.debug1`，名称为 **EhViewer Odin 3**，可以与官方版并存。两者的登录状态和应用数据独立；自编译签名无法直接覆盖官方签名的 APK。

使用上游的 JDK 21、Android SDK 35 和 Gradle 包装器：

```sh
bash gradlew app:assembleDebug app:assembleDebugAndroidTest
```

APK 输出在 `app/build/outputs/apk/`。fork 的 Build 工作流会同时构建应用 APK 和设备测试 APK。

## 回归验证

`GalleryNavigationTest` 在设备上使用首页实际的布局、卡片适配器和瀑布流管理器，填充离线测试卡片，不需要登录或访问站点。它覆盖列表／网格的四方向导航与滚动、A／确认／Enter 激活、确认键长按、适配器更新期间的无效位置、触屏及封面点击、摇杆和十字键轴输入。

安装应用 APK 和测试 APK 后运行：

```sh
adb shell am instrument -w -e class com.hippo.ehviewer.ui.scene.gallery.list.GalleryNavigationTest com.xjs.ehviewer.debug1.test/androidx.test.runner.AndroidJUnitRunner
```

离线验证页只包含在 debug 构建中，可从 `GalleryNavigationTestActivity` 打开；release 构建仅包含正常应用和导航修复。
