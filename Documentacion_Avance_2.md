# Documentación del Proyecto: FinCore - Avance 2

**Asignatura:** Programación II  
**Grupo:** 09  

---

## 1. Introducción y Objetivos
El proyecto **FinCore** es una aplicación bancaria desarrollada en Java orientada a la gestión de operaciones financieras esenciales. En este segundo avance se implementa una arquitectura modular por capas (DAO, Servicios, Modelos y Utilidades) y la persistencia de datos mediante lectura y escritura de archivos.

---

## 2. Historias de Usuario y Criterios de Aceptación

* **HU-001 (Registro de Clientes):** Permite registrar nuevos clientes solicitando Nombre, Apellido, DUI, Teléfono, Correo y Dirección, validando que el DUI sea único.
* **HU-002 (Depósitos en Cuentas):** Permite realizar depósitos a cuentas activas con montos mayores a $0.00.
* **HU-003 (Retiro de Fondos):** Controla el retiro de efectivo en cuenta validando previamente los fondos disponibles.
* **HU-004 (Consulta de Saldo):** Muestra la información de la cuenta y el saldo actualizado en pantalla.
* **HU-005 (Historial de Movimientos):** Presenta la lista de transacciones realizadas en orden cronológico.
* **HU-006 (Depósitos a Terceros):** Permite abonar fondos a cuentas de otros usuarios registrados.
* **HU-007 (Retiro en Ventanilla):** Gestiona retiros presenciales verificando la identidad del titular.
* **HU-008 (Transferencias entre Cuentas):** Ejecuta el débito en la cuenta origen y el crédito en la cuenta destino de forma simultánea.

---

## 3. Arquitectura del Proyecto (Estructura de Paquetes)

El código fuente está estructurado bajo la ruta `com.sv.fincore` en los siguientes paquetes:

* **`com.sv.fincore.modelo`:** Contiene las clases de entidad que representan la estructura de datos (Cliente, Cuenta, Transaccion).
* **`com.sv.fincore.dao`:** Encargado del acceso a datos y operaciones de almacenamiento (ej. `TransaccionDAO.java`).
* **`com.sv.fincore.servicio`:** Aloja la lógica de negocio y las validaciones previas a la ejecución de las operaciones bancarias.
* **`com.sv.fincore.util`:** Clases auxiliares y herramientas de soporte para la lectura/escritura de archivos (ej. `ArchivoUtil.java`).
* **`com.sv.fincore.principal`:** Punto de entrada ejecutable de la aplicación.

---

## 4. Pruebas y Verificación
Se cuenta con un módulo de pruebas funcionales automatizadas que evalúa la correcta ejecución de transferencias, retiros y depósitos antes de integrar los cambios al código principal.
