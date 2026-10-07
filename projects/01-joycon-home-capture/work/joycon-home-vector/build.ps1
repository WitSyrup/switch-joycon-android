$ErrorActionPreference = 'Stop'
$taskRoot = $PSScriptRoot
$sdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { 'C:\Program Files (x86)\Android\android-sdk' }
$bt = Join-Path $sdkRoot 'build-tools\29.0.3'
$androidJar = Join-Path $sdkRoot 'platforms\android-29\android.jar'
$apiJar = Join-Path $taskRoot '..\joycon-hook\xposed-api.jar'
$r8Jar = Join-Path $taskRoot '..\joycon-hook\r8.jar'
New-Item -ItemType Directory -Force (Join-Path $taskRoot 'classes'),(Join-Path $taskRoot 'dex') | Out-Null
$sources = Get-ChildItem (Join-Path $taskRoot 'src') -Filter '*.java' | ForEach-Object FullName
& javac -encoding UTF-8 -source 8 -target 8 -cp "$androidJar;$apiJar" -d (Join-Path $taskRoot 'classes') $sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
$classFiles = Get-ChildItem (Join-Path $taskRoot 'classes') -Recurse -Filter '*.class' | ForEach-Object FullName
& java -cp $r8Jar com.android.tools.r8.D8 --min-api 29 --lib $androidJar --classpath $apiJar --output (Join-Path $taskRoot 'dex') $classFiles
if ($LASTEXITCODE -ne 0) { throw 'D8 failed' }
& "$bt\aapt.exe" package -f -M (Join-Path $taskRoot 'AndroidManifest.xml') -I $androidJar -S (Join-Path $taskRoot 'res') -A (Join-Path $taskRoot 'assets') -F (Join-Path $taskRoot 'unsigned.apk')
if ($LASTEXITCODE -ne 0) { throw 'aapt failed' }
Push-Location (Join-Path $taskRoot 'dex')
try { & "$bt\aapt.exe" add (Join-Path $taskRoot 'unsigned.apk') classes.dex } finally { Pop-Location }
& "$bt\zipalign.exe" -f 4 (Join-Path $taskRoot 'unsigned.apk') (Join-Path $taskRoot 'aligned.apk')
$key = if ($env:JOYCON_KEYSTORE) { $env:JOYCON_KEYSTORE } else { Join-Path $taskRoot 'signing.jks' }
if (!$env:JOYCON_KEYSTORE_PASSWORD) { throw 'Set JOYCON_KEYSTORE_PASSWORD before signing; keep the password private.' }
if (!(Test-Path $key)) {
 & keytool -genkeypair -keystore $key -storepass:env JOYCON_KEYSTORE_PASSWORD -keypass:env JOYCON_KEYSTORE_PASSWORD -alias joyconhome -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=witsyrup'
 if ($LASTEXITCODE -ne 0) { throw 'Key generation failed' }
}
New-Item -ItemType Directory -Force (Join-Path $taskRoot '..\..\outputs') | Out-Null
$out = Join-Path $taskRoot '..\..\outputs\JoyCon-Home-Vector-2.1.apk'
& java -jar "$bt\lib\apksigner.jar" sign --ks $key --ks-pass env:JOYCON_KEYSTORE_PASSWORD --key-pass env:JOYCON_KEYSTORE_PASSWORD --out $out (Join-Path $taskRoot 'aligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'Signing failed' }
& java -jar "$bt\lib\apksigner.jar" verify --verbose $out
if ($LASTEXITCODE -ne 0) { throw 'Verification failed' }
Write-Output $out
