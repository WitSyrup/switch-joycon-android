#!/system/bin/sh
[ "$ARCH" = arm64 ] || abort 'This module requires an arm64 Switch.'
[ -d /sys/bus/serial/drivers/joycon_serdev ] || abort 'Switch Joy-Con serial driver not found.'
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/action.sh" 0 0 0755
ui_print 'Default: HOME-only wake while connected; screen-off LEDs off.'
ui_print 'Action restores HOME-only wake mode; it does not toggle disconnect.'
ui_print 'No CPU wake lock. Fully disconnected/deep-sleep controller wake is unavailable.'
