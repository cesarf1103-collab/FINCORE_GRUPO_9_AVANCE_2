# Historias de Usuario - Avance 2 (FinCore)

## Requerimientos Funcionales

* **HU-001 (Registro):** Registrar clientes con validación de DUI único.
* **HU-002 (Depósitos):** Depósito en cuenta con actualización de saldo.
* **HU-003 (Retiros):** Retiro de fondos validando saldo suficiente.
* **HU-004 (Consulta de Saldo):** Visualización de datos y saldo de cuenta.
* **HU-005 (Historial):** Detalle de movimientos realizados.
* **HU-006 (Depósito a Terceros):** Depósito a cuentas propias o de terceros.
* **HU-007 (Retiro en Ventanilla):** Retiro en efectivo por ventanilla.
* **HU-008 (Transferencias entre Cuentas - NUEVA):**
  * **Descripción:** Como cliente, quiero realizar transferencias entre cuentas para enviar fondos a otro usuario.
  * **Criterios de Aceptación:**
    1. Monto mayor a $0.00.
    2. Saldo suficiente en cuenta origen.
    3. Cuenta destino existente y activa.
    4. Registro de débito en origen y crédito en destino.
