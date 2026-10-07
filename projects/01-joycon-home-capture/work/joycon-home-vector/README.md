# Joy-Con Home + Capture / Vector 2.1

Device: Switchroot Android 15; official combined Joy-Cons. Package remains
`local.joycon.home`, with no activity, launcher, permission, or settings UI.

Keep `system` and desired apps selected in Vector. Scoped window titles are
marked by the app-side hook; system policy uses that marker, not a package
allowlist. Restart target apps after scope changes. Reboot after module updates.

* Home (scan 316) -> controller Guide, retaining the physical controller ID.
* Capture (scan 309) -> keyboard F12 (Android 142, Linux scan 88), using virtual
  keyboard identity and keyboard source to bypass controller remapping.
* Other apps retain stock Home and Capture behavior.

Capture is intercepted before Android policy can send MEDIA_PLAY_PAUSE to a
media session. The target UID and scope decision are fixed for the gesture.
Input injection remains asynchronous and UID-targeted. Home and Capture have
separate gesture maps so they can be pressed together.

The native joycond daemon toggles mouse mode before Android receives Capture.
This implementation immediately resets it via the existing stable AIDL v1
`setRsmouse(false)` (transaction 8). It does not replace or patch the vendor
daemon. There is a short native-to-Java interval during which native events
can still be interpreted as mouse input; this is a reset, not a native veto.
The existing mouse setting becomes off after Capture in a selected app.

The companion Magisk module `joycon_keys_access` contains metadata and
two narrowly scoped SELinux rules: system_server may find joycon_service and
call joycond via Binder. A boot script applies these once because this device's
Magisk build doesn't load policy from a directly staged module automatically.
It has no daemon. It is required for mouse reset.
On reset failure, the gesture falls back to stock behavior rather than sending
a partial F12 gesture. Remove the companion when uninstalling this module.

Build: `build.ps1` using the existing local Android SDK and signing key.
Install APK, place the companion files in `/data/adb/modules/joycon_keys_access`,
and reboot. Keep SELinux enforcing. No keylayout or vendor partition changes.

Tests use `test-capture.sh` at the physical left Joy-Con evdev node, upstream of
joycond, to exercise both native toggling and Android remapping. Device paths
must be rechecked after controller reconnection or reboot. `probe-f12.ps1`
observes only Windows F12 state during a bounded 45-second stream test.
