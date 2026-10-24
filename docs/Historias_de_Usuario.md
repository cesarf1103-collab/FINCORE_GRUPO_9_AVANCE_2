# Historias de usuario — FinCore

Estos requisitos reúnen las historias de usuario y los criterios de aceptación de los documentos académicos del equipo. La memoria original aportada por Samaria/Samira en el [PR #50](https://github.com/cesarf1103-collab/FINCORE_GRUPO_9_AVANCE_3/pull/50) se conserva íntegra en los materiales de exposición externos al repositorio.

| Historia | Funcionalidad y criterios de aceptación |
|---|---|
| HU-001 — Registro | Registrar clientes con DUI único, formato `12345678-9` y campos obligatorios completos. |
| HU-002 — Depósitos | Abonar un monto mayor que $0.00 a una cuenta activa y actualizar su saldo. |
| HU-003 — Retiros | Verificar la cuenta, exigir un monto positivo y saldo suficiente antes de retirar fondos. |
| HU-004 — Consulta de saldo | Mostrar los datos y el saldo actualizado de la cuenta. |
| HU-005 — Historial | Consultar los movimientos por cuenta en orden cronológico. |
| HU-006 — Depósito a terceros | Permitir abonos a cuentas propias o de otros clientes registrados. |
| HU-007 — Retiro en ventanilla | Verificar la identidad del titular antes del retiro presencial. La documentación también incluye la desactivación de cuentas. |
| HU-008 — Transferencias | Como cliente, quiero transferir entre cuentas para enviar fondos a otro usuario. El monto debe ser mayor que $0.00, el origen debe tener saldo suficiente y el destino debe existir y estar activo. Registrar débito y crédito, con sus movimientos, de manera atómica mediante `commit` o `rollback`. |

## Alcance de la verificación presencial

La consola pide el DUI del documento presentado y la confirmación expresa del operador de que comprobó físicamente la identidad. `CuentaService.retirarEnVentanilla` rechaza un DUI distinto del titular o la falta de confirmación antes de debitar. La prueba `retiroEnVentanillaExigeConfirmacionYCoincidenciaDeDui`, en [ClienteYHistorialConsistenciaTest](../src/test/java/com/sv/fincore/integration/ClienteYHistorialConsistenciaTest.java), verifica que los rechazos conserven saldo e historial.

La aplicación no reconoce documentos físicos ni autentica al operador. El retiro general de la interfaz web no se presenta como retiro presencial con identidad verificada. Si se exige ese control también en el canal web, su alcance debe confirmarse con el Product Owner o docente; no se presupone un requisito de autenticación adicional.

Los escenarios funcionales y su cobertura se describen en la [matriz de pruebas](Matriz_de_Pruebas.md).
