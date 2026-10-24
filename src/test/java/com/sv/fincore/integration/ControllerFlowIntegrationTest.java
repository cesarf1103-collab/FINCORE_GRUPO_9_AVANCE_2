package com.sv.fincore.integration;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ControllerFlowIntegrationTest {
    @Test
    void existingControllersKeepCompleteBankingFlow() throws Exception {
        String id = UUID.randomUUID().toString();
        String dui = String.format("%08d-9", Math.floorMod(UUID.randomUUID().hashCode(), 100000000));
        String origen = UUID.randomUUID().toString();
        String destino = UUID.randomUUID().toString();
        try {
            ClienteService clientes = new ClienteService();
            try (CuentaService cuentas = new CuentaService(clientes)) {
                ClienteController clienteController = new ClienteController(clientes);
                CuentaController cuentaController = new CuentaController(cuentas);
                TransaccionController movimientos = new TransaccionController(cuentas);
                assertEquals("Cliente registrado exitosamente", clienteController.registrarCliente(
                    new Cliente(id, dui, "Test", "Controller", "test@example.test", "00000000",
                        "Temporal", LocalDate.of(2000, 1, 1))));
                assertEquals("Cuenta creada exitosamente", cuentaController.crearCuenta(origen, dui, "Ahorro"));
                assertEquals("Cuenta creada exitosamente", cuentaController.crearCuenta(destino, dui, "Corriente"));
                assertNotNull(clienteController.buscarPorNumeroCuenta(origen));
                assertEquals("Deposito exitoso", movimientos.depositar(origen, new BigDecimal("100.00")));
                assertEquals("Retiro exitoso", movimientos.retirar(origen, new BigDecimal("10.00")));
                assertEquals("Transferencia exitosa", movimientos.transferir(origen, destino, new BigDecimal("25.00")));
                assertEquals(0, new BigDecimal("65.00").compareTo(cuentaController.buscarCuenta(origen).getSaldo()));
                assertEquals(0, new BigDecimal("25.00").compareTo(cuentaController.buscarCuenta(destino).getSaldo()));
                assertEquals(3, cuentaController.obtenerHistorial(origen).size());
                assertEquals(1, cuentaController.obtenerHistorial(destino).size());
                assertEquals("Cuenta desactivada", cuentaController.desactivarCuenta(destino));
                assertEquals("Cuenta inactiva", movimientos.depositar(destino, BigDecimal.ONE));
            }
        } finally {
            try (Connection conn = ConexionDB.obtenerConexion()) {
                for (String sql : new String[]{
                        "DELETE FROM transacciones WHERE numero_cuenta IN (?, ?)",
                        "DELETE FROM cuentas WHERE numero_cuenta IN (?, ?)"}) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, origen);
                        stmt.setString(2, destino);
                        stmt.executeUpdate();
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clientes WHERE id = ?")) {
                    stmt.setString(1, id);
                    stmt.executeUpdate();
                }
            }
        }
    }
}
