@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if errorlevel 1 (
  echo Apache Maven no esta disponible en PATH.
  exit /b 1
)
call mvn clean compile
if errorlevel 1 exit /b 1
call mvn -q exec:java "-Dexec.args=%*"
exit /b %errorlevel%
