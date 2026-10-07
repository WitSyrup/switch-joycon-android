Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class F12Probe {
 [DllImport("user32.dll")] public static extern short GetAsyncKeyState(int key);
}
"@
$taskLast = $false
$taskEnd = [DateTime]::UtcNow.AddSeconds(45)
while ([DateTime]::UtcNow -lt $taskEnd) {
 $taskDown = ([F12Probe]::GetAsyncKeyState(0x7B) -band 0x8000) -ne 0
 if ($taskDown -ne $taskLast) {
  $taskAction = if ($taskDown) {'DOWN'} else {'UP'}
  Write-Output ('F12 ' + $taskAction + ' ' + [DateTime]::Now.ToString('HH:mm:ss.fff'))
  $taskLast = $taskDown
 }
 Start-Sleep -Milliseconds 5
}
