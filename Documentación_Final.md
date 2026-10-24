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

El presente informe constituye la memoria técnica formal de la Entrega Final del Core 
Bancario Simplificado "FinCore", desarrollado en Java 17+ mediante una arquitectura por 
capas desacoplada.

Esta versión final consolida los requerimientos del ciclo, migrando la persistencia 
plana/binaria hacia un esquema de base de datos relacional alimentado por JDBC y el motor
H2 (almacenado en archivo local ./datos/fincore-demo). Además, la arquitectura integra un 
pool de conexiones Apache Commons DBCP2 configurado en DataSourceConfig, ejecución de 
procesos concurrentes multihilo mediante ExecutorService y CompletableFuture, doble interfaz 
de acceso (Consola interactiva e Interfaz Web local vía JDK HttpServer y JSON) y la 
automatización de calidad con JUnit 5 y Mockito.

## 3. Descripción del Problema

Las plataformas de banca centralizada (Core Banking Systems) demandan un procesamiento de
transacciones financieras altamente confiable, con estricto apego a las propiedades ACID 
(Atomicidad, Consistencia, Aislamiento y Durabilidad).

El proyecto "FinCore" aborda y resuelve las problemáticas asociadas con la gestión de 
saldos e historiales bancarios, tales como las condiciones de carrera (race conditions) 
en transferencias simultáneas, errores de precisión decimal flotante y perdidas de 
trazabilidad. Mediante el bloqueo de filas a nivel de SQL (SELECT ... FOR UPDATE), el orden 
estricto de bloqueos de cuentas y el control manual de transacciones JDBC (commit / rollback),
"FinCore" garantiza que los movimientos monetarios sean completamente atómicos y persistentes 
entre reinicios.

## 4. Objetivos del Sistema

## 4.1 Objetivo General
Desarrollar y desplegar una aplicación bancaria funcional, robusta y escalable en Java 17, 
bajo una arquitectura por capas con persistencia relacional JDBC en H2, pool de conexiones 
DataSource (DBCP2), concurrencia multihilo y pruebas automatizadas JUnit 5/Mockito.

## 4.2 Objetivos Específicos

**OE1:** Aplicar los principios fundamentales de la Programación Orientada a Objetos (POO), 
incluyendo jerarquías de herencia unificadas (Persona ➔ Cliente / Empleado), clases abstractas, 
interfaces genéricas (CrudRepository<T, ID>) y polimorfismo.

**OE2:** Implementar la capa de acceso a datos mediante JDBC nativo con sentencias preparadas 
(PreparedStatement) y lectura por cursores (ResultSet) gestionados por DataSourceConfig.

**OE3:** Integrar la programación concurrente (ExecutorService, Runnable, CompletableFuture) 
para el procesamiento de reportes bancarios e intereses en segundo plano sin congelar la 
interfaz de usuario.

**OE4:** Validar el 100% de los componentes del sistema a través de una suite automatizada de 
18 clases de prueba con JUnit 5 y Mockito, garantizando la cobertura de los 17 escenarios 
funcionales de la matriz de pruebas.

5. Requerimientos e Historias de Usuario

**HU-001** (Registro de Cliente): Captura y validación de atributos del cliente con comprobación
estricta de DUI único (formato 12345678-9).

**HU-002** (Búsqueda y Consulta de Cliente): Búsqueda directa por DUI para desplegar información
general y cuentas asociadas.

**HU-003** (Creación de Cuenta Bancaria): Apertura de cuentas de Ahorro o Corriente asociadas a 
un cliente existente, con saldo inicial de $0.00 y estado activo.

**HU-004** (Consulta de Saldo): Lectura persistida del saldo disponible mediante tipo de dato 
BigDecimal.

**HU-005** (Depósito de Fondos): Incremento de saldo en una cuenta activa validando montos 
estrictamente positivos con hasta dos decimales.

**HU-006** (Retiro de Fondos): Débito de saldo validando previamente la existencia de fondos 
suficientes, evitando saldos negativos.

**HU-007** (Transferencia entre Cuentas - Operación Atómica):

Verificación de cuentas origen y destino distintas, existentes y activas.

Verificación de saldo disponible en la cuenta origen.

Ejecución atómica en BD: Bloqueo de filas (FOR UPDATE), débito en origen, crédito en
destino y registro de dos movimientos en la tabla transacciones dentro de una misma conexión
JDBC (commit único).

Rollback total ante cualquier fallo intermedio.

**HU-008** (Historial y Generación de Reportes): Auditoría append-only de movimientos y generación 
asíncrona de reportes de texto en segundo plano.

**6. Arquitectura del Proyecto y Estructura por Capas**

El repositorio FinCore-Avance3-Final se organiza formalmente bajo la estructura por capas 
estandarizada por Maven (src/main/java/com/sv/fincore/):

com.sv.fincore/
├── config/                  <-- (Utilidades)
├── controller/
│   ├── ClienteController.java      <-- Coordina solicitudes de clientes
│   ├── CuentaController.java       <-- Coordina operaciones de cuentas e historial
│   └── TransaccionController.java  <-- Entrada a depósitos, retiros y transferencias
├── dao/
│   ├── ReadRepository.java / WriteRepository.java / CrudRepository.java <-- Interfaces
│   ├── ClienteDAO.java             <-- CRUD JDBC de Clientes
│   ├── CuentaDAO.java              <-- Consultas, bloqueos FOR UPDATE y saldos
│   └── TransaccionDAO.java         <-- Historial JDBC append-only
├── exception/                      <-- Excepciones de negocio (SaldoInsuficienteException, etc.)
├── main/
│   ├── Main.java                   <-- Arranque de consola tradicional
│   └── WebMain.java                <-- Arranque del Servidor HTTP Web local
├── model/
│   ├── Persona.java                <-- Clase abstracta base
│   ├── Cliente.java                <-- Hereda de Persona
│   ├── Cuenta.java                 <-- Atributos con BigDecimal
│   └── Transaccion.java            <-- Subclases: Deposito, Retiro, Transferencia
├── service/
│   ├── ClienteService.java         <-- Reglas de clientes
│   ├── CuentaService.java          <-- Reglas bancarias, SQL atómico y tareas asíncronas
│   └── ProcesadorReportesTask.java <-- Tarea Runnable para reportes en segundo plano
├── ui/ & view/                     <-- Menús interactivos de consola y formateadores
├── util/
│   ├── DataSourceConfig.java       <-- Pool de conexiones DBCP2 (15 parámetros)
│   ├── ConexionDB.java             <-- Punto único de obtención de conexiones
│   └── SchemaInitializer.java      <-- Inicializador de schema.sql y data.sql
└── web/
├── FinCoreHttpServer.java      <-- Servidor HTTP local con API JSON
└── (resources/web): index.html, app.css, app.js, fincore-logo.png


## 7. Diagrama UML Actualizado del Modelo de Dominio

Diagrama de Clases del Modelo de Dominio e Integración de Capas FinCore

       +----------------------------------+
       |        <<abstract>>              |
       |             Persona              |
       +----------------------------------+
       | # String id                      |
       | # String nombre                  |
       | # String correo                  |
       | # String telefono                |
       | # String direccion               |
       +----------------------------------+
       | + getTipoPersona()* : String     |
       +-----------------+----------------+
                         |
         +---------------+---------------+
         |                               |
+--------+-------+              +--------+-------+
|    Cliente     |              |  <<abstract>>  |
|                |              |    Empleado    |
+----------------+              +----------------+
| - String dui   |              | # double salario|
+----------------+              +--------+-------+
|
+-------+-------+
|               |
+-------+-------+ +-----+---------+
| Administrador | |    Cajero     |
+---------------+ +---------------+

    Cliente "1" -------> "many" Cuenta
    Cuenta  "1" -------> "many" Transaccion


## 8. Programación Orientada a Objetos (POO) Aplicada

**Clase y Objeto:** Cuenta define la estructura; Cuenta c = new Cuenta("FIN-001", "12345678-9", 
"Ahorro") crea una instancia concreta con estado propio.

**Constructor:** Inicializa el estado mínimo. this.saldo = BigDecimal.ZERO; y this.activa = true; 
garantizan que toda cuenta inicie consistente.

**this y super:** super(id, nombre, correo, telefono, direccion); invoca la inicialización de la clase padre
Persona, mientras this.dui = dui; asigna el atributo propio de Cliente.

**Encapsulamiento:** Atributos declarados como private expuestos de forma defensiva mediante métodos 
getters/setters (getSaldo()).

**Herencia y Abstracción:** Cliente extends Persona reutiliza los campos comunes y concreta el método 
abstracto getTipoPersona() devolviendo "Cliente".

**Polimorfismo (Sobrescritura):** Invocación dinámica de p.getTipoPersona() sobre referencias de tipo 
persona respondiendo según la instancia real (Cliente o Empleado).

**Interfaces:** public interface CrudRepository<T, ID> define el contrato genérico desacoplado de la 
implementación SQL concreta.

## 9. Colecciones y Genéricos

El proyecto parametriza los repositorios e interfaces para evitar redundancia de código y garantizar 
seguridad 
de tipos en tiempo de compilación:

public interface CrudRepository<T, ID> extends ReadRepository<T, ID>, WriteRepository<T>, 
DeletableRepository<ID> {
// Hereda métodos parametrizados para la entidad T y su identificador ID
}

Asimismo, se utilizan colecciones genéricas como List<Cliente> para listados de entidades y Map<String, 
CompletableFuture<Path>> en FinCoreHttpServer para mapear el identificador de tareas de reporte con sus 
promesas asíncronas de ejecución.

## 10. Programación Concurrente e Integración con Base de Datos

La concurrencia en "FinCore" no es una prueba aislada; cumple la función de procesar reportes masivos e 
imposición hipotética de intereses en segundo plano:

ExecutorService: Administra un grupo de hilos (ThreadPool) reutilizables.

ProcesadorReportesTask: Clase que implementa Runnable para ejecutar el reporte bancario en segundo
plano.

CompletableFuture: CuentaService.generarReporteAsync() envía la tarea al ejecutor y devuelve una promesa
asíncrona. La interfaz web sondea el estado del reporte vía /api/reportes hasta su finalización.

Acceso Thread-Safe a BD: Cada hilo de ejecución solicita de manera independiente una conexión desde el 
pool DataSourceConfig, asegurando aislamiento y cierre automático.

**11. Persistencia mediante JDBC y Motor H2**

Toda la persistencia del sistema se realiza sobre una base de datos relacional H2 (almacenada físicamente 
en el archivo ./datos/fincore-demo.mv.db):

Sentencias Preparadas (PreparedStatement): Evitan ataques de inyección SQL al separar los parámetros con 
el carácter ? del cuerpo de la consulta.

**Bloqueos FOR UPDATE:** La consulta SELECT numero_cuenta, saldo FROM cuentas WHERE numero_cuenta = ? FOR 
UPDATE bloquea la fila SQL durante la transacción para evitar condiciones de carrera.

**Transacciones Atómicas:** connection.setAutoCommit(false); agrupa múltiples modificaciones. Si la 
transacción completa los débitos, créditos e inserción de historial, se invoca connection.commit(). 
Ante cualquier excepción se ejecuta connection.rollback().

## 12. Configuración y Parámetros del Pool DataSource (DBCP2)

La clase DataSourceConfig configura un pool Apache Commons DBCP2 (BasicDataSource) utilizando 15 
parámetros técnicos de optimización:

**validationQuery:** SELECT 1 (Sentencia SQL para validar la salud de la conexión).

**initialSize**: Número inicial de conexiones físicas a crear al arrancar.

**maxTotal:** Límite máximo de conexiones activas en el pool.

**minIdle:** Número mínimo de conexiones inactivas retenidas.

**maxIdle:** Límite máximo de conexiones inactivas permitidas.

**maxWaitMillis:** Tiempo máximo de espera en milisegundos para obtener una conexión libre.

**testOnBorrow:** true (Valida la conexión antes de entregarla a un hilo).

**testOnReturn:** false (Optimiza tiempo al devolver conexiones al pool).

**testWhileIdle:** true (Valida conexiones inactivas periódicamente en segundo plano).

**validationQueryTimeoutSeconds:** Tiempo de espera para la consulta de validación.

**timeBetweenEvictionRunsSeconds:** Intervalo entre ejecuciones del hilo limpiador de conexiones inútiles.

**minEvictableIdleTimeSeconds:** Tiempo de inactividad necesario para considerar expulsable una conexión.

numTestsPerEvictionRun: Cantidad de conexiones a examinar por ciclo de limpieza.

**removeAbandonedOnBorrow:** true (Recupera conexiones abandonadas por fugas de código).

**removeAbandonedTimeoutSeconds:** Tiempo para marcar una conexión como abandonada.

## 13. Cohesión y Acoplamiento

Alta Cohesión: CuentaService agrupa exclusivamente reglas bancarias y transaccionales, delegando la 
persistencia SQL a CuentaDAO y la recepción de datos a CuentaController.

Bajo Acoplamiento: La capa de servicio depende de las interfaces genéricas de repositorio 
(CrudRepository, ReadRepository), permitiendo intercambiar el motor de base de datos sin modificar la 
lógica de negocio.

## 14. Legibilidad y Reutilización de Código

Se eliminó el código duplicado en la captura de entradas por consola mediante la clase ConsolaBanco, 
estandarizando expresiones regulares para la validación de DUI e importes. Asimismo, la lógica bancaria 
reside centralizada en CuentaService, siendo consumida tanto por la interfaz de Consola (MenuPrincipal) 
como por la interfaz Web (FinCoreHttpServer), evitando duplicación de reglas.

## 15. Manejo de Excepciones y Control Transaccional

Se implementa una jerarquía de excepciones de negocio (SaldoInsuficienteException, CuentaNoEncontradaException, 
DatabaseConnectionException). Ante cualquier error de base de datos (SQLException), la capa de servicio captura
el fallo, ejecuta inmediatamente connection.rollback() para proteger el balance de las cuentas y traduce el 
error a una excepción de negocio legible para la interfaz.

## 16. Pruebas Unitarias e Integración con JUnit 5 y Mockito

El proyecto cuenta con 18 clases de prueba automatizadas bajo src/test/java/, habiendo registrado 100 pruebas
ejecutadas con 0 fallos y 0 errores:

JUnit 5: Verifica assertions exactas (assertEquals, assertThrows).

Mockito: Emplea @Mock e @InjectMocks para aislar la lógica de los controladores probando la interacción 
sin golpear la base de datos.

Pruebas de Atomicidad y Persistencia: CuentaServiceAtomicityTest verifica el funcionamiento del rollback
provocando fallos intermedios, mientras PersistenciaDiscoTest valida que los saldos persistan correctamente 
tras reiniciar la base de datos.

## 17. Control de Versiones con Git y GitHub

El proyecto se gestionó mediante un repositorio en GitHub bajo un flujo de trabajo estructurado por ramas
y Pull Requests, utilizando la convención Conventional Commits (feat:, fix:, refactor:, test:). El código
fuente se encuentra consolidado y funcional en la rama principal.


## 18. Conclusiones

1. **Arquitectura Desacoplada:** La separación en capas (Controller, Service, DAO, Model) en Java 17 
garantiza alta cohesión, bajo acoplamiento y reusabilidad entre la consola e interfaz web.
2. **Integridad Transaccional:** La migración a JDBC con H2 y transacciones atómicas (`commit`/`rollback`
con bloqueos `FOR UPDATE`) asegura la consistencia de saldos sin pérdidas ni duplicaciones.
3. **Concurrencia Eficiente:** El uso de `ExecutorService` y `CompletableFuture` con `DataSourceConfig` 
4. (DBCP2) permite procesar reportes masivos en segundo plano de forma *thread-safe* sin bloquear la 
interfaz.
4. **Calidad Certificada:** La suite automatizada con JUnit 5 y Mockito valida el 100% de las reglas de
negocio y los 17 escenarios de la matriz de pruebas sin fallos.
