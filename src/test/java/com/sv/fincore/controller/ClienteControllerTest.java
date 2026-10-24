package com.sv.fincore.controller;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock ClienteService clienteService;
    @InjectMocks ClienteController controller;

    @Test
    void registrarCliente_delega() {
        Cliente c = mock(Cliente.class);
        when(clienteService.registrarCliente(c)).thenReturn("Cliente registrado exitosamente");

        assertEquals("Cliente registrado exitosamente", controller.registrarCliente(c));
    }

    @Test
    void listarClientes_devuelveLaMismaLista() {
        List<Cliente> lista = List.of(mock(Cliente.class));
        when(clienteService.listarClientes()).thenReturn(lista);

        assertSame(lista, controller.listarClientes());
    }

    @Test
    void buscarPorDuiYPorCuenta_delegan() {
        Cliente c = mock(Cliente.class);
        when(clienteService.buscarPorDui("12345678-9")).thenReturn(c);
        when(clienteService.buscarPorNumeroCuenta("001")).thenReturn(c);

        assertSame(c, controller.buscarPorDui("12345678-9"));
        assertSame(c, controller.buscarPorNumeroCuenta("001"));
    }

    @Test
    void modificarCliente_pasaTodosLosParametros() {
        when(clienteService.modificarCliente("d", "n", "a", "c", "t", "dir")).thenReturn("Cliente actualizado");

        assertEquals("Cliente actualizado", controller.modificarCliente("d", "n", "a", "c", "t", "dir"));
        verify(clienteService).modificarCliente("d", "n", "a", "c", "t", "dir");
    }

    @Test
    void eliminarCliente_delega() {
        when(clienteService.eliminarCliente("d")).thenReturn("Cliente eliminado");

        assertEquals("Cliente eliminado", controller.eliminarCliente("d"));
    }
}
