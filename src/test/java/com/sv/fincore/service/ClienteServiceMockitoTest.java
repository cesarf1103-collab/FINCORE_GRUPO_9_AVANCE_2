package com.sv.fincore.service;

import com.sv.fincore.dao.ClienteRepository;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.model.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** Pruebas unitarias con un repositorio simulado, sin conexiones JDBC. */
@ExtendWith(MockitoExtension.class)
class ClienteServiceMockitoTest {
    private static final String DUI = "12345678-9";

    @Mock ClienteRepository clienteRepository;
    @InjectMocks ClienteService service;

    @Test
    void registrarClienteValidoConsultaUnicidadYGuarda() throws SQLException {
        Cliente cliente = cliente(DUI);

        assertEquals("Cliente registrado exitosamente", service.registrarCliente(cliente));

        verify(clienteRepository).buscarPorDui(DUI);
        verify(clienteRepository).guardar(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void registrarDuiDuplicadoNoGuarda() throws SQLException {
        Cliente cliente = cliente(DUI);
        when(clienteRepository.buscarPorDui(DUI)).thenReturn(cliente);

        assertEquals("DUI ya registrado", service.registrarCliente(cliente));

        verify(clienteRepository).buscarPorDui(DUI);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void registrarDuiInvalidoNoConsultaElRepositorio() {
        assertEquals("Datos de cliente invalidos",
                service.registrarCliente(cliente("dui-invalido")));

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void registrarTraduceFalloDelRepositorioYConservaLaCausa() throws SQLException {
        Cliente cliente = cliente(DUI);
        SQLException fallo = new SQLException("Repositorio no disponible", "08006");
        doThrow(fallo).when(clienteRepository).guardar(cliente);

        DatabaseConnectionException error = assertThrows(DatabaseConnectionException.class,
                () -> service.registrarCliente(cliente));

        assertEquals("No se pudo guardar el cliente.", error.getMessage());
        assertSame(fallo, error.getCause());
        verify(clienteRepository).buscarPorDui(DUI);
        verify(clienteRepository).guardar(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void registrarConflictoDeUnicidadConcurrenteDevuelveDuplicado() throws SQLException {
        Cliente cliente = cliente(DUI);
        doThrow(new SQLException("DUI duplicado", "23505"))
                .when(clienteRepository).guardar(cliente);

        assertEquals("DUI ya registrado", service.registrarCliente(cliente));

        verify(clienteRepository).buscarPorDui(DUI);
        verify(clienteRepository).guardar(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void modificarClienteValidoEntregaLosCambiosAlRepositorio() throws SQLException {
        Cliente cliente = cliente(DUI);
        when(clienteRepository.buscarPorDui(DUI)).thenReturn(cliente);
        when(clienteRepository.actualizar(cliente)).thenReturn(true);

        assertEquals("Cliente actualizado", service.modificarCliente(DUI,
                "Luis", "Gomez", "luis@example.invalid", "87654321", "Nueva direccion"));

        assertAll(
                () -> assertEquals("Luis", cliente.getNombre()),
                () -> assertEquals("Gomez", cliente.getApellidos()),
                () -> assertEquals("luis@example.invalid", cliente.getCorreo()),
                () -> assertEquals("87654321", cliente.getTelefono()),
                () -> assertEquals("Nueva direccion", cliente.getDireccion()));
        verify(clienteRepository).buscarPorDui(DUI);
        verify(clienteRepository).actualizar(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void modificarCorreoInvalidoNoConsultaNiActualiza() {
        assertEquals("Datos de cliente invalidos", service.modificarCliente(DUI,
                "Ana", "Perez", "sin-arroba", "12345678", "Calle 1"));

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void eliminarClienteConCuentasNoSolicitaElBorrado() throws SQLException {
        Cliente cliente = cliente(DUI);
        cliente.agregarCuenta("CTA-001");
        when(clienteRepository.buscarPorDui(DUI)).thenReturn(cliente);

        assertEquals("No se puede eliminar un cliente con cuentas asociadas",
                service.eliminarCliente(DUI));

        verify(clienteRepository).buscarPorDui(DUI);
        verifyNoMoreInteractions(clienteRepository);
    }

    private Cliente cliente(String dui) {
        return new Cliente("cliente-1", dui, "Ana", "Perez", "ana@example.invalid",
                "12345678", "Calle 1", LocalDate.of(1990, 1, 1));
    }
}
