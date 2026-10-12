@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo JDK not found. Install a JDK and add javac to PATH.
  pause
  exit /b 1
)
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
  echo Compilation failed. Check the messages above.
  pause
  exit /b 1
)
java -cp "out;lib\*" SwingMain
pause
