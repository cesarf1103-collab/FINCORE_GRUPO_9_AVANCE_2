@echo off
setlocal
cd /d "%~dp0"
if exist "%USERPROFILE%\Tools-FinCore\jdk-17.0.20.1+1\bin\java.exe" set "JAVA_HOME=%USERPROFILE%\Tools-FinCore\jdk-17.0.20.1+1"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
if exist "%USERPROFILE%\Tools-FinCore\apache-maven-3.9.16\bin\mvn.cmd" set "PATH=%USERPROFILE%\Tools-FinCore\apache-maven-3.9.16\bin;%PATH%"
java -version >nul 2>&1
if errorlevel 1 (
  echo No se encontro Java.
  pause
  exit /b 1
)
for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVA_VERSION=%%V"
if not "%JAVA_VERSION:~0,3%"=="17." (
  echo Se requiere Java JDK 17. Compruebe JAVA_HOME y PATH.
  pause
  exit /b 1
)
"%SystemRoot%\System32\where.exe" javac >nul 2>&1
if errorlevel 1 (
  echo No se encontro javac del JDK 17.
  pause
  exit /b 1
)
"%SystemRoot%\System32\where.exe" mvn >nul 2>&1
if errorlevel 1 (
  echo No se encontro Apache Maven.
  pause
  exit /b 1
)
rem Demo H2 persistente, separado de datos/fincore.mv.db y de cualquier FINCORE_DB_URL heredada.
if not exist "datos" mkdir "datos"
if errorlevel 1 (
  echo No se pudo crear la carpeta local de datos.
  pause
  exit /b 1
)
set "FINCORE_DB_URL=jdbc:h2:file:./datos/fincore-demo"
set "FINCORE_DB_SEED=true"
echo Preparando FinCore con Java 17 y la base demo aislada...
call mvn -B -ntp clean compile
if errorlevel 1 (
  echo No se pudo compilar FinCore.
  pause
  exit /b 1
)
echo El navegador se abrira cuando el servidor este listo. Para cerrar, pulse Ctrl+C.
call mvn -q exec:java "-Dfincore.main.class=com.sv.fincore.web.WebMain" "-Dfincore.db.url=jdbc:h2:file:./datos/fincore-demo" "-Dfincore.db.seed=true" "-Dexec.args=--open-browser"
if errorlevel 1 (
  echo FinCore se cerro. Si no llego a abrirse, revise los mensajes anteriores.
  exit /b 1
)
echo FinCore cerrado.
endlocal
