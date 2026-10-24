# Submenús y presentación monetaria — aporte de Elvis

| Antes | Integración final |
|---|---|
| Cada submenú repetía lectura de opciones y encabezados. | `MenuBase` concentra `imprimirMenu`, `leerOpcion` e `imprimirSeparador`. |
| Saldos e importes se imprimían sin formato uniforme. | `FormatoSalida` presenta `BigDecimal` como `$#,##0.00` y fechas en `dd/MM/yyyy HH:mm`. |
| Menús anteriores dependían directamente de servicios. | Se conservan `ClienteController`, `CuentaController` y `TransaccionController` del `main` actual. |

`FormatoSalida`, `MenuBase` y la versión compatible de `MenuClientes` provienen de `refactor/submenus-bigdecimal` de Elvis. `MenuCuentas` y `MenuOperaciones` aplican sus utilidades a los controladores actuales para no desplazar los cambios de #58 ni la persistencia de #62. La validación de entrada y de importes sigue en `ConsolaBanco` y `CuentaService`; el formato de salida no altera valores guardados.
