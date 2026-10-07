#!/system/bin/sh
set -e
DEV=/dev/input/event6
sendevent "$DEV" 1 309 1
sendevent "$DEV" 0 0 0
sleep 0.15
sendevent "$DEV" 1 309 0
sendevent "$DEV" 0 0 0
