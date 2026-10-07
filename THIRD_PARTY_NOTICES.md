# 来源与署名

本项目自制插件、模块的作者与维护者统一为 **witsyrup**。这不改变第三方源码、框架和依赖的作者身份。签名证书是历史构建证书，与项目作者字段不同，保留旧密钥签名可以保持 APK 升级兼容。

参考来源：
- LineageOS Joy-Con 服务：https://github.com/LineageOS/android_hardware_nintendo_joycond/tree/lineage-22.2 （GPL v3 核心逻辑及 Apache 2.0 AIDL/构建部分；原许可证保留在 archive/references/joycond-source。）
- LineageOS Switch 设备配置：https://github.com/LineageOS/android_device_nintendo_nx （原源码版权及授权声明保留。）
- Joy-Con 滑轨驱动：https://github.com/LineageOS/android_kernel_nvidia_kernel-nx/blob/lineage-22.2/drivers/input/joystick/joycon-serdev.c （原版权包含 Daniel J. Ogorchock、CTCaer 等；GPL-2.0+ 标记以原文件为准。）
- Android 策略参考：https://github.com/aosp-mirror/platform_frameworks_base/blob/android-15.0.0_r1/services/core/java/com/android/server/policy/PhoneWindowManager.java （保留 AOSP 原声明。）
- Moonlight：https://github.com/moonlight-stream/moonlight-android ，另有 V+ 的研究源码片段。
- Xposed API、R8、Vector、Magisk 安装引导属于第三方依赖或代码；本地依赖与框架包保留于相关目录，默认不提交 GitHub。发布前依据各自原授权确认分发条件。

没有把第三方代码的 copyright、MODULE_AUTHOR 或原许可证替换为 witsyrup。当前未替用户选择整个自制项目的开源许可证；公开源代码不自动授予开源授权。如需允许别人修改分发，可在明确授权范围后添加合适的 LICENSE，第三方内容继续遵循原许可证。
