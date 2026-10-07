$ErrorActionPreference = 'Stop'
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { 'C:\Program Files (x86)\Android\android-sdk' }
$bt = Join-Path $sdk 'build-tools\29.0.3'
$androidJar = Join-Path $sdk 'platforms\android-29\android.jar'
$deps = Join-Path $PSScriptRoot '..\01-joycon-home-capture\work\joycon-hook'
$api = Join-Path $deps 'xposed-api.jar'
$r8 = Join-Path $deps 'r8.jar'
$source = Join-Path $PSScriptRoot 'work\joycon-vplus'
$classes = Join-Path $source 'classes'
$dex = Join-Path $source 'dex'
$out = Join-Path $PSScriptRoot 'outputs'
New-Item -ItemType Directory -Force $classes,$dex,$out | Out-Null
$sources = Get-ChildItem (Join-Path $source 'src') -Filter '*.java' | ForEach-Object FullName
& javac -encoding UTF-8 -source 8 -target 8 -cp "$androidJar;$api" -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
$classFiles = Get-ChildItem $classes -Recurse -Filter '*.class' | ForEach-Object FullName
& java -cp $r8 com.android.tools.r8.D8 --lib $androidJar --classpath $api --min-api 26 --output $dex $classFiles
if ($LASTEXITCODE -ne 0) { throw 'D8 failed' }
$unsigned = Join-Path $source 'unsigned.apk'
$aligned = Join-Path $source 'aligned.apk'
& "$bt\aapt.exe" package -f -M (Join-Path $source 'AndroidManifest.xml') -I $androidJar -A (Join-Path $source 'assets') -F $unsigned
if ($LASTEXITCODE -ne 0) { throw 'aapt failed' }
Push-Location $dex
try {
    & "$bt\aapt.exe" add $unsigned classes.dex
    if ($LASTEXITCODE -ne 0) { throw 'aapt add failed' }
} finally { Pop-Location }
& "$bt\zipalign.exe" -f 4 $unsigned $aligned
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed' }
if (!$env:VPLUS_KEYSTORE) {
    Write-Output "Unsigned build completed: $aligned"
    exit 0
}
if (!$env:VPLUS_KEYSTORE_PASSWORD) { throw 'Set VPLUS_KEYSTORE_PASSWORD before signing' }
$signed = Join-Path $out 'Switch-JoyCon-VPlus-Fix-1.0.apk'
$signArgs = @('sign','--ks',$env:VPLUS_KEYSTORE,'--ks-pass','env:VPLUS_KEYSTORE_PASSWORD','--out',$signed)
if ($env:VPLUS_KEY_PASSWORD) { $signArgs += @('--key-pass','env:VPLUS_KEY_PASSWORD') }
& java -jar "$bt\lib\apksigner.jar" @signArgs $aligned
if ($LASTEXITCODE -ne 0) { throw 'Signing failed' }
& java -jar "$bt\lib\apksigner.jar" verify $signed
if ($LASTEXITCODE -ne 0) { throw 'Signature verification failed' }
Write-Output $signed
