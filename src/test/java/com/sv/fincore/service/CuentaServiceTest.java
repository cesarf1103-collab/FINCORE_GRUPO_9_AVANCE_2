package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Deposito;
import com.sv.fincore.dao.ClienteDAO;
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

public class CuentaServiceTest {

    private ClienteService clienteService;
    private CuentaService cuentaService;
    private Cliente cliente;
    private String dui;
    private String cuenta1;
    private String cuenta2;
    private String cuentaInexistente;
    private String duiInexistente;

    @BeforeEach
    void setUp() {
        dui = String.format("%08d-9", Math.floorMod(UUID.randomUUID().hashCode(), 100000000));
        duiInexistente = UUID.randomUUID().toString();
        cuenta1 = UUID.randomUUID().toString();
        cuenta2 = UUID.randomUUID().toString();
        cuentaInexistente = UUID.randomUUID().toString();
        clienteService = new ClienteService();
        cuentaService = new CuentaService(clienteService);

        cliente = new Cliente(
            UUID.randomUUID().toString(),
            dui,
            "Ana",
            "Lopez",
            "ana@gmail.com",
            "77778888",
            "San Salvador",
            LocalDate.of(2000, 5, 10)
        );

        clienteService.registrarCliente(cliente);
    }

    @AfterEach
    void limpiarDatos() throws Exception {
        // Solo elimina los movimientos de las cuentas únicas de esta prueba H2.
        try (Connection c = ConexionDB.obtenerConexion();
             PreparedStatement p = c.prepareStatement("DELETE FROM transacciones WHERE numero_cuenta IN (?, ?)")) {
            p.setString(1, cuenta1);
            p.setString(2, cuenta2);
            p.executeUpdate();
        }
        // La limpieza de H2 no usa el DAO de negocio, que protege saldos e historial.
        try (Connection c = ConexionDB.obtenerConexion();
             PreparedStatement p = c.prepareStatement("DELETE FROM cuentas WHERE numero_cuenta IN (?, ?)")) {
            p.setString(1, cuenta1);
            p.setString(2, cuenta2);
            p.executeUpdate();
        }
        new ClienteDAO().eliminar(cliente.getId());
        cuentaService.close();
    }
    @Test
    void crearCuentaExitosamente() {
        String resultado = cuentaService.crearCuenta(
            cuenta1, dui, "Ahorro"
        );

        assertEquals("Cuenta creada exitosamente", resultado);
        assertNotNull(cuentaService.buscarCuenta(cuenta1));
    }

    @Test
    void rechazarCuentaConNumeroDuplicado() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");

        String resultado = cuentaService.crearCuenta(
            cuenta1, dui, "Corriente"
        );

        assertEquals("Cuenta ya registrada", resultado);
    }

    @Test
    void rechazarCuentaSiClienteNoExiste() {
        String resultado = cuentaService.crearCuenta(
            cuenta2, duiInexistente, "Ahorro"
        );

        assertEquals("Cliente no encontrado", resultado);
    }

    @Test
    void depositarExitosamente() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");

        String resultado = cuentaService.depositar(
            cuenta1, new BigDecimal("100.00")
        );

        assertEquals("Deposito exitoso", resultado);
        assertEquals(
            0,
            cuentaService.buscarCuenta(cuenta1)
                .getSaldo().compareTo(new BigDecimal("100.00"))
        );
    }

    @Test
    void rechazarDepositoEnCuentaInexistente() {
        String resultado = cuentaService.depositar(
            cuentaInexistente, new BigDecimal("50.00")
        );

        assertEquals("Cuenta no encontrada", resultado);
    }

    @Test
    void rechazarDepositoConMontoInvalido() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");

        String resultado = cuentaService.depositar(
            cuenta1, BigDecimal.ZERO
        );

        assertEquals("Monto invalido", resultado);
    }

    @Test
    void rechazarRetiroPorSaldoInsuficiente() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");

        String resultado = cuentaService.retirar(
            cuenta1, new BigDecimal("50.00")
        );

        assertEquals("Saldo insuficiente", resultado);
    }

    @Test
    void rechazarOperacionEnCuentaInactiva() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");
        cuentaService.desactivarCuenta(cuenta1);

        String resultado = cuentaService.depositar(
            cuenta1, new BigDecimal("50.00")
        );

        assertEquals("Cuenta inactiva", resultado);
    }

    @Test
    void rechazarTransferenciaPorSaldoInsuficiente() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");
        cuentaService.crearCuenta(cuenta2, dui, "Corriente");

        String resultado = cuentaService.transferir(
            cuenta1, cuenta2, new BigDecimal("50.00")
        );

        assertEquals("Saldo insuficiente", resultado);
    }

    @Test
    void desactivarCuentaExitosamente() {
        cuentaService.crearCuenta(cuenta1, dui, "Ahorro");

        String resultado = cuentaService.desactivarCuenta(cuenta1);
        Cuenta cuenta = cuentaService.buscarCuenta(cuenta1);

        assertEquals("Cuenta desactivada", resultado);
        assertFalse(cuenta.isActiva());
    }

    @Test
    void agregarTransaccionValida() {
        Cuenta cuenta = new Cuenta(cuenta1, dui, "Ahorro");
        Deposito transaccion = new Deposito(
            UUID.randomUUID().toString(),
            cuenta1,
            new BigDecimal("10.00"),
            "Prueba"
        );
        cuenta.agregarTransaccion(transaccion);
        assertEquals(1, cuenta.getHistorial().size());
        assertSame(transaccion, cuenta.getHistorial().get(0));
    }
}
