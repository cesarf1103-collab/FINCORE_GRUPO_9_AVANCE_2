# FinCore — Avance 3

FinCore es una aplicación bancaria académica del Grupo 9 de Programación II. Permite gestionar clientes y cuentas, realizar depósitos, retiros y transferencias, consultar saldos e historiales y generar reportes en segundo plano. Incluye consola e interfaz web local, conectadas a los mismos servicios Java y a una base H2 mediante JDBC.

## Requisitos

- JDK 17 y Apache Maven 3.9 o posterior.
- `JAVA_HOME` debe apuntar al JDK 17; Java y Maven deben estar disponibles en `PATH`.
- Compruebe el entorno con `java -version`, `javac -version` y `mvn -version`.
- Las fuentes y los recursos de texto utilizan UTF-8.

## Arquitectura

El flujo es **vista → controlador → servicio → repositorio/DAO → JDBC/H2**. Los servicios validan las operaciones y los DAO ejecutan SQL parametrizado. `Persona` es la base de `Cliente` y de la jerarquía `modulo.empleado`; los repositorios usan contratos genéricos. Las transferencias bloquean las cuentas en orden estable y guardan ambos saldos y movimientos dentro de una sola transacción SQL, con `commit` o `rollback`. El historial bancario no permite eliminación física desde su repositorio.

```text
src/main/java/       Clases Java de la aplicación
src/main/resources/  schema.sql, data.sql y recursos web
src/test/java/       Pruebas JUnit y Mockito
```

El identificador Maven es `com.sv.fincore:fincore`. La interfaz web utiliza el servidor HTTP del JDK y Jackson; la persistencia usa H2 y Apache Commons DBCP2 como pool de conexiones.

## Compilar y ejecutar pruebas

Desde la raíz del proyecto:

```sh
mvn clean compile
mvn clean test
```

La suite contiene **120 pruebas en 21 clases**, incluidos los [17 escenarios funcionales](docs/Matriz_de_Pruebas.md). Surefire utiliza H2 en memoria; la prueba de persistencia tras reinicio crea su propio archivo en un directorio temporal de JUnit. Las pruebas no usan las bases locales de demostración. También se verifican Mockito, herencia, rollback, concurrencia y protección del historial.

Las 13 pruebas adicionales de servicios se distribuyen entre `ClienteServiceMockitoTest` (8) y `CuentaServiceMockitoTest` (5). Inyectan repositorios simulados para comprobar validaciones, delegación, rechazo de operaciones y conservación de causas de error sin abrir H2. Complementan las pruebas JDBC; no sustituyen las verificaciones de atomicidad y persistencia. La validación local del 24 de octubre de 2026 con Java 17.0.20.1 y Maven 3.9.16 terminó con **120 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas**.

## Consola

Ejecute `run.bat` en Windows o `sh run.sh` en un entorno compatible con Unix. Ambos compilan antes de iniciar. También puede usar `mvn compile exec:java`.

El menú ofrece **1 Clientes**, **2 Cuentas**, **3 Operaciones**, **4 Generar reporte** y **5 Salir**. Cree primero un cliente y luego sus cuentas. Los importes se manejan con `BigDecimal` y se presentan con dos decimales. La opción 4 genera `datos/reporte_bancario.txt` en segundo plano.

La simulación de intereses es opcional y de solo lectura. Tras compilar, se solicita con:

```sh
mvn exec:java "-Dexec.args=--simular-intereses"
```

El retiro presencial por consola solicita el DUI del titular y la confirmación del operador de que revisó el documento; un rechazo conserva saldo e historial. El alcance de esta comprobación se explica en las [historias de usuario](docs/Historias_de_Usuario.md).

## Interfaz web local

En Windows, haga doble clic en `Iniciar-FinCore.bat`. Comprueba Java 17 y Maven, compila y abre `http://127.0.0.1:8765/`. Si el navegador no se abre automáticamente, copie la dirección. Cierre el servidor con **Ctrl+C** en su ventana de consola. El iniciador también reconoce opcionalmente las herramientas portables en `%USERPROFILE%\Tools-FinCore`; no son necesarias si Java 17 y Maven ya están en `PATH`.

Para iniciar la web directamente con Maven:

```sh
mvn compile exec:java "-Dfincore.main.class=com.sv.fincore.web.WebMain"
```

La web permite registrar y editar clientes, crear y desactivar cuentas, operar, consultar historiales y descargar reportes bajo demanda. Usa el logo oficial y escucha solo en la computadora local. El retiro web es una operación general; no representa la verificación presencial de identidad ni autentica operadores.

Para comprobar el flujo bancario con datos de ejemplo, cree un cliente y dos cuentas: deposite $100 en A, retire $20 y transfiera $30 de A a B. Deben quedar $50 y $30, con tres movimientos en A y uno en B. Cierre y reinicie usando la misma base para consultar la persistencia. Utilice únicamente datos ficticios.

## Persistencia H2 y configuración

La consola y el arranque web directo usan `datos/fincore.mv.db` por defecto. `Iniciar-FinCore.bat` selecciona expresamente `datos/fincore-demo.mv.db`, conserva sus datos entre reinicios y habilita los datos de ejemplo. Ambas bases permanecen locales y excluidas de Git.

Al iniciar, `src/main/resources/schema.sql` crea las tablas H2 que falten sin borrar datos. `data.sql` es opcional e idempotente: `FINCORE_DB_SEED=true` incorpora un cliente, una cuenta con saldo cero y un usuario deshabilitado sin contraseña utilizable. El esquema no sustituye un sistema de migraciones para bases antiguas incompatibles.

Configure `FINCORE_DB_URL`, `FINCORE_DB_USER` y `FINCORE_DB_PASSWORD`; las propiedades JVM `fincore.db.url`, `fincore.db.user` y `fincore.db.password` tienen prioridad. `FINCORE_DB_INIT` controla la inicialización del esquema, habilitada por defecto para H2. Un motor SQL distinto requiere su controlador y validar la compatibilidad del esquema.

`DataSourceConfig` configura los 15 parámetros del pool:

| Parámetro | Predeterminado |
|---|---:|
| initialSize | 2 |
| maxTotal (maxActive en DBCP antiguo) | 10 |
| minIdle | 2 |
| maxIdle | 5 |
| maxWaitMillis | 5000 |
| testOnBorrow | true |
| testOnReturn | true |
| testWhileIdle | true |
| validationQuery | SELECT 1 |
| validationQueryTimeoutSeconds | 3 |
| timeBetweenEvictionRunsSeconds | 30 |
| minEvictableIdleTimeSeconds | 60 |
| numTestsPerEvictionRun | 3 |
| removeAbandonedOnBorrow | true |
| removeAbandonedTimeoutSeconds | 120 |

Se pueden ajustar mediante `FINCORE_POOL_*` o `fincore.pool.*`; por ejemplo, `FINCORE_POOL_MAX_TOTAL=20` o `-Dfincore.pool.maxTotal=20`. Cada préstamo de conexión se devuelve al pool al cerrar el recurso JDBC.

## Documentación del equipo

- [Informe académico final: arquitectura, UML y pruebas](Documentación_Final.md).
- [Historias de usuario y criterios](docs/Historias_de_Usuario.md).
- [Matriz de 17 escenarios funcionales](docs/Matriz_de_Pruebas.md).
- [Evidencias históricas de calidad y POO](docs/PruebasTecnicasPOO.md).
- [Contribuciones y transparencia](docs/Contribuciones_y_transparencia.md).

`target/`, bases locales, logs, cachés, configuraciones personales y secretos se excluyen mediante `.gitignore`. No deben publicarse credenciales ni datos reales.
