package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CuentaServiceTest {

    private ClienteService clienteService;
    private CuentaService cuentaService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService();
        cuentaService = new CuentaService(clienteService);

        cliente = new Cliente(
            "1",
            "12345678-9",
            "Ana",
            "Lopez",
            "ana@gmail.com",
            "77778888",
            "San Salvador",
            LocalDate.of(2000, 5, 10)
        );

        clienteService.registrarCliente(cliente);
    }

    @Test
    void crearCuentaExitosamente() {
        String resultado = cuentaService.crearCuenta(
            "1001", "12345678-9", "Ahorro"
        );

        assertEquals("Cuenta creada exitosamente", resultado);
        assertNotNull(cuentaService.buscarCuenta("1001"));
    }

    @Test
    void rechazarCuentaConNumeroDuplicado() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");

        String resultado = cuentaService.crearCuenta(
            "1001", "12345678-9", "Corriente"
        );

        assertEquals("Cuenta ya registrada", resultado);
    }

    @Test
    void rechazarCuentaSiClienteNoExiste() {
        String resultado = cuentaService.crearCuenta(
            "1002", "00000000-0", "Ahorro"
        );

        assertEquals("Cliente no encontrado", resultado);
    }

    @Test
    void depositarExitosamente() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");

        String resultado = cuentaService.depositar(
            "1001", new BigDecimal("100.00")
        );

        assertEquals("Deposito exitoso", resultado);
        assertEquals(
            0,
            cuentaService.buscarCuenta("1001")
                .getSaldo().compareTo(new BigDecimal("100.00"))
        );
    }

    @Test
    void rechazarDepositoEnCuentaInexistente() {
        String resultado = cuentaService.depositar(
            "9999", new BigDecimal("50.00")
        );

        assertEquals("Cuenta no encontrada", resultado);
    }

    @Test
    void rechazarDepositoConMontoInvalido() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");

        String resultado = cuentaService.depositar(
            "1001", BigDecimal.ZERO
        );

        assertEquals("Monto invalido", resultado);
    }

    @Test
    void rechazarRetiroPorSaldoInsuficiente() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");

        String resultado = cuentaService.retirar(
            "1001", new BigDecimal("50.00")
        );

        assertEquals("Saldo insuficiente", resultado);
    }

    @Test
    void rechazarOperacionEnCuentaInactiva() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");
        cuentaService.desactivarCuenta("1001");

        String resultado = cuentaService.depositar(
            "1001", new BigDecimal("50.00")
        );

        assertEquals("Cuenta inactiva", resultado);
    }

    @Test
    void rechazarTransferenciaPorSaldoInsuficiente() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");
        cuentaService.crearCuenta("1002", "12345678-9", "Corriente");

        String resultado = cuentaService.transferir(
            "1001", "1002", new BigDecimal("50.00")
        );

        assertEquals("Saldo insuficiente", resultado);
    }

    @Test
    void desactivarCuentaExitosamente() {
        cuentaService.crearCuenta("1001", "12345678-9", "Ahorro");

        String resultado = cuentaService.desactivarCuenta("1001");
        Cuenta cuenta = cuentaService.buscarCuenta("1001");

        assertEquals("Cuenta desactivada", resultado);
        assertFalse(cuenta.isActiva());
    }

    @Test
    void rechazarTransaccionNula() {
        Cuenta cuenta = new Cuenta(
        "1001",
        "12345678-9",
        "Ahorro"
        );

        assertThrows(
        IllegalArgumentException.class,
        () -> cuenta.agregarTransaccion(null)
        );
    }
}