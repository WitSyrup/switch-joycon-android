#!/system/bin/sh
set -e
BASE=/data/adb/modules/joycon_keys_access
mkdir -p "$BASE"
cp /data/local/tmp/joycon-keys-access/module.prop "$BASE/module.prop"
cp /data/local/tmp/joycon-keys-access/sepolicy.rule "$BASE/sepolicy.rule"
cp /data/local/tmp/joycon-keys-access/post-fs-data.sh "$BASE/post-fs-data.sh"
chmod 0755 "$BASE"
chmod 0755 "$BASE/post-fs-data.sh"
chmod 0644 "$BASE/module.prop" "$BASE/sepolicy.rule"
magiskpolicy --live --apply "$BASE/sepolicy.rule"
