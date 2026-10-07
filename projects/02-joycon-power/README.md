# Switch Joy-Con Power

作者：**witsyrup**。推荐版本：**Magisk 1.2**。

[下载 Power 1.2 Magisk ZIP](https://github.com/WitSyrup/switch-joycon-android/releases/tag/bundle-2026.10.08)。下文 outputs 为本地目录，在线安装包请从 Release 附件获取。

## 解决什么问题

Switchroot 熄屏后未必立即深睡，Joy-Con 可能保持连接并亮灯。本模块提供熄屏灭灯与仅 Home 请求亮屏，并允许选择真正断开滑轨的模式。模块不解除安全锁屏，不持有 CPU 唤醒锁。

## 安装与操作

1. 使用已 root、装有 Magisk 的 arm64 Switchroot Android 15，确认原装 Joy-Con 通过滑轨连接。
2. Magisk → 模块 → 从本地安装，选择 `outputs/Switch-JoyCon-Power-Magisk-1.2.zip`，完成后重启。
3. 默认进入 **wake** 模式。Magisk 模块页点击 **操作 / Action** 恢复 wake 模式并确保后台启动。1.2 起操作按钮不再切换到 disconnect，防止误点后 Home 无法唤醒。配置仍保存在模块的 `mode.conf`，disconnect 仅供手动配置。

| 模式 | 熄屏时 | 唤醒方式 |
| --- | --- | --- |
| wake（默认） | 保留滑轨连接，约 3 秒检测熄屏并关闭玩家灯、Home 灯 | 仅收到 Home（Linux 316）首次按下时请求亮屏；连接及输入必须仍可用 |
| disconnect | 解绑两侧滑轨驱动，停止该连接及充电 | 按主机电源键亮屏后自动重新绑定滑轨 |

亮屏后恢复原有指示灯。wake 模式忽略其他按键、摇杆和长按重复事件；这描述的是本模块的行为，其他系统唤醒来源不由模块控制。

## 功能边界与测试

完全断开或深睡导致输入中断后，不能靠 Joy-Con 按键唤醒。当前驱动没有断开后的按键唤醒通道。灭灯不代表手柄关机，尚未测量长期待机节省多少电量；蓝牙手柄未验证。

历史 1.0 已有用户实体测试确认熄屏灭灯和按键亮屏；disconnect 模式已验证两侧解绑和亮屏后重连。1.1 改为 Home-only，已有启动日志确认，但 Home 与 A/B 对照实体测试仍待完成。此次整理没有连接设备重新测试。

自行验证：熄屏后确认灯灭，A/B 不应被本模块用于唤醒，Home 应在输入连接仍可用时亮屏；切换 disconnect 后用主机电源键亮屏并检查手柄重连。测待机需拔掉 USB 并退出仍持有保活唤醒锁的串流会话。

## 日志与卸载

状态：`/data/adb/modules/switch_joycon_power/status.txt`；日志：同目录 `runtime.log`。Magisk 禁用或移除模块后建议重启。运行程序会检测 disable/remove 标记并恢复其修改的指示灯与滑轨连接。

不改写系统分区，不修改 V+ APK。1.0 Magisk ZIP 是历史版本，可能响应多个按键。

## 源码

主要实现：`work/joycon-power/magisk/daemon/Controller.java`；开机入口 `service.sh`；模式切换 `action.sh`。`work/joycon-power/joycon-serdev.c` 是滑轨驱动参考源码，保留原作者版权。

当前发布 ZIP 包含已编译 controller.dex；从源码生成及打包见 `build.ps1`。需要 JDK、Android SDK 的 android-29 与本项目 `tools/r8.jar` 编译依赖。可设置 ANDROID_HOME 指定 SDK。本次已用该脚本成功从源码生成 DEX 和 ZIP。

## 1.2 故障修复（2026-10-08）

设备检查发现 mode.conf 已切换到 disconnect，且 02:22 系统服务重启导致后台进程退出。已恢复 wake 模式，增加 supervisor.sh：后台退出后等待 5 秒重启，重新获取安卓电源服务；禁用或移除模块后停止重试。未增加 CPU 唤醒锁。

已验证新后台运行，并主动终止模块后台程序，确认监督程序自动重新启动。实体 Home 与 A/B 对照测试以用户反馈为准。正式重启前试运行位置为 /data/adb/modules_update/switch_joycon_power。

用户已完成 1.2 实体对照测试并确认：熄屏灯灭，A/B 不亮屏，只有 Home 亮屏。
