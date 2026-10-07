"""校验本地发布安装包并更新 SHA-256 清单。只使用 Python 标准库。"""
from pathlib import Path
import hashlib
import zipfile

root = Path(__file__).resolve().parents[1]
required = {
    'projects/01-joycon-home-capture/outputs/JoyCon-Keys-Access-Magisk-2.1.zip':
        ['module.prop', 'sepolicy.rule', 'post-fs-data.sh', 'META-INF/com/google/android/update-binary'],
    'projects/02-joycon-power/outputs/Switch-JoyCon-Power-Magisk-1.2.zip':
        ['module.prop', 'controller.dex', 'service.sh', 'supervisor.sh', 'action.sh', 'META-INF/com/google/android/update-binary'],
}
for name, members in required.items():
    with zipfile.ZipFile(root / name) as z:
        assert z.testzip() is None, name
        assert set(members) <= set(z.namelist()), (name, z.namelist())
        assert 'author=witsyrup' in z.read('module.prop').decode('utf-8'), name
        for member in members:
            if member.endswith(('.sh', '.prop', 'update-binary')):
                assert b'\r\n' not in z.read(member), (name, member, 'CRLF')
for outputs in root.glob('projects/*/outputs'):
    lines = []
    for p in sorted(outputs.iterdir()):
        if p.suffix.lower() not in ('.zip', '.apk'): continue
        with zipfile.ZipFile(p) as z:
            assert z.testzip() is None, p
            assert not any(n.lower().endswith(('.jks', '.keystore')) for n in z.namelist()), p
        lines.append(hashlib.sha256(p.read_bytes()).hexdigest() + '  ' + p.name)
    (outputs / 'SHA256SUMS.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
assert not list(root.rglob('*.jks'))
assert not list(root.rglob('*.keystore'))
print('Magisk 目录结构、作者字段、脚本换行、ZIP 完整性与私钥排除检查通过；SHA256SUMS 已更新。')
