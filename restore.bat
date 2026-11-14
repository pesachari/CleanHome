@echo off
setlocal
title Clean Home - restore Google TV home screen

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist "%ADB%" ( echo [!] adb.exe not found at %ADB% & pause & exit /b 1 )

echo On the TV: Settings ^> System ^> Developer options ^> Wireless debugging
set /p CONN=Type the "IP address & Port" shown there: 
"%ADB%" connect %CONN%
"%ADB%" -s %CONN% shell pm enable com.google.android.apps.tv.launcherx
"%ADB%" -s %CONN% shell pm enable com.google.android.tungsten.setupwraith

set /p ANS=Also uninstall Clean Home? (y/n): 
if /i "%ANS%"=="y" "%ADB%" -s %CONN% uninstall com.cleantv.home

echo.
echo Google TV home screen is back. Press Home on the remote (restart the Chromecast if needed).
pause
