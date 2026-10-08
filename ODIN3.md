# 奥丁 3 手柄适配

上游：[xiaojieonly/Ehviewer_CN_SXJ](https://github.com/xiaojieonly/Ehviewer_CN_SXJ)，基于 2.0.2.5 / `e820f1b`。

本分支修复首页浏览列表整体获得焦点、无法选择内部卡片的问题。首页、搜索结果、订阅和热门页面共用同一套画廊卡片，因此这些入口同时支持逐卡导航。

## 操作

- 左摇杆或十字键：在卡片之间移动，移动至可视区域边缘时继续滚动。
- 在列表顶部继续按上：触发下拉刷新；如有上一页，先加载上一页。在底部继续按下：触发上拉加载下一页。长按方向键不会重复发起同一次边界操作。
- 在最左列继续按左：焦点移至左上角菜单按钮；按 A／确认／Enter 打开菜单，上下选择菜单项，按返回关闭并回到菜单按钮，按下回到列表。手柄导航时搜索栏保持可见。
- A、十字键确认或 Enter：打开当前卡片对应的画廊。
- 长按确认键：调用卡片原有的长按菜单。
- 当前卡片使用 Android 原生的焦点背景高亮，列表模式和缩略图网格模式都适用。
- 详情页的缩略图和“更多预览”页面支持逐图导航，A／确认／Enter 从选中的页开始阅读；“更多预览”文字单独获得焦点。
- 浏览列表不显示侧面的强调色快速滑动条。
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

列表用例还覆盖顶部刷新、底部刷新、下一页追加后的焦点保留、长按方向键去重和滑动条隐藏。`GalleryPreviewNavigationTest` 使用实际的缩略图布局，覆盖详情预览和完整预览网格的四方向导航、滚动、确认键打开所选页、长按重试入口、触屏点击，以及“更多预览”的独立焦点。

`MenuNavigationTest` 使用实际的主界面抽屉、搜索栏和卡片布局，覆盖列表／网格进入菜单、菜单项选择、A／确认／Enter 激活、焦点限制与返回、滚动后的菜单可达性，以及摇杆轴输入。

`TouchNavigationTest` 使用触屏事件验证卡片／封面、图片／页码／预览空白区域、长按、滚动、触摸刷新与分页、菜单点击与手势、搜索框和搜索栏显隐。详情页的预览空白区域和页码保留原有的“更多预览”入口，手柄缩略图通过按键转发打开图片，不占用这些触摸入口。

安装应用 APK 和测试 APK 后运行：

```sh
adb shell am instrument -w -e class com.hippo.ehviewer.ui.scene.gallery.list.GalleryNavigationTest,com.hippo.ehviewer.ui.scene.GalleryPreviewNavigationTest,com.hippo.ehviewer.ui.scene.gallery.list.MenuNavigationTest,com.hippo.ehviewer.ui.scene.gallery.list.TouchNavigationTest com.xjs.ehviewer.debug1.test/androidx.test.runner.AndroidJUnitRunner
```

离线验证页只包含在 debug 构建中，可从 `GalleryNavigationTestActivity` 打开；release 构建仅包含正常应用和导航修复。
