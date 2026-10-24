package com.sv.fincore.integration;

import com.sv.fincore.dao.TransaccionDAO;
import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Usa la H2 en memoria configurada por Surefire; elimina solo sus registros. */
class ClienteYHistorialConsistenciaTest {
    private final String sufijo = UUID.randomUUID().toString().substring(0, 8);
    private final String dui = String.format("%08d-7", Math.floorMod(sufijo.hashCode(), 100_000_000));
    private final String numero = "CONS-" + sufijo;
    private final String idCliente = "CLIENTE-" + sufijo;
    private ClienteService clientes;
    private CuentaService cuentas;

    @BeforeEach
    void preparar() {
        clientes = new ClienteService();
        cuentas = new CuentaService(clientes);
        assertEquals("Cliente registrado exitosamente", clientes.registrarCliente(cliente()));
    }

    @AfterEach
    void limpiar() throws Exception {
        cuentas.close();
        try (Connection c = ConexionDB.obtenerConexion()) {
            ejecutar(c, "DELETE FROM transacciones WHERE numero_cuenta = ?", numero);
            ejecutar(c, "DELETE FROM cuentas WHERE numero_cuenta = ?", numero);
            ejecutar(c, "DELETE FROM clientes WHERE id = ?", idCliente);
        }
    }

    @Test
    void otraInstanciaLeeCambiosPersistidosSinReiniciar() {
        ClienteService otra = new ClienteService();
        assertEquals(1, otra.listarClientes().stream().filter(c -> dui.equals(c.getDui())).count());
        assertEquals("Cliente actualizado", clientes.modificarCliente(dui, "Berta", "Prueba",
                "berta@example.invalid", "12345678", "Temporal"));
        assertEquals("Berta", otra.buscarPorDui(dui).getNombre());
    }

    @Test
    void servicioRechazaDatosInvalidosSinModificarLaBase() {
        Cliente invalido = new Cliente("OTRO-" + sufijo, "DUI-INVALIDO", "Nombre", "Apellido",
                "correo@example.invalid", "12345678", "Temporal", LocalDate.of(2000, 1, 1));
        assertEquals("Datos de cliente invalidos", clientes.registrarCliente(invalido));
        assertEquals("Datos de cliente invalidos", clientes.modificarCliente(dui, "Nuevo", "Apellido",
                "sin-correo", "12345678", "Temporal"));
        assertEquals("Ana", new ClienteService().buscarPorDui(dui).getNombre());
        assertNull(new ClienteService().buscarPorDui("DUI-INVALIDO"));
        assertEquals("Datos de cuenta invalidos", cuentas.crearCuenta(numero, dui, "Plazo fijo"));
        assertNull(cuentas.buscarCuenta(numero));
    }

    @Test
    void clienteConCuentaNoSeEliminaYLaRelacionSeConsultaDesdeSql() {
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(numero, dui, "Ahorro"));
        assertEquals(dui, new ClienteService().buscarPorNumeroCuenta(numero).getDui());
        assertEquals("No se puede eliminar un cliente con cuentas asociadas", clientes.eliminarCliente(dui));
        assertNotNull(new ClienteService().buscarPorDui(dui));
    }

    @Test
    void historialSqlSeEntregaEnOrdenCronologico() throws Exception {
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(numero, dui, "Ahorro"));
        try (Connection c = ConexionDB.obtenerConexion()) {
            insertarMovimiento(c, "NUEVO-" + sufijo, "2026-10-21 10:00:00");
            insertarMovimiento(c, "VIEJO-" + sufijo, "2026-10-20 10:00:00");
        }
        var historial = new TransaccionDAO().listarPorCuenta(numero);
        assertEquals(2, historial.size());
        assertEquals("VIEJO-" + sufijo, historial.get(0).idTransaccion());
        assertEquals("NUEVO-" + sufijo, historial.get(1).idTransaccion());
    }

    @Test
    void cuentaConMovimientoNoPuedeBorrarseAunqueVuelvaASaldoCero() throws Exception {
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(numero, dui, "Ahorro"));
        assertEquals("Deposito exitoso", cuentas.depositar(numero, new BigDecimal("10.00")));
        assertEquals("Retiro exitoso", cuentas.retirar(numero, new BigDecimal("10.00")));
        assertFalse(new CuentaDAO().eliminar(numero));
        assertNotNull(cuentas.buscarCuenta(numero));
        assertEquals(2, cuentas.obtenerHistorial(numero).size());
    }

    @Test
    void retiroEnVentanillaExigeConfirmacionYCoincidenciaDeDui() {
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(numero, dui, "Ahorro"));
        assertEquals("Deposito exitoso", cuentas.depositar(numero, new BigDecimal("50.00")));
        assertEquals("Verificacion de identidad requerida", cuentas.retirarEnVentanilla(
                numero, new BigDecimal("10.00"), dui, false));
        assertEquals("DUI no corresponde al titular", cuentas.retirarEnVentanilla(
                numero, new BigDecimal("10.00"), "00000000-0", true));
        assertEquals(0, new BigDecimal("50.00").compareTo(cuentas.buscarCuenta(numero).getSaldo()));
        assertEquals(1, cuentas.obtenerHistorial(numero).size());
        assertEquals("Retiro exitoso", cuentas.retirarEnVentanilla(
                numero, new BigDecimal("10.00"), dui, true));
        assertEquals(0, new BigDecimal("40.00").compareTo(cuentas.buscarCuenta(numero).getSaldo()));
        assertEquals(2, cuentas.obtenerHistorial(numero).size());
    }

    private Cliente cliente() {
        return new Cliente(idCliente, dui, "Ana", "Prueba", "ana@example.invalid",
                "12345678", "Temporal", LocalDate.of(2000, 1, 1));
    }

    private static void ejecutar(Connection c, String sql, String valor) throws Exception {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, valor);
            s.executeUpdate();
        }
    }

    private void insertarMovimiento(Connection c, String id, String fecha) throws Exception {
        try (PreparedStatement s = c.prepareStatement("""
                INSERT INTO transacciones
                (id_transaccion, numero_cuenta, tipo, monto, saldo_posterior, descripcion, fecha)
                VALUES (?, ?, 'DEPOSITO', 1.00, 1.00, 'Prueba de orden', ?)
                """)) {
            s.setString(1, id);
            s.setString(2, numero);
            s.setTimestamp(3, java.sql.Timestamp.valueOf(fecha));
            s.executeUpdate();
        }
    }
}
