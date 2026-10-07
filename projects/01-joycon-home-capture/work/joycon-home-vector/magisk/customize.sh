#!/system/bin/sh
ui_print "Joy-Con Keys: enable scoped Capture/F12 mouse reset access"
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/post-fs-data.sh" 0 0 0755
