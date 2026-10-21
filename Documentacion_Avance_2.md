# UNIVERSIDAD EVANGÉLICA DE EL SALVADOR
**Facultad de Ingeniería - Desarrollo de Software y Ciencia de Datos**  
**Asignatura: Programación II  
**Proyecto:"FinCore" — **Avance 2**  


## 1. Integrantes del Equipo (Grupo 09)

| N° | Nombre | CIF | Rol Scrum | Rol Técnico |
| :---: | :--- | :---: | :---: | :---: |
| 1 | Samaria Isabel Gamez González | 2026011590 | Product Owner | Analista |
| 2 | César Fernando Estrada Martínez | 2026010835 | Scrum Master | Desarrollador Fullstack |
| 3 | Vidal Caleb Rodríguez Paulino | 2026011708 | Developer | Backend |
| 4 | Josué Isaac Elias Bonilla | 2026011141 | Developer | Backend |
| 5 | Emmanuel Tomas Contreras Valle | 2026011587 | Developer | Backend |
| 6 | José Efraín Silva Rodríguez | 2026011592 | Developer | Frontend |
| 7 | Elvis Adiel Flores | 2026011266 | Developer | Frontend |
| 8 | Julio Agustín Garay Miranda | 2026011159 | Developer | Frontend |
| 9 | Elmer Antonio Morales López | 2026010134 | Developer | Base de Datos |
| 10 | Jesse Maribel Mena Fuentes | 2026010775 | QA | Pruebas |
| 11 | Iris Yesenia Arteaga de Rodríguez | 2026010189 | QA | Pruebas |
| 12 | Nelson Alejandro Lopez Parada | 2026010069 | Architect | Arquitecto |


## 2. Introducción y Objetivos del Avance 2

### **Introducción**
El presente informe documenta el desarrollo e implementación del segundo avance del sistema bancario **"FinCore"**, un software en **Java 17** desacoplado que simula las operaciones esenciales de una entidad financiera[cite: 33, 34]. En este avance, el sistema evoluciona hacia una arquitectura por capas madura (`UI` / `View`, `Controller`, `Service`, `DAO` y `Model`)[cite: 37, 42], haciendo uso de los principios de Programación Orientada a Objetos, persistencia de datos local mediante archivos binarios `.dat`[cite: 36, 38, 39] y ejecuciones multihilo.

### **Objetivo General**
Desarrollar un sistema bancario que permita gestionar clientes, cuentas y transacciones (depósitos, retiros y transferencias), asegurando la integridad de saldos, el registro en historial, la persistencia local en archivos `.dat` y la ejecución concurrente de reportes mediante una arquitectura por capas desacoplada.

### **Objetivos Específicos (Segundo Avance)**
* **OE1:** Aplicar principios de POO (Encapsulamiento, Herencia con clases abstractas y Polimorfismo en operaciones bancarias).
* **OE2:** Implementar una arquitectura por capas desacoplada (`View` ➔ `Controller` ➔ `Service` ➔ `DAO` ➔ Archivos `.dat`)[cite: 37, 42].
* **OE3:** Garantizar la persistencia mediante archivos binarios en la carpeta `datos/` (`clientes.dat`, `cuentas.dat`, `transacciones.dat`)[cite: 36, 38, 39].
* **OE4:** Incorporar ejecuciones multihilo para la generación de reportes bancarios en segundo plano.


## 3. Requerimientos del Sistema (Historias de Usuario)

Con base en la especificación formal (`docs/Historias_de_Usuario.md`)[cite: 35], el sistema satisface las siguientes Historias de Usuario[cite: 35]:

* **HU-001 (Registro de Clientes):** Captura y valida datos del cliente (DUI, nombre, apellidos, correo, teléfono, dirección y fecha de nacimiento)[cite: 35], garantizando la unicidad del DUI.
* **HU-002 (Depósitos en Cuentas):** Ingreso de saldo a cuentas activas utilizando la clase `BigDecimal` para evitar imprecisiones financieras[cite: 35].
* **HU-003 (Retiro de Fondos):** Validación de existencia de cuenta y saldo disponible suficiente antes de debitar fondos[cite: 35].
* **HU-004 (Consulta de Saldo):** Muestra del saldo actual y datos generales de la cuenta bancaria[cite: 35].
* **HU-005 (Historial de Movimientos):** Consulta de la lista cronológica de transacciones con fecha, tipo, monto y saldo posterior[cite: 35].
* **HU-006 (Depósito a Terceros):** Permite realizar depósitos a cuentas propias o de otros usuarios dentro del sistema[cite: 35].
* **HU-007 (Retiro en Ventanilla / Desactivación):** Gestión presencial e inclusión de inactivación de cuentas (`desactivarCuenta`)[cite: 35].
* **HU-008 (Transferencias entre Cuentas - NUEVA):**
  * **Descripción:** Como cliente, quiero realizar transferencias entre cuentas para enviar fondos a otro usuario[cite: 35].
  * **Criterios de Aceptación:**
    1. Monto superior a $0.00[cite: 35].
    2. Saldo suficiente en la cuenta de origen[cite: 35].
    3. Cuenta destino existente y en estado activo[cite: 35].
    4. Registro atómico y simultáneo de débito en origen y crédito en destino[cite: 35].


## 4. Arquitectura del Sistema y Estructura de Paquetes

El proyecto está estructurado dentro del paquete base `com.sv.fincore`[cite: 37, 40, 42] e integrado mediante **Apache Maven** (`pom.xml`):

´´´text
FINCORE_GRUPO_9_AVANCE_2/
├── pom.xml (Java 17, UTF-8, JUnit Jupiter)
├── datos/
│   ├── clientes.dat
│   ├── cuentas.dat
│   └── transacciones.dat
├── docs/
│   ├── Historias_de_Usuario.md
│   ├── Matriz_de_Pruebas.md
│   ├── MavenResults.png
│   ├── Metrcis.png
│   ├── QodanaConsola.png
│   ├── SonarIssues.png
│   └── SonarResume.png
└── src/main/java/com/sv/fincore/
    ├── controller/ (ClienteController, CuentaController, TransaccionController)
    ├── dao/ (ClienteDAO, CuentaDAO, TransaccionDAO)
    ├── main/ (Main.java)
    ├── model/ (Cliente, Cuenta, Transaccion, Usuario, Administrador, Cajero, AtencionCliente)
    ├── service/ (ClienteService, CuentaService)
    ├── ui/ (ConsultaHistorial)
    ├── util/ (ArchivoUtil)
    └── view/ (MenuClientes, MenuCuentas, MenuOperaciones, MenuPrincipal)


##Detalle de Componentes por Capas:
1.Punto de Entrada (com.sv.fincore.main): Main.java orquesta el arranque de la aplicación, inicializando las capas DAO, de servicio, los controladores y la interfaz gráfica de consola (MenuPrincipal).
2.Configuración Maven (pom.xml): Proyecto configurado en Java 17 (maven.compiler.release = 17), codificación UTF-8 e inclusión del artefacto de pruebas junit-jupiter (v5.8.1)[cite: 33].
3.Capa Modelo (com.sv.fincore.model): Entidades del dominio bancario que implementan la interfaz Serializable para habilitar el almacenamiento binario (Cliente, Cuenta, Transaccion, Usuario, Administrador, AtencionCliente, Cajero).
4. Capa Acceso a Datos (com.sv.fincore.dao): Abstrae la persistencia hacia los archivos binarios de la carpeta datos/ utilizando Java Streams para consultas y búsquedas por DUI o número de cuenta (ClienteDAO, CuentaDAO, TransaccionDAO).
5.Capa Servicio (com.sv.fincore.service): Reglas de negocio y lógica financiera como el cálculo de saldos mediante la clase BigDecimal (ClienteService, CuentaService).
6.Capa Controlador y Vista (com.sv.fincore.controller / view / ui): Gestión del flujo de interacción por consola con menús especializados (MenuPrincipal, MenuClientes, MenuCuentas, MenuOperaciones y ConsultaHistorial).
7.Capa Utilidades (com.sv.fincore.util): ArchivoUtil.java proporciona métodos genéricos de serialización (guardar y cargar) respaldados por ObjectOutputStream y ObjectInputStream


## Modelo de Dominio y Relaciones entre Clases
El diagrama representa la jerarquía conceptual de herencia y las asociaciones principales del sistema:

classDiagram
    class Persona {
        <<abstract>>
        #String id
        #String nombre
        #String email
        #String telefono
        #String direccion
        +getTipoPersona() String*
    }
    class Cliente {
        -String dui
        -String apellidos
        -LocalDate fechaNacimiento
        -List~String~ numerosCuenta
        +agregarCuenta(String)
        +getTipoPersona() String
    }
    class Cuenta {
        -String numeroCuenta
        -String duiCliente
        -BigDecimal saldo
        -String tipoCuenta
        -boolean activa
        -List~Transaccion~ historial
        +agregarTransaccion(Transaccion)
    }
    class Transaccion {
        -String idTransaccion
        -String tipo
        -BigDecimal monto
        -BigDecimal saldoPosterior
        -String cuentaOrigen
        -String cuentaDestino
    }
    class Empleado {
        <<abstract>>
        #double salario
    }
    class Administrador
    class Cajero
    class AtencionCliente

    Persona <|-- Cliente
    Persona <|-- Empleado
    Empleado <|-- Administrador
    Empleado <|-- Cajero
    Empleado <|-- AtencionCliente
    Cliente "1" --> "many" Cuenta : posee
    Cuenta "1" --> "many" Transaccion : registra

## 6. Persistencia de datos

El sistema **no utiliza una base de datos**; en su lugar, persiste los objetos mediante **serialización binaria de Java** (`ObjectOutputStream` / `ObjectInputStream`), gestionada centralmente por `ArchivoUtil`:

| DAO | Archivo | Estructura en memoria |
|---|---|---|
| `ClienteDAO` | `datos/clientes.dat` | `Map<String, Cliente>` (clave = id) |
| `CuentaDAO` | `datos/cuentas.dat` | `Map<String, Cuenta>` (clave = número de cuenta) |
| `TransaccionDAO` | `datos/transacciones.dat` | `List<Transaccion>` |

**Manejo de errores:** cada operación de negocio (`ClienteService`, `CuentaService`) aplica el patrón de **compensación manual**: si la escritura en disco falla (`IOException`), el estado en memoria se revierte (por ejemplo, se resta el monto que se había sumado al saldo) antes de reportar el error al usuario, evitando así una inconsistencia entre la memoria y el archivo persistido.



## 7. Historias de usuario y criterios de aceptación

| ID | Historia | Criterios de aceptación clave |
|---|---|---|
| HU-001 | Registro de clientes | DUI único, formato `12345678-9`, campos obligatorios completos |
| HU-002 | Depósitos en cuentas | Cuenta activa, monto > $0.00 |
| HU-003 | Retiro de fondos | Saldo suficiente, monto > $0.00 |
| HU-004 | Consulta de saldo | Muestra saldo y datos actualizados de la cuenta |
| HU-005 | Historial de movimientos | Lista cronológica de transacciones por cuenta |
| HU-006 | Depósitos a terceros | Permite abonar a cuentas de otros clientes registrados |
| HU-007 | Retiro en ventanilla | Verifica identidad del titular antes del retiro presencial |
| HU-008 | Transferencias entre cuentas | Monto > $0, saldo suficiente, cuenta destino existente y activa, débito/crédito simultáneo |


## 8. Matriz de pruebas funcionales

17 casos de prueba documentados en `docs/Matriz_de_Pruebas.md`, cubriendo registro, depósitos, retiros y transferencias (casos exitosos y de error). Resumen de resultados:

| Estado | Cantidad de casos | Casos |
|---|---|---|
|  PASS | 15 | TC-001, TC-002, TC-005 a TC-017 |
|  FAIL | 2 | TC-003 (DUI con formato inválido), TC-004 (campos obligatorios vacíos) |

**Los 2 casos en FAIL indican una brecha real entre lo documentado y el comportamiento observado**: aunque `ConsolaBanco.dui()` y `ConsolaBanco.textoObligatorio()` sí validan formato y campos vacíos a nivel de consola, la matriz reporta que estos escenarios fallaron en la ejecución registrada — se recomienda re-ejecutar y confirmar si el problema persiste en la versión actual del código antes de cerrar el avance.


## 9. Verificación de principios de POO

Según `docs/PruebasTecnicasPOO.md`, el equipo aplicó tres tipos de análisis estático:

1. **Encapsulamiento:** 11 clases revisadas (`Administrador`, `AtencionCliente`, `Cajero`, `Cliente`, `Transaccion`, `Usuario`, los 3 DAO y los 2 Service) — todas con atributos privados y acceso vía getters/setters: **PASS**.
2. **Herencia y polimorfismo:** análisis con Maven + SonarQube y Qodana (17 issues detectados). Se identificó que la estructura inicial no aprovechaba la herencia; se propuso migrar todo a `com.sv.fincore.model` con `Persona` como clase abstracta base.
3. **Cohesión y acoplamiento (plugin MetricsReloaded):** DIT (Depth of Inheritance Tree) = 1 en varias clases, evidenciando la falta de herencia real en la implementación efectiva; LCOM = 1 en `ClienteService` y `CuentaService`, lo que refleja buena cohesión en la lógica de negocio.


## 10. Hallazgos técnicos y recomendaciones

| # | Hallazgo | Riesgo | Recomendación |
|---|---|---|---|
| 1 | Duplicidad de modelos: `Administrador`, `Cajero`, `AtencionCliente` y `Usuario` existen tanto en `com.sv.fincore.model` (sin herencia) como en `modulo.empleado`/`modulo.persona` (con herencia de `Persona`) | Alto — confusión sobre cuál clase es la "oficial"; la herencia documentada no se refleja en el modelo realmente usado por los servicios | Unificar ambas jerarquías bajo `com.sv.fincore.model`, haciendo que `Cliente` y los roles de empleado hereden de una única clase `Persona` |
| 2 | Clases sueltas `Cliente.java` y `Usuario.java` en la **raíz del repositorio**, sin paquete, con estructura distinta a las versiones oficiales | Medio — código muerto/confuso que puede compilarse por error junto con el resto | Eliminar o mover a una carpeta de ejercicios anteriores fuera del código fuente activo |
| 3 | `pom.xml` ubicado dentro de `com/sv/fincore/` en lugar de la raíz del proyecto | Alto — Maven espera el `pom.xml` en la raíz para poder ejecutar `mvn clean verify`, `mvn package`, etc. | Mover `pom.xml` a la raíz del repositorio |
| 4 | No existen clases de prueba (JUnit está declarado como dependencia pero no hay carpeta `src/test`) | Medio — las pruebas funcionales de la Matriz de Pruebas son manuales, no automatizadas | Crear pruebas unitarias con JUnit 5 para `ClienteService` y `CuentaService`, especialmente para los casos TC-003 y TC-004 que están en FAIL |
| 5 | `frontend validaciones.html` es un mockup visual estático sin conexión real al backend Java | Bajo — puede generar expectativa de una interfaz funcional | Aclarar en la documentación que es solo un prototipo visual de referencia |
| 6 | El modelo `Usuario` (autenticación) no está integrado a ningún flujo de la aplicación | Bajo/Medio | Definir si la autenticación es parte del alcance de un próximo avance |

---

## 11. Manual de compilación y ejecución

>  Debido al hallazgo #3, antes de compilar mueve `pom.xml` desde `com/sv/fincore/pom.xml` hacia la raíz del proyecto.

**Requisitos:** JDK 17 o superior, Maven 3.8+.

```bash
# 1. Ubicarse en la raíz del proyecto (con pom.xml ya movido a la raíz)
cd FINCORE_GRUPO_9_AVANCE_2

# 2. Compilar
mvn clean compile

# 3. Ejecutar la clase principal
mvn exec:java -Dexec.mainClass="com.sv.fincore.main.Main"
```

Al ejecutarse, la aplicación crea automáticamente la carpeta `datos/` en el directorio de trabajo, donde se almacenan los archivos `clientes.dat`, `cuentas.dat` y `transacciones.dat`.


## 12. Conclusiones

El Avance 2 de **FinCore** consolida una arquitectura por capas clara y funcional para las operaciones bancarias centrales (registro de clientes, cuentas, depósitos, retiros y transferencias), con persistencia en archivos y manejo defensivo de errores mediante compensación de estado. El equipo ya identificó proactivamente, mediante análisis estático (SonarQube, Qodana, MetricsReloaded), la principal deuda técnica del proyecto: la falta de una jerarquía de herencia unificada entre los modelos de `com.sv.fincore.model` y `modulo.persona`/`modulo.empleado`. Resolver esta duplicidad, junto con reubicar el `pom.xml` y limpiar las clases sueltas de la raíz, son los pasos más importantes antes del próximo avance.
