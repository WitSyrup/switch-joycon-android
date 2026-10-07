#!/system/bin/sh
MODDIR=${0%/*}
PIDFILE="$MODDIR/supervisor.pid"
if [ -f "$PIDFILE" ]; then
  previous_pid=$(cat "$PIDFILE")
  case "$previous_pid" in
    ''|*[!0-9]*) ;;
    *) if kill -0 "$previous_pid" 2>/dev/null && tr '\000' ' ' < "/proc/$previous_pid/cmdline" | grep -q "$MODDIR/supervisor.sh"; then exit 0; fi ;;
  esac
fi
echo $$ > "$PIDFILE"
trap 'rm -f "$PIDFILE"' EXIT
export CLASSPATH="$MODDIR/controller.dex"
while [ ! -f "$MODDIR/disable" ] && [ ! -f "$MODDIR/remove" ]; do
  while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 3; done
  /system/bin/app_process64 /system/bin local.switchpower.Controller "$MODDIR"
  daemon_result=$?
  [ -f "$MODDIR/disable" ] && break
  [ -f "$MODDIR/remove" ] && break
  echo "$(date): controller exited ($daemon_result); retrying in 5 seconds"
  sleep 5
done
