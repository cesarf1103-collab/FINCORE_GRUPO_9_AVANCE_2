# Matriz de Pruebas Funcionales - FinCore

## Objetivo

Verificar mediante pruebas funcionales que las principales operaciones del sistema bancario FinCore funcionen correctamente, incluyendo escenarios exitosos y escenarios de error.

## Casos de prueba

| ID | Historia de Usuario | Escenario de Prueba | Entrada / Condición | Resultado Esperado | Estado |
|---|---|---|---|---|---|
| TC-001 | HU-001 | Registro exitoso de cliente | DUI válido, nombre y campos obligatorios completos | El cliente se registra correctamente | PASS |
| TC-002 | HU-001 | Registro con DUI duplicado | DUI que ya existe en el sistema | El sistema rechaza el registro | PASS |
| TC-003 | HU-001 | Registro con DUI inválido | DUI con formato incorrecto | El sistema rechaza la entrada y muestra un mensaje de error | PASS |
| TC-004 | HU-001 | Registro con campos obligatorios vacíos | Uno o más campos requeridos sin completar | El sistema solicita completar los campos obligatorios | PASS |
| TC-005 | HU-002 | Depósito exitoso | Monto positivo mayor que $0.00 | El depósito se realiza y el saldo aumenta correctamente | PASS |
| TC-006 | HU-002 | Depósito con monto negativo | Monto menor que $0.00 | El sistema rechaza la operación | PASS |
| TC-007 | HU-003 | Retiro exitoso | Monto menor o igual al saldo disponible | El retiro se realiza y el saldo se actualiza correctamente | PASS |
| TC-008 | HU-003 | Retiro con fondos insuficientes | Monto mayor al saldo disponible | El sistema rechaza el retiro por fondos insuficientes | PASS |
| TC-009 | HU-008 | Transferencia exitosa | Cuentas origen y destino activas y fondos suficientes | La transferencia se realiza correctamente y los saldos se actualizan | PASS |
| TC-010 | HU-008 | Transferencia por $0.00 | Monto igual a $0.00 | El sistema rechaza la transferencia | PASS |
| TC-011 | HU-008 | Transferencia sin fondos suficientes | Monto mayor al saldo disponible | El sistema rechaza la transferencia | PASS |
| TC-012 | HU-008 | Transferencia a cuenta inexistente | Cuenta destino no registrada | El sistema rechaza la transferencia y muestra un mensaje de error | PASS |
| TC-013 | HU-008 | Transferencia con monto negativo | Monto menor que $0.00 | El sistema rechaza la transferencia | PASS |
| TC-014 | HU-004 | Consulta de saldo exitosa | Cuenta existente con saldo disponible | El sistema muestra correctamente el saldo de la cuenta | PASS |
| TC-015 | HU-005 | Consulta de historial | Cuenta existente con movimientos registrados | El sistema muestra correctamente el historial de transacciones | PASS |
| TC-016 | HU-006 | Depósito a terceros exitoso | Cuenta destino existente y monto positivo | El sistema realiza el depósito y actualiza correctamente el saldo | PASS |
| TC-017 | HU-007 | Retiro en ventanilla exitoso | Cuenta existente con saldo suficiente y monto válido | El sistema realiza el retiro y actualiza correctamente el saldo | PASS |
## Criterio de resultado

Los 17 casos se ejecutan mediante `EscenariosFuncionalesTest` con H2 aislado. TC-003 y TC-004 recorren el submenú de clientes con entradas inválidas y comprueban que el registro solo llega a la base tras corregirlas.

TC-017 comprueba el débito y el saldo mediante el retiro general del servicio. El control presencial de identidad se cubre adicionalmente con `retiroEnVentanillaExigeConfirmacionYCoincidenciaDeDui` en [ClienteYHistorialConsistenciaTest](../src/test/java/com/sv/fincore/integration/ClienteYHistorialConsistenciaTest.java) y con [MenuOperacionesTest](../src/test/java/com/sv/fincore/view/MenuOperacionesTest.java). El alcance se detalla en las [historias de usuario](Historias_de_Usuario.md).

- **PASS:** el sistema se comporta de acuerdo con el resultado esperado.
- **FAIL:** el sistema no cumple con el resultado esperado y se debe reportar el error.
- **Pendiente:** la prueba todavía no ha sido ejecutada.
