# Pruebas técnicas de POO y evidencias de calidad

Las capturas conservadas proceden de la revisión anterior aportada por Iris. Documentan el trabajo de calidad del equipo y los problemas encontrados en aquella versión. **No son mediciones actuales de la rama integrada.** SonarQube, Qodana y MetricsReloaded deben ejecutarse nuevamente para obtener resultados de esta versión.

## Evidencias históricas

| Evidencia | Qué permite comprobar |
|---|---|
| [Resumen SonarQube](SonarResume.png) | Resultado histórico del proyecto analizado; muestra incidencias y cobertura reportada en esa ejecución. |
| [Incidencias SonarQube](SonarIssues.png) | Ejemplos de problemas de mantenibilidad y fiabilidad detectados. |
| [Qodana en IntelliJ](QodanaConsola.png) | Once hallazgos históricos, incluidos paquetes incorrectos y problemas de serialización. |
| [Resumen Qodana](qodanaReume.png) | Once hallazgos y una advertencia sobre 17 problemas de preparación que podían afectar al análisis. La advertencia no equivale a 17 fallos funcionales. |
| [Métricas de clases](Metrcis.png) | Valores históricos de CBO, DIT, LCOM, NOC, RFC y WMC; no deben citarse como métricas recalculadas de esta entrega. |
| [Estructura anterior](estructuraProyecto.png) | Organización de paquetes que motivó los ajustes de herencia y estructura Maven. |

El informe original registró LCOM = 1 para los servicios y señaló oportunidades de mejora en herencia y acoplamiento. Son resultados históricos y no demuestran por sí solos la calidad de la versión actual. Los documentos originales y la captura antigua de seis pruebas JUnit se preservaron en los materiales de exposición externos al repositorio.

## Evidencia en el código actual

- **Encapsulamiento:** `Cliente` y `Cuenta` protegen sus colecciones con vistas no modificables; [ClienteYCuentaTest](../src/test/java/com/sv/fincore/model/ClienteYCuentaTest.java) comprueba ese contrato.
- **Herencia y polimorfismo:** `Cliente` y `modulo.empleado.Empleado` heredan de `Persona`; los tres roles heredan de `Empleado`. [PersonaJerarquiaTest](../src/test/java/com/sv/fincore/audit/PersonaJerarquiaTest.java) comprueba la jerarquía. Los modelos de roles con nombres iguales en `com.sv.fincore.model` son clases históricas independientes con otros constructores y se conservan por compatibilidad; no son la jerarquía probada.
- **Cohesión y acoplamiento:** las vistas delegan en controladores, los servicios aplican reglas mediante interfaces de repositorio y los DAO ejecutan JDBC. `CuentaService` aún coordina reportes y el servidor web instancia un DAO para el resumen; esta descripción no constituye una medición nueva de LCOM o CBO.
- **Pruebas:** [ClienteControllerTest](../src/test/java/com/sv/fincore/controller/ClienteControllerTest.java) utiliza Mockito para verificar delegación. Los [17 escenarios](Matriz_de_Pruebas.md) y las pruebas H2 comprueban operaciones y persistencia. Las pruebas automatizadas no sustituyen un recorrido manual de todos los formularios.

## Configuración de Qodana

[qodana.yaml](../qodana.yaml) conserva el perfil `qodana.starter` y el analizador `jetbrains/qodana-jvm:2026.2`, con `projectJDK: "17"` para alinearse con Java 17. JetBrains documenta tanto esa versión del analizador como la descarga de JDK 17 mediante `projectJDK` en su [configuración de JVM](https://www.jetbrains.com/help/qodana/jvm.html#configuring-the-jdk).

Para obtener métricas actuales debe ejecutarse nuevamente el analizador con sus requisitos de instalación y licencia. No hay un resultado nuevo de análisis estático que permita declarar cerradas todas las observaciones de calidad.
