# Joy-Con 改键 2.1 / 电源管理 1.2 / V+ 震动补丁 1.0

作者：witsyrup。测试环境：Switch OLED / Switchroot Android 15 / 原装滑轨 Joy-Con。

- Home / Capture：Vector 选中的应用内 Home → Guide，截图键 → F12；应用外保留原功能。无独立界面，配套 Magisk 权限模块无常驻服务。
- V+ 震动补丁：修复 Moonlight V+ 对原装 Combined Joy-Cons 震动能力的识别，仅注入 V+；实际串流震动待实体测试。
- Power 1.2：默认熄屏灭灯，仅 Home 请求亮屏；后台退出后自动重启。操作按钮恢复 Home 唤醒模式，避免误切到断开模式。disconnect 仅供手动配置，断开后需主机电源键亮屏。

安装：Home APK + Keys Access ZIP，Vector 勾选系统框架和目标应用，重启。Power 1.2 ZIP 按需在 Magisk 安装并重启。V+ 补丁安装 APK，在 Vector 仅勾选 Moonlight V+（com.limelight.qiin），启用后强行停止并重开 V+，开始新串流。

已验证：Home / F12 按键转发；Power 1.2 后台自动恢复及用户实体测试“灯灭、A/B 不亮屏、只有 Home 亮屏”。

限制：断开滑轨或深睡后不能用手柄唤醒；没有量化省电结论；鼠标模式复位存在短暂间隙；V+ 实际串流震动尚待实体测试。详情见三个项目 README。

本次以预发布版提供，不添加项目整体 LICENSE。自制插件作者与维护者为 witsyrup，第三方版权署名保留。
