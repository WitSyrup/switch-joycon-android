# Switch Joy-Con Android 插件合集

作者与维护者：**witsyrup**

适用于本项目测试的 Switch OLED、Switchroot Android 15 与原装滑轨 Joy-Con。其他 ROM、蓝牙连接及第三方手柄未验证。

| 项目 | 解决的问题 | 当前版本 | 环境 |
| --- | --- | --- | --- |
| [Home / 截图键按应用映射](projects/01-joycon-home-capture/README.md) | 串流时 Home 返回安卓桌面；截图键切换鼠标模式 | Vector 2.1 + Magisk 配套 2.1 | root、Vector、Magisk |
| [V+ 震动能力识别补丁](projects/03-joycon-vplus-rumble/README.md) | V+ 未识别原装合并 Joy-Con 的震动能力 | Vector 1.0 | root、Vector、Moonlight V+ |
| [锁屏唤醒与熄屏管理](projects/02-joycon-power/README.md) | 熄屏灭灯、仅 Home 唤醒，后台退出后自动恢复 | Magisk 1.2 | root、Magisk、arm64 滑轨驱动 |

三个模块可以按需搭配使用。按键映射作用于 Vector 所选应用；电源管理作用于熄屏状态；震动补丁只作用于 Moonlight V+。

目录中的 `outputs/` 保留当前与历史安装包；各项目 `work/` 保存源码、构建文件及验证记录。`archive/` 保存旧设置 App、上游参考源码、框架安装包及研究资料，默认不提交 GitHub。签名私钥未复制，原始文件保留在原工作目录。

V+ 震动补丁已单独归为第三项目，包含源码、APK、源码包及中文安装说明。

必须了解的限制：完全断开滑轨后不能通过 Joy-Con 按键重新连接并唤醒；需要主机电源键。灭灯不等于手柄关闭，尚无量化省电结果。截图键通过系统接口即时关闭鼠标模式，存在极短的底层切换间隙。

[GitHub 发布说明](docs/GITHUB发布说明.md) · [来源与署名](THIRD_PARTY_NOTICES.md) · [整理与验证记录](docs/整理记录.md)
