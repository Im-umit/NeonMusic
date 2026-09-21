@echo off
setlocal
set GRADLE_VERSION=8.7
set CACHE=%USERPROFILE%\.gradle\wrapper\dists\neonmusic-gradle-%GRADLE_VERSION%
set DIST=%CACHE%\gradle-%GRADLE_VERSION%-bin.zip
set DIR=%CACHE%\gradle-%GRADLE_VERSION%
if not exist "%DIR%\bin\gradle.bat" (
  echo Please download Gradle 8.7 from https://services.gradle.org/distributions/gradle-8.7-bin.zip and extract it to:
  echo %DIR%
  exit /b 1
)
call "%DIR%\bin\gradle.bat" %*
