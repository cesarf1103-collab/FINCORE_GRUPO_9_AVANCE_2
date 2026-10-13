# UNIVERSIDAD EVANGÉLICA DE EL SALVADOR
**Facultad de Ingeniería – Desarrollo de Software y Ciencia de Datos**  
**Asignatura:** Programación II  
**Facilitador:** Ing. Daniel Enrique Guevara Gómez  
**Proyecto:** Core Bancario Simplificado "FinCore" — **Entrega Final**  
**San Salvador, El Salvador**

## 1. Portada e Integrantes del Equipo (Grupo 09)

| N°| Nombre | CIF | Rol Scrum | Rol Técnico |
| 1 | Samaria Isabel Gamez González | 2026011590 | Product Owner | Analista |
| 2 | César Fernando Estrada Martínez | 2026010835 | Scrum Master | Desarrollador Fullstack |
| 3 | Vidal Caleb Rodríguez Paulino | 2026011708 | Developer | Backend & Concurrencia |
| 4 | Josué Isaac Elias Bonilla | 2026011141 | Developer | Backend & Lógica |
| 5 | Emmanuel Tomas Contreras Valle | 2026011587 | Developer | Backend & Genéricos |
| 6 | José Efraín Silva Rodríguez | 2026011592 | Developer | Frontend & Vistas |
| 7 | Elvis Adiel Flores | 2026011266 | Developer | Frontend |
| 8 | Julio Agustín Garay Miranda | 2026011159 | Developer | Despliegue & Scripts |
| 9 | Elmer Antonio Morales López | 2026010134 | Developer | Base de Datos & JDBC |
| 10 | Jesse Maribel Mena Fuentes | 2026010775 | QA | Pruebas Unitarias Lead |
| 11 | Iris Yesenia Arteaga de Rodríguez | 2026010189 | QA | Mockito & Calidad |
| 12 | Nelson Alejandro Lopez Parada | 2026010069 | Architect | Arquitecto de Software |


## 2. Introducción

El presente documento constituye la memoria técnica formal de la **Entrega Final** del sistema bancario **"FinCore"**, 
una aplicación desarrollada en **Java 17** bajo una arquitectura por capas desacoplada. En esta versión final se integran 
las mejoras requeridas sobre los avances anteriores, destacando la migración completa de la persistencia de datos binaria 
hacia una base de datos relacional mediante **JDBC**, la implementación de un pool de conexiones optimizado a través de 
**DataSourceConfig**, la integración de **programación concurrente real con acceso transaccional a la base de datos** y 
el aseguramiento de calidad mediante pruebas unitarias automatizadas con **JUnit 5** y **Mockito**.


## 3. Descripción del Problema

Las entidades financieras demandan plataformas centrales transaccionales capaces de procesar múltiples operaciones simultáneas 
garantizando la consistencia, la integridad referencial de los saldos y la alta disponibilidad. La solución "FinCore" simula las 
operaciones clave de la banca (gestión de clientes, apertura de cuentas, depósitos, retiros, transferencias entre cuentas y auditoría)
superando el almacenamiento local plano o binario e implementando mecanismos de concurrencia y persistencia relacional empresarial.


## 4. Objetivos

### **Objetivo General**
Construir y desplegar una aplicación bancaria en Java funcional, mantenible y escalable, estructurada en capas desacopladas que incorpore 
programación orientada a objetos, persistencia JDBC relacional, pool de conexiones DataSource, ejecuciones multihilo concurrentes con acceso
a BD y pruebas unitarias automatizadas.

### **Objetivos Específicos**
* Consolidar la arquitectura por capas desacoplando las vistas del dominio mediante interfaces genéricas (`CrudRepository<T, ID>`).
* Sustituir la persistencia local en archivos por consultas SQL relacionales usando **JDBC** (`PreparedStatement`, `ResultSet`).
* Configurar y documentar los 15 parámetros de optimización del pool de conexiones **DataSource**.
* Implementar procesos **concurrentes multihilo** (`ExecutorService` / `Runnable`) que interactúen directamente con la base de datos para procesamiento de fondo.
* Validar el comportamiento de controladores, servicios y lógica de negocio mediante **JUnit 5** y **Mockito**.


## 5. Requerimientos del Sistema (Historias de Usuario)

* **HU-001 (Registro de Clientes):** Captura y validación de atributos de clientes con verificación de unicidad de DUI (formato `12345678-9`).
* **HU-002 (Depósitos en Cuentas):** Abono de saldo a cuentas activas utilizando la clase `BigDecimal` para evitar imprecisiones financieras.
* **HU-003 (Retiro de Fondos):** Verificación de existencia de cuenta y validación estricta de saldo suficiente previo al débito.
* **HU-004 (Consulta de Saldo):** Despliegue de los datos generales del producto bancario y su saldo disponible actual.
* **HU-005 (Historial de Movimientos):** Consulta cronológica de transacciones registradas.
* **HU-006 (Depósito a Terceros):** Capacidad de abonar fondos a cuentas pertenecientes a otros clientes del sistema.
* **HU-007 (Retiro en Ventanilla / Desactivación):** Operación presencial en ventanilla e inactivación de cuentas bancarias.
* **HU-008 (Transferencias entre Cuentas - Atomisidad en BD):**
  * **Monto:** Debe ser un valor estrictamente positivo (monto > $0.00).
  * **Verificación de Origen:** La cuenta de origen debe poseer saldo suficiente para cubrir el débito.
  * **Verificación de Destino:** La cuenta de destino debe existir en el sistema y encontrarse en estado activo.
  * **Ejecución Transaccional Atómica:** El débito en la cuenta origen y el crédito en la cuenta destino se registran simultáneamente dentro de un único bloque transaccional en la base de datos (`commit` / `rollback`).


## 6. Arquitectura del Proyecto

Ubicación del paquete principal en `src/main/java/com/sv/fincore/`:

```text
com.sv.fincore/
├── config/
│   └── DataSourceConfig.java       <-- Configuración del Pool de Conexiones (15 parámetros)
├── controller/
│   ├── ClienteController.java
│   └── CuentaController.java
├── dao/
│   ├── CrudRepository.java         <-- Interfaz genérica
│   ├── ClienteDAO.java             <-- Persistencia JDBC
│   ├── CuentaDAO.java              <-- Persistencia JDBC
│   └── TransaccionDAO.java         <-- Persistencia JDBC
├── exception/
│   ├── SaldoInsuficienteException.java
│   └── CuentaNoEncontradaException.java
├── main/
│   └── Main.java                   <-- Punto de entrada de la aplicación
├── model/
│   ├── Persona.java                <-- Clase abstracta base
│   ├── Cliente.java                <-- Hereda de Persona
│   ├── Empleado.java               <-- Clase abstracta
│   ├── Administrador.java          <-- Hereda de Empleado
│   ├── Cajero.java                 <-- Hereda de Empleado
│   ├── Cuenta.java
│   └── Transaccion.java
├── service/
│   ├── ClienteService.java
│   ├── CuentaService.java
│   └── ReporteConcurrenteTask.java <-- Tarea multihilo integrada con BD
└── view/
    ├── MenuClientes.java
    ├── MenuCuentas.java
    └── MenuPrincipal.java
