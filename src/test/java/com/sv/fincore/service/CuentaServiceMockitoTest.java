package com.sv.fincore.service;

import com.sv.fincore.dao.ClienteRepository;
import com.sv.fincore.dao.CuentaRepository;
import com.sv.fincore.dao.TransaccionRegistro;
import com.sv.fincore.dao.TransaccionRepository;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** Solo operaciones aislables mediante repositorios; no simula transacciones JDBC. */
@ExtendWith(MockitoExtension.class)
class CuentaServiceMockitoTest {
    private static final String DUI = "12345678-9";
    private static final String NUMERO = "CTA-001";

    @Mock ClienteRepository clienteRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock TransaccionRepository transaccionRepository;

    private CuentaService service;

    @BeforeEach
    void preparar() {
        service = new CuentaService(cuentaRepository, transaccionRepository,
                new ClienteService(clienteRepository));
    }

    @AfterEach
    void cerrar() {
        service.close();
    }

    @Test
    void crearCuentaGuardaUnaCuentaActivaConSaldoCero() throws SQLException {
        Cliente cliente = new Cliente("cliente-1", DUI, "Ana", "Perez", "ana@example.invalid",
                "12345678", "Calle 1", LocalDate.of(1990, 1, 1));
        when(clienteRepository.buscarPorDui(DUI)).thenReturn(cliente);

        assertEquals("Cuenta creada exitosamente", service.crearCuenta(NUMERO, DUI, "Ahorro"));

        ArgumentCaptor<Cuenta> guardada = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).buscarPorNumero(NUMERO);
        verify(clienteRepository).buscarPorDui(DUI);
        verify(cuentaRepository).guardar(guardada.capture());
        Cuenta cuenta = guardada.getValue();
        assertAll(
                () -> assertEquals(NUMERO, cuenta.getNumeroCuenta()),
                () -> assertEquals(DUI, cuenta.getDuiCliente()),
                () -> assertEquals("Ahorro", cuenta.getTipoCuenta()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getSaldo())),
                () -> assertTrue(cuenta.isActiva()),
                () -> assertTrue(cliente.getNumerosCuenta().contains(NUMERO)));
        verifyNoMoreInteractions(cuentaRepository, clienteRepository);
        verifyNoInteractions(transaccionRepository);
    }

    @Test
    void crearCuentaConTipoInvalidoNoConsultaRepositorios() {
        assertEquals("Datos de cuenta invalidos", service.crearCuenta(NUMERO, DUI, "Plazo fijo"));

        verifyNoInteractions(clienteRepository, cuentaRepository, transaccionRepository);
    }

    @Test
    void listarCuentasTraduceElFalloDelRepositorioYConservaLaCausa() throws SQLException {
        SQLException fallo = new SQLException("Repositorio no disponible", "08006");
        when(cuentaRepository.listar()).thenThrow(fallo);

        DatabaseConnectionException error = assertThrows(DatabaseConnectionException.class,
                () -> service.listarCuentas());

        assertEquals("No se pudieron consultar las cuentas.", error.getMessage());
        assertSame(fallo, error.getCause());
        verify(cuentaRepository).listar();
        verifyNoMoreInteractions(cuentaRepository);
        verifyNoInteractions(clienteRepository, transaccionRepository);
    }

    @Test
    void desactivarCuentaDelegaEnElRepositorioSinCrearMovimientos() throws SQLException {
        when(cuentaRepository.desactivar(NUMERO)).thenReturn(true);

        assertEquals("Cuenta desactivada", service.desactivarCuenta(NUMERO));

        verify(cuentaRepository).desactivar(NUMERO);
        verifyNoMoreInteractions(cuentaRepository);
        verifyNoInteractions(clienteRepository, transaccionRepository);
    }

    @Test
    void obtenerHistorialConvierteLosRegistrosDelRepositorio() throws SQLException {
        LocalDateTime fecha = LocalDateTime.of(2026, 1, 15, 10, 30);
        TransaccionRegistro registro = new TransaccionRegistro("mov-1", NUMERO, "DEPOSITO",
                new BigDecimal("10.00"), new BigDecimal("35.00"), "Deposito", fecha, null, null);
        when(transaccionRepository.listarPorCuenta(NUMERO)).thenReturn(List.of(registro));

        List<Transaccion> historial = service.obtenerHistorial(NUMERO);

        assertEquals(1, historial.size());
        Transaccion movimiento = historial.get(0);
        assertAll(
                () -> assertEquals("mov-1", movimiento.getIdTransaccion()),
                () -> assertEquals(NUMERO, movimiento.getNumeroCuenta()),
                () -> assertEquals("DEPOSITO", movimiento.getTipo()),
                () -> assertEquals(new BigDecimal("10.00"), movimiento.getMonto()),
                () -> assertEquals(new BigDecimal("35.00"), movimiento.getSaldoPosterior()),
                () -> assertEquals(fecha, movimiento.getFecha()));
        verify(transaccionRepository).listarPorCuenta(NUMERO);
        verifyNoMoreInteractions(transaccionRepository);
        verifyNoInteractions(clienteRepository, cuentaRepository);
    }
}
