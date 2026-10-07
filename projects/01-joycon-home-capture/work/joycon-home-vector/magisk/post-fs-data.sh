#!/system/bin/sh
# Apply once at boot; no background process. Directly staged modules are not
# included in this Magisk build's cached early-policy partition.
MODDIR=${0%/*}
magiskpolicy --live --apply "$MODDIR/sepolicy.rule"
