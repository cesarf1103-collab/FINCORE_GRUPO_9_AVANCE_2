# INFORME DE PRUEBAS UNITARIAS

## FinCore — Sistema de Gestión Bancaria

### Implementación y validación con JUnit 5 y Maven

Objetivo

Verificar el funcionamiento de los servicios de clientes y cuentas mediante pruebas automatizadas con JUnit 5 y Maven.

Herramientas utilizadas

- Java
- Maven
- JUnit 5

Pruebas ejecutadas

Se ejecutó el comando "mvn clean test".

Resultados

- Pruebas ejecutadas: 15
- Fallos: 0
- Errores: 0
- Pruebas omitidas: 0
- Resultado de Maven: BUILD SUCCESS

Escenarios verificados

- Registro de clientes y validación de DUI duplicado.
- Creación de cuentas y validación de clientes inexistentes.
- Depósitos y retiros.
- Validación de saldo insuficiente.
- Operaciones con cuentas inactivas.
- Transferencias entre cuentas.
- Consulta del historial de transacciones.
- Validación de transacciones nulas.

Conclusión

Las pruebas automatizadas finalizaron correctamente. Los 15 casos ejecutados pasaron sin fallos ni errores en la ejecución registrada.