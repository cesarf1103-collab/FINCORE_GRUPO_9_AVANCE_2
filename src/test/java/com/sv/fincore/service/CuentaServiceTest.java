package com.sv.fincore.service;

import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final String DUI = "12345678-9";

    @Mock CuentaDAO cuentaDAO;
    @Mock ClienteService clienteService;

    CuentaService service;
    Cliente cliente;

    @BeforeEach
    void setUp() {
        when(cuentaDAO.listar()).thenReturn(new ArrayList<>());
        cliente = new Cliente("id-1", DUI, "Ana", "Perez", "ana@x.com",
                "12345678", "Calle 1", LocalDate.of(1990, 1, 1));
        lenient().when(clienteService.buscarPorDui(DUI)).thenReturn(cliente);
        service = new CuentaService(clienteService, cuentaDAO);
    }

    private void crear(String numero) {
        assertEquals("Cuenta creada exitosamente", service.crearCuenta(numero, DUI, "Ahorro"));
    }

    private void assertSaldo(String numero, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(service.buscarCuenta(numero).getSaldo()),
                "Saldo actual: " + service.buscarCuenta(numero).getSaldo());
    }

    // ---------- crearCuenta ----------

    @Test
    void crearCuenta_exitosa_persisteCuentaYActualizaCliente() throws Exception {
        crear("001");

        verify(cuentaDAO).guardar(any(Cuenta.class));
        verify(clienteService).guardarCliente(cliente);
        assertTrue(cliente.getNumerosCuenta().contains("001"));
        assertNotNull(service.buscarCuenta("001"));
    }

    @Test
    void crearCuenta_duplicada() {
        crear("001");

        assertEquals("Cuenta ya registrada", service.crearCuenta("001", DUI, "Ahorro"));
    }

    @Test
    void crearCuenta_clienteNoExiste() throws Exception {
        assertEquals("Cliente no encontrado", service.crearCuenta("001", "00000000-0", "Ahorro"));
        verify(cuentaDAO, never()).guardar(any());
    }

    @Test
    void crearCuenta_falloAlGuardarCuenta_revierte() throws Exception {
        doThrow(new IOException()).when(cuentaDAO).guardar(any(Cuenta.class));

        assertEquals("No se pudo guardar la cuenta", service.crearCuenta("001", DUI, "Ahorro"));
        verify(cuentaDAO).eliminar("001");
        assertNull(service.buscarCuenta("001"));
        assertTrue(cliente.getNumerosCuenta().isEmpty());
    }

    @Test
    void crearCuenta_falloAlGuardarCliente_revierteCuentaYNumero() throws Exception {
        doThrow(new IOException()).when(clienteService).guardarCliente(any());

        assertEquals("No se pudo guardar la cuenta", service.crearCuenta("001", DUI, "Ahorro"));
        verify(cuentaDAO).eliminar("001");
        assertNull(service.buscarCuenta("001"));
        assertTrue(cliente.getNumerosCuenta().isEmpty());
    }

    @Test
    void crearCuenta_fallaTambienLaReversion() throws Exception {
        doThrow(new IOException()).when(clienteService).guardarCliente(any());
        doThrow(new IOException()).when(cuentaDAO).eliminar("001");

        assertEquals("No se pudo completar ni revertir la creación de la cuenta",
                service.crearCuenta("001", DUI, "Ahorro"));
    }

    // ---------- depositar ----------

    @Test
    void depositar_exitoso() throws Exception {
        crear("001");

        assertEquals("Deposito exitoso", service.depositar("001", new BigDecimal("100")));
        assertSaldo("001", "100");
        assertEquals(1, service.obtenerHistorial("001").size());
        verify(cuentaDAO, atLeastOnce()).guardar(service.buscarCuenta("001"));
    }

    @Test
    void depositar_cuentaNoExiste() {
        assertEquals("Cuenta no encontrada", service.depositar("999", BigDecimal.TEN));
    }

    @Test
    void depositar_montoInvalido() {
        crear("001");

        assertEquals("Monto invalido", service.depositar("001", BigDecimal.ZERO));
        assertEquals("Monto invalido", service.depositar("001", new BigDecimal("-1")));
        assertEquals("Monto invalido", service.depositar("001", null));
    }

    @Test
    void depositar_cuentaInactiva() {
        crear("001");
        service.desactivarCuenta("001");

        assertEquals("Cuenta inactiva", service.depositar("001", BigDecimal.TEN));
    }

    @Test
    void depositar_falloDao_revierteSaldoEHistorial() throws Exception {
        crear("001");
        doThrow(new IOException()).when(cuentaDAO).guardar(any(Cuenta.class));

        assertEquals("No se pudo guardar el deposito", service.depositar("001", new BigDecimal("50")));
        assertSaldo("001", "0");
        assertTrue(service.obtenerHistorial("001").isEmpty());
    }

    // ---------- retirar ----------

    @Test
    void retirar_exitoso() {
        crear("001");
        service.depositar("001", new BigDecimal("100"));

        assertEquals("Retiro exitoso", service.retirar("001", new BigDecimal("30")));
        assertSaldo("001", "70");
        assertEquals(2, service.obtenerHistorial("001").size());
    }

    @Test
    void retirar_saldoInsuficiente() {
        crear("001");

        assertEquals("Saldo insuficiente", service.retirar("001", BigDecimal.TEN));
        assertTrue(service.obtenerHistorial("001").isEmpty());
    }

    @Test
    void retirar_validaciones() {
        crear("001");

        assertEquals("Cuenta no encontrada", service.retirar("999", BigDecimal.TEN));
        assertEquals("Monto invalido", service.retirar("001", BigDecimal.ZERO));
        service.desactivarCuenta("001");
        assertEquals("Cuenta inactiva", service.retirar("001", BigDecimal.TEN));
    }

    @Test
    void retirar_falloDao_restauraSaldo() throws Exception {
        crear("001");
        service.depositar("001", new BigDecimal("100"));
        doThrow(new IOException()).when(cuentaDAO).guardar(any(Cuenta.class));

        assertEquals("No se pudo guardar el retiro", service.retirar("001", new BigDecimal("30")));
        assertSaldo("001", "100");
        assertEquals(1, service.obtenerHistorial("001").size());
    }

    // ---------- transferir ----------

    @Test
    void transferir_exitoso_actualizaSaldosEHistoriales() {
        crear("001");
        crear("002");
        service.depositar("001", new BigDecimal("100"));

        assertEquals("Transferencia exitosa", service.transferir("001", "002", new BigDecimal("40")));
        assertSaldo("001", "60");
        assertSaldo("002", "40");
        assertEquals(2, service.obtenerHistorial("001").size()); // deposito + salida
        assertEquals(1, service.obtenerHistorial("002").size()); // entrada
    }

    @Test
    void transferir_validaciones() {
        crear("001");
        crear("002");

        assertEquals("Cuenta no encontrada", service.transferir("001", "999", BigDecimal.TEN));
        assertEquals("Las cuentas deben ser diferentes", service.transferir("001", "001", BigDecimal.TEN));
        assertEquals("Monto invalido", service.transferir("001", "002", BigDecimal.ZERO));
        assertEquals("Saldo insuficiente", service.transferir("001", "002", BigDecimal.TEN));
        service.desactivarCuenta("002");
        assertEquals("Cuenta inactiva", service.transferir("001", "002", BigDecimal.TEN));
    }

    @Test
    void transferir_falloDao_revierteAmbasCuentas() throws Exception {
        crear("001");
        crear("002");
        service.depositar("001", new BigDecimal("100"));
        doThrow(new IOException()).when(cuentaDAO).guardar(any(Cuenta.class));

        assertEquals("No se pudo guardar la transferencia",
                service.transferir("001", "002", new BigDecimal("40")));
        assertSaldo("001", "100");
        assertSaldo("002", "0");
        assertEquals(1, service.obtenerHistorial("001").size());
        assertTrue(service.obtenerHistorial("002").isEmpty());
    }

    // ---------- desactivar / historial ----------

    @Test
    void desactivarCuenta_exitosa() {
        crear("001");

        assertEquals("Cuenta desactivada", service.desactivarCuenta("001"));
        assertFalse(service.buscarCuenta("001").isActiva());
    }

    @Test
    void desactivarCuenta_noExiste() {
        assertEquals("Cuenta no encontrada", service.desactivarCuenta("999"));
    }

    @Test
    void desactivarCuenta_falloDao_vuelveAActivar() throws Exception {
        crear("001");
        doThrow(new IOException()).when(cuentaDAO).guardar(any(Cuenta.class));

        assertEquals("No se pudo guardar la desactivación", service.desactivarCuenta("001"));
        assertTrue(service.buscarCuenta("001").isActiva());
    }

    @Test
    void obtenerHistorial_cuentaInexistente_devuelveListaVacia() {
        assertTrue(service.obtenerHistorial("999").isEmpty());
    }
}
