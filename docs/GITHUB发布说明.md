# 发布到 GitHub

建议使用一个仓库 `switch-joycon-android`，三个项目按当前目录分类。仓库简介可写：**适用于 Switchroot Android 15 的 Joy-Con 按应用改键、Home 唤醒、熄屏管理与 V+ 震动识别插件。作者 witsyrup。**

## 提交源代码

1. 登录 GitHub，点右上角 + → New repository，填写仓库名与简介，选择 Public 或 Private。
2. 不勾选自动创建 README、.gitignore、LICENSE，因为本地已有内容。项目许可证尚未选定，需要开源授权时再选择并添加；保留第三方授权声明。
3. 在此文件夹打开 PowerShell。依次执行（用户名换成实际 GitHub 账号）：

```powershell
git init -b main
git add .
git status
git diff --cached --stat
git commit -m "整理 Joy-Con 按键与电源管理模块"
git remote add origin https://github.com/你的用户名/switch-joycon-android.git
git push -u origin main
```

首次提交若提示身份未设置，执行 `git config user.name "witsyrup"` 和 `git config user.email "GitHub中配置的邮箱或noreply邮箱"`，再提交。不要填虚构邮箱。认证使用 GitHub 登录授权、凭证管理器或个人令牌，不使用账户密码。

当前文件夹位于 NAS 映射盘，Git 检查出现了 `detected dubious ownership`。确认这是你自己的项目目录后，可执行下面的命令，仅信任这个目录（不要信任所有目录）：

```powershell
git config --global --add safe.directory "//Syrupnas/d/SWITCH/anzhuo15/switch-joycon-witsyrup"
```

也可将整个文件夹复制到本机自己的磁盘再发布。本次未更改全局 Git 信任设置。

`.gitignore` 会排除本地 archive、构建依赖、安装包、编译缓存和私钥；不要使用 `git add -f` 把它们强制提交。保留的上游参考版权不应改名。源代码引用的本地 SDK 和依赖需由构建者自行准备，操作见各项目 README。

官方说明：[新建仓库](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-new-repository)、[推送本地代码](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github)。

## 发布安装包

进入仓库右侧 Releases → Create a new release，建议首个合集标签 `bundle-2026.10.08`，标题 `Joy-Con 改键 2.1 / 电源管理 1.2 / V+ 震动补丁 1.0`。

上传四个主要附件：
- `projects/01-joycon-home-capture/outputs/JoyCon-Home-Vector-2.1.apk`
- `projects/01-joycon-home-capture/outputs/JoyCon-Keys-Access-Magisk-2.1.zip`
- `projects/02-joycon-power/outputs/Switch-JoyCon-Power-Magisk-1.2.zip`

- `projects/03-joycon-vplus-rumble/outputs/Switch-JoyCon-VPlus-Fix-1.0.apk`（实际震动未验证，标为实验性）

可另附第三项目的 `Switch-JoyCon-VPlus-Fix-source.zip`，已同步为整理后的源码与中文说明，不含签名密钥或构建依赖。旧 APK/ZIP 不建议混在首个正式版本中。附上各 outputs 目录的 SHA256SUMS.txt 供校验。

发布正文可以复制 `docs/RELEASE说明.md`。V+ 实体震动尚待验证，因此首发建议勾选 **Set as a pre-release**，实测通过后再标正式发布。附件与 README 应同时上传；用户先安装 Home APK、配套权限 ZIP，再按需安装 Power ZIP。

官方说明：[管理 Release 与附件](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)。

## 后续更新

源代码修改后 git add、commit、push；发布新标签及附件。保留私有 APK 签名密钥用于后续更新，密钥留在本地或私有安全存储中。改用新密钥签名时，需要告知旧用户卸载再安装。

本次只完成本地整理，没有创建远程仓库或上传文件。
