#!/system/bin/sh
MODDIR=${0%/*}
while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 2; done
[ -f "$MODDIR/disable" ] && exit 0
nohup /system/bin/sh "$MODDIR/supervisor.sh" > "$MODDIR/launcher.log" 2>&1 &
