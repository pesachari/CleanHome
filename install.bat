@echo off
setlocal
title Clean Home - install to Chromecast

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist "%ADB%" (
  echo [!] adb.exe was not found at:
  echo     %ADB%
  echo     Open Android Studio once so it downloads the Android SDK, then run this again.
  pause & exit /b 1
)

set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK%" (
  echo [!] The app has not been built yet.
  echo     In Android Studio: Build ^> Build App Bundle^(s^) / APK^(s^) ^> Build APK^(s^)
  pause & exit /b 1
)

echo.
echo === STEP 1: Pair with the Chromecast (only needed the first time) ===
echo On the TV: Settings ^> System ^> Developer options ^> Wireless debugging
echo            ^> "Pair device with pairing code"
echo.
set /p PAIR=Type the IP:PORT shown in the pairing window (or press Enter to skip): 
if not "%PAIR%"=="" "%ADB%" pair %PAIR%

echo.
echo === STEP 2: Connect ===
echo Close the pairing window. On the Wireless debugging screen, look at
echo "IP address & Port" (the port is DIFFERENT from the pairing port).
echo.
set /p CONN=Type that IP:PORT: 
"%ADB%" connect %CONN%
if errorlevel 1 ( echo [!] Could not connect. & pause & exit /b 1 )

echo.
echo === STEP 3: Install Clean Home ===
"%ADB%" -s %CONN% install -r "%APK%"
if errorlevel 1 ( echo [!] Install failed. & pause & exit /b 1 )

echo.
echo === STEP 4: Make it the home screen ===
echo This turns OFF the Google TV home screen (you can undo it with restore.bat).
set /p ANS=Continue? (y/n): 
if /i not "%ANS%"=="y" goto done
"%ADB%" -s %CONN% shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
"%ADB%" -s %CONN% shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
"%ADB%" -s %CONN% shell am start -n com.cleantv.home/.HomeActivity

:done
echo.
echo Done! Press the Home button on the remote.
pause
