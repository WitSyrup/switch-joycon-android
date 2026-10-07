# Joy-Con Home / Capture 按应用映射

作者：**witsyrup**。推荐版本：**JoyCon Home Vector 2.1**。

[下载 APK 与配套 Magisk ZIP](https://github.com/WitSyrup/switch-joycon-android/releases/tag/bundle-2026.10.08)。下文 outputs 为本地目录，在线安装包请从 Release 附件获取。

## 解决什么问题

Switchroot 默认将 Joy-Con Home 作为安卓主页键，Moonlight 串流时会离开游戏。截图键又会在底层切换摇杆鼠标模式。本插件在指定应用里将 Home 改为手柄 Guide，将截图键改为键盘 F12，方便在电脑端绑定截图或其他操作。未选中的应用保留原行为。

## 安装与使用

1. 确认已有 Magisk 与 Vector，使用原装滑轨合并 Joy-Con。
2. 安装 `outputs/JoyCon-Home-Vector-2.1.apk`，它没有桌面图标或设置界面。
3. 在 Magisk → 模块 → 从本地安装，选择 `outputs/JoyCon-Keys-Access-Magisk-2.1.zip`。这是访问底层 Joy-Con 服务的配套权限模块，没有常驻服务。
4. 在 Vector 中启用 Joy-Con Home，作用域保留 **系统框架（system）**，再勾选 Moonlight 等需要生效的应用。
5. 重启安卓。更改应用作用域后，强行停止并重开目标应用；更新插件后重启系统。
6. 开始串流：按 Home 应传递 Guide，按截图键应发送 F12。电脑端需要把 F12 绑定到希望执行的功能，实际 Guide 菜单取决于主机软件设置。

不要同时启用历史 Home 模块或全局改键模块，以免重复处理按键。APK 包名仍是 `local.joycon.home`，便于保持升级兼容。

## 验证与限制

历史设备测试已验证所选应用中的 Guide，取消勾选后恢复安卓 Home；2.1 验证了重启后 F12 按下/松开、电脑收到三组成对 F12 事件、应用外截图键仍可切换鼠标模式。记录见 `work/joycon-home-vector/verification-2.1.txt`。

本模块不替换 vendor 手柄服务，而是在收到截图键后调用接口复位鼠标模式。底层到 Java 处理之间仍有短暂间隙；所选应用中使用截图键会把鼠标模式设为关闭。接口调用失败时回退原行为。SELinux 保持 Enforcing。

## 禁用与卸载

Vector 中禁用 Joy-Con Home，然后重启；移除 APK，并在 Magisk 移除 Joy-Con Keys service access 配套模块。只取消某个应用时，取消作用域勾选并重开该应用即可。

## 配套震动补丁

V+ 震动能力识别补丁已单独整理到 [第三项目](../03-joycon-vplus-rumble/README.md)，安装包与源码也在该目录。此处 `work/joycon-hook` 保留构建共享依赖和早期源码副本；第三项目为后续维护入口。

## 源码与历史版本

`work/joycon-home-vector` 为当前模块；2.0 APK 只改 Home；1.1 控制 App 及源码归档在根目录 `archive/home-control-1.1`，已被无界面的 Vector 版本替代。英文原始技术说明仍保留在源码目录。

构建需要 JDK、Android SDK build-tools 29.0.3、android-29 的 android.jar，以及放入兄弟目录 `work/joycon-hook/` 的 Xposed API 与 R8 依赖。设置 ANDROID_HOME、JOYCON_KEYSTORE（私有密钥路径）及 JOYCON_KEYSTORE_PASSWORD（私有密码）后执行 `work/joycon-home-vector/build.ps1`。首次生成证书主体为 witsyrup，不上传密钥或密码。新密钥构建的 APK 无法直接覆盖旧签名 APK，需卸载旧版后安装。
