#!/system/bin/sh
MODDIR=${0%/*}
echo wake > "$MODDIR/mode.conf"
sh "$MODDIR/service.sh"
echo 'Restored HOME-only wake mode. Screen-off LEDs turn off; rails stay connected.'
echo 'Action no longer toggles rail disconnect mode.'
