@echo off
echo === Limpiando y compilando con Maven ===
call mvn clean compile

IF %ERRORLEVEL% EQU 0 (
    echo === Iniciando la aplicacion ===
    call mvn exec:java -Dexec.mainClass="com.sv.fincore.main.Main"
) ELSE (
    echo Error en la compilacion.
    pause
)