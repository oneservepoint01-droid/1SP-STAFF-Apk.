@echo off
setlocal
cd /d "%~dp0"

set "GRADLE_VERSION=8.9"
set "GRADLE_HOME_LOCAL=%~dp0.gradle-dist\gradle-%GRADLE_VERSION%"

if not exist "%GRADLE_HOME_LOCAL%\bin\gradle.bat" (
  echo Downloading Gradle %GRADLE_VERSION%...
  if not exist ".gradle-dist" mkdir ".gradle-dist"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '.gradle-dist\gradle.zip'; Expand-Archive -Force '.gradle-dist\gradle.zip' '.gradle-dist'; Remove-Item '.gradle-dist\gradle.zip'"
  if errorlevel 1 (
    echo Gradle download failed. Check your internet connection.
    pause
    exit /b 1
  )
)

if "%ANDROID_HOME%"=="" (
  if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
)
if "%ANDROID_SDK_ROOT%"=="" if not "%ANDROID_HOME%"=="" set "ANDROID_SDK_ROOT=%ANDROID_HOME%"

if "%ANDROID_HOME%"=="" (
  echo Android SDK not found.
  echo Install Android Studio and Android SDK Platform 35, then set ANDROID_HOME.
  pause
  exit /b 1
)

call "%GRADLE_HOME_LOCAL%\bin\gradle.bat" assembleDebug
if errorlevel 1 (
  echo.
  echo Build failed. Confirm Android SDK Platform 35 and Build Tools 35.0.0 are installed.
  pause
  exit /b 1
)

echo.
echo APK created:
echo %~dp0app\build\outputs\apk\debug\app-debug.apk
pause
