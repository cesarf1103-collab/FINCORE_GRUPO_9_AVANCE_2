package com.sv.fincore.integration;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import com.sv.fincore.util.DataSourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaDiscoTest {
    @Test
    void esquemaDefinitivoYPersistenciaTrasReabrirPool(@TempDir Path temporal) throws Exception {
        String anterior = System.getProperty("fincore.db.url");
        String seedAnterior = System.getProperty("fincore.db.seed");
        Path archivo = temporal.resolve("fincore_prueba");
        try {
            DataSourceConfig.cerrar();
            System.setProperty("fincore.db.url", "jdbc:h2:file:" + archivo.toAbsolutePath());
            System.setProperty("fincore.db.seed", "true");
            ClienteService clientes = new ClienteService();
            Cliente cliente = new Cliente("ARCHIVO-CLIENTE", "87654321-0", "Prueba", "Disco",
                    "disco@example.invalid", "77778888", "Temporal", LocalDate.of(1990, 1, 1));
            assertEquals("Cliente registrado exitosamente", clientes.registrarCliente(cliente));
            try (CuentaService cuentas = new CuentaService(clientes)) {
                assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta("ARCHIVO-A", cliente.getDui(), "Ahorro"));
                assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta("ARCHIVO-B", cliente.getDui(), "Ahorro"));
                assertEquals("Deposito exitoso", cuentas.depositar("ARCHIVO-A", new BigDecimal("100.00")));
                assertEquals("Transferencia exitosa", cuentas.transferir("ARCHIVO-A", "ARCHIVO-B", new BigDecimal("25.00")));
            }
            DataSourceConfig.cerrar();
            assertTrue(Files.exists(Path.of(archivo + ".mv.db")));
            try (CuentaService reabierto = new CuentaService(new ClienteService())) {
                assertEquals(new BigDecimal("75.00"), reabierto.buscarCuenta("ARCHIVO-A").getSaldo());
                assertEquals(new BigDecimal("25.00"), reabierto.buscarCuenta("ARCHIVO-B").getSaldo());
                assertEquals(2, reabierto.obtenerHistorial("ARCHIVO-A").size());
                assertEquals(1, reabierto.obtenerHistorial("ARCHIVO-B").size());
                assertNotNull(reabierto.obtenerHistorial("ARCHIVO-B").get(0).getFecha());
                assertEquals("ARCHIVO-A", reabierto.obtenerHistorial("ARCHIVO-B").get(0).getCuentaOrigen());
            }
            try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
                for (String tabla : new String[] {"CLIENTES", "CUENTAS", "TRANSACCIONES", "USUARIOS"}) {
                    try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                        assertTrue(r.next());
                    }
                }
                try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM usuarios WHERE id = 'demo-user-disabled' AND activo = FALSE")) {
                    assertTrue(r.next());
                    assertEquals(1, r.getInt(1), "data.sql debe ser idempotente al reiniciar");
                }
            }
        } finally {
            DataSourceConfig.cerrar();
            if (anterior == null) System.clearProperty("fincore.db.url");
            else System.setProperty("fincore.db.url", anterior);
            if (seedAnterior == null) System.clearProperty("fincore.db.seed");
            else System.setProperty("fincore.db.seed", seedAnterior);
        }
    }
}
