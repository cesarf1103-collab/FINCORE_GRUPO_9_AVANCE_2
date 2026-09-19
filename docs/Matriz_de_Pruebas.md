# Matriz de Pruebas Funcionales - FinCore

## Objetivo

Verificar mediante pruebas funcionales que las principales operaciones del sistema bancario FinCore funcionen correctamente, incluyendo escenarios exitosos y escenarios de error.

## Casos de prueba

| ID | Historia de Usuario | Escenario de Prueba | Entrada / Condición | Resultado Esperado | Estado |
|---|---|---|---|---|---|
| TC-001 | HU-001 | Registro exitoso de cliente | DUI válido, nombre y campos obligatorios completos | El cliente se registra correctamente | Pendiente |
| TC-002 | HU-001 | Registro con DUI duplicado | DUI que ya existe en el sistema | El sistema rechaza el registro | Pendiente |
| TC-003 | HU-001 | Registro con DUI inválido | DUI con formato incorrecto | El sistema rechaza la entrada y muestra un mensaje de error | Pendiente |
| TC-004 | HU-001 | Registro con campos obligatorios vacíos | Uno o más campos requeridos sin completar | El sistema solicita completar los campos obligatorios | Pendiente |
| TC-005 | HU-002 | Depósito exitoso | Monto positivo mayor que $0.00 | El depósito se realiza y el saldo aumenta correctamente | Pendiente |
| TC-006 | HU-002 | Depósito con monto negativo | Monto menor que $0.00 | El sistema rechaza la operación | Pendiente |
| TC-007 | HU-003 | Retiro exitoso | Monto menor o igual al saldo disponible | El retiro se realiza y el saldo se actualiza correctamente | Pendiente |
| TC-008 | HU-003 | Retiro con fondos insuficientes | Monto mayor al saldo disponible | El sistema rechaza el retiro por fondos insuficientes | Pendiente |
| TC-009 | HU-008 | Transferencia exitosa | Cuentas origen y destino activas y fondos suficientes | La transferencia se realiza correctamente y los saldos se actualizan | Pendiente |
| TC-010 | HU-008 | Transferencia por $0.00 | Monto igual a $0.00 | El sistema rechaza la transferencia | Pendiente |
| TC-011 | HU-008 | Transferencia sin fondos suficientes | Monto mayor al saldo disponible | El sistema rechaza la transferencia | Pendiente |
| TC-012 | HU-008 | Transferencia a cuenta inexistente | Cuenta destino no registrada | El sistema rechaza la transferencia y muestra un mensaje de error | Pendiente |
| TC-013 | HU-008 | Transferencia con monto negativo | Monto menor que $0.00 | El sistema rechaza la transferencia | Pendiente |
| TC-014 | HU-004 | Consulta de saldo exitosa | Cuenta existente con saldo disponible | El sistema muestra correctamente el saldo de la cuenta | Pendiente |
| TC-015 | HU-005 | Consulta de historial | Cuenta existente con movimientos registrados | El sistema muestra correctamente el historial de transacciones | Pendiente |
| TC-016 | HU-006 | Depósito a terceros exitoso | Cuenta destino existente y monto positivo | El sistema realiza el depósito y actualiza correctamente el saldo | Pendiente |
| TC-017 | HU-007 | Retiro en ventanilla exitoso | Cuenta existente con saldo suficiente y monto válido | El sistema realiza el retiro y actualiza correctamente el saldo | Pendiente |
## Criterio de resultado

- **PASS:** el sistema se comporta de acuerdo con el resultado esperado.
- **FAIL:** el sistema no cumple con el resultado esperado y se debe reportar el error.
- **Pendiente:** la prueba todavía no ha sido ejecutada.

## Errores encontrados

Los errores encontrados durante la ejecución de las pruebas serán documentados para su posterior corrección.
