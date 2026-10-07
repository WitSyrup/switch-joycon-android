$ErrorActionPreference = 'Stop'
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { 'C:\Program Files (x86)\Android\android-sdk' }
$androidJar = Join-Path $sdk 'platforms\android-29\android.jar'
$r8 = Join-Path $PSScriptRoot 'tools\r8.jar'
$mod = Join-Path $PSScriptRoot 'work\joycon-power\magisk'
$classes = Join-Path $PSScriptRoot 'work\joycon-power\daemon-classes'
New-Item -ItemType Directory -Force $classes | Out-Null
& javac -encoding UTF-8 -source 8 -target 8 -cp $androidJar -d $classes (Join-Path $mod 'daemon\Controller.java')
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
$classFiles = Get-ChildItem $classes -Recurse -Filter '*.class' | ForEach-Object FullName
& java -cp $r8 com.android.tools.r8.D8 --lib $androidJar --min-api 29 --output $mod $classFiles
if ($LASTEXITCODE -ne 0) { throw 'D8 failed' }
Move-Item -LiteralPath (Join-Path $mod 'classes.dex') -Destination (Join-Path $mod 'controller.dex') -Force
Get-ChildItem $mod -Recurse -File | Where-Object { $_.Extension -ne '.dex' } | ForEach-Object {
    $content = [IO.File]::ReadAllText($_.FullName)
    [IO.File]::WriteAllText($_.FullName, $content.Replace("`r`n", "`n"), [Text.UTF8Encoding]::new($false))
}
New-Item -ItemType Directory -Force (Join-Path $PSScriptRoot 'outputs') | Out-Null
$out = Join-Path $PSScriptRoot 'outputs\Switch-JoyCon-Power-Magisk-1.2.zip'
Compress-Archive -Path (Join-Path $mod '*') -DestinationPath $out -Force
Write-Output $out
