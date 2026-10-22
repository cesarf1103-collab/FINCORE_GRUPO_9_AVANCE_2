#!/bin/bash

echo "=== Limpiando y compilando con Maven ==="
mvn clean compile

if [ $? -eq 0 ]; then
    echo "=== Iniciando la aplicacion ==="
    mvn spring-boot:run
else
    echo "Error en la compilación."
    exit 1
fi