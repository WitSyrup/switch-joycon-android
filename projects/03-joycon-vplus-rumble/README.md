# Moonlight V+ Joy-Con 震动识别补丁

作者与维护者：**witsyrup**。版本：**1.0**。类型：Vector / Xposed 插件。

## 解决什么问题

Switchroot 的原装合并 Joy-Con 可能已有振动器，但 Moonlight V+ 的震动能力判断仍将其排除。本补丁修正 V+ 的 `hasRumbleCapability` 检测，让满足条件的 Joy-Con 可以进入原有震动处理流程。

仅处理 Nintendo Combined Joy-Cons（厂商 057e、产品 2008），要求存在缓存的 vibrator/vibratorManager、摇杆轴，并符合原有控制器分配条件。其他控制器保持原有结果；字段或方法不兼容时保留原行为并记录日志。

## 安装和操作

1. 确认设备已有 root 和 Vector，使用原装合并 Joy-Con 及 Moonlight V+。
2. 安装 `outputs/Switch-JoyCon-VPlus-Fix-1.0.apk`。包名为 `local.switchfix.joycon`，无独立设置界面。
3. 在 Vector 启用 **Switch Joy-Con V+ Fix**，作用域只勾选 **Moonlight V+（com.limelight.qiin）**。不需要勾选系统框架，也不需要本项目的配套 Magisk 权限模块。
4. 强行停止并重新打开 V+，建立新的串流会话。若 V+ 提供手柄震动开关，确认已开启；主机游戏或控制器测试也要启用震动。
5. 首次测试仅连接 Joy-Con，使用电脑端游戏或控制器震动测试确认实际反馈。

可以与合集的 Home / F12 改键模块及 Power 模块同时安装。两个 Vector 插件各自使用不同作用域配置。

## 验证结果和限制

历史验证已确认 APK 签名、安装、启用作用域及 V+ 进程中的 `installed 2 capability hooks` 日志。**实际串流震动仍需实体测试**，不能把 hook 加载视为震动已成功。

命中修复条件时，预期日志为：`SwitchJoyConFix: enabled rumble for built-in Combined Joy-Cons`。不兼容时查看 `hook unavailable` 或 `incompatible layout` 日志。

它只修正客户端的能力识别，不创建缺失的振动器、不替换驱动、不解决主机震动配置或控制器编号冲突。普通 Moonlight、其他 V+ 包名、蓝牙独立 Joy-Con、第三方手柄与其他 ROM 未验证。V+ 更新后，兼容性取决于内部类、方法和字段是否保留。

## 禁用和卸载

Vector 中禁用补丁，强行停止并重开 V+ 即可恢复原来的检测行为。需要时卸载 APK，不用更改主机或系统配置。

## 文件与构建

- `work/joycon-vplus/src/Entry.java`：补丁实现。
- `work/joycon-vplus/AndroidManifest.xml`、`assets/xposed_init`：模块声明与入口。
- `outputs/Switch-JoyCon-VPlus-Fix-1.0.apk`：原来已验证签名的安装包，保持原签名与行为。
- `outputs/Switch-JoyCon-VPlus-Fix-source.zip`：当前整理后的源码、构建脚本及中文说明，不包含私钥或依赖。
- `outputs/SHA256SUMS.txt`：附件校验清单。

`build.ps1` 使用 JDK、Android SDK build-tools 29.0.3、android-29 的 android.jar，以及第一项目 `work/joycon-hook/` 中的 Xposed API 和 R8。设置 ANDROID_HOME 可指定 SDK。

直接运行脚本仅生成 unsigned/aligned APK，不覆盖已验证的发布 APK。发布签名时设置 `VPLUS_KEYSTORE`、`VPLUS_KEYSTORE_PASSWORD`（必要时设置 `VPLUS_KEY_PASSWORD`），使用自己的私有签名密钥。不同密钥签名不能覆盖原 APK，需卸载旧版后安装。作者已在整理后的源码说明中标为 witsyrup；保留的历史 APK 未重新签名。
