package com.sv.fincore.service;

import com.sv.fincore.dao.ClienteDAO;
import com.sv.fincore.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final String DUI = "12345678-9";

    @Mock ClienteDAO clienteDAO;
    ClienteService service;

    @BeforeEach
    void setUp() {
        when(clienteDAO.listar()).thenReturn(new ArrayList<>());
        service = new ClienteService(clienteDAO);
    }

    private Cliente cliente(String dui) {
        return new Cliente("id-" + dui, dui, "Ana", "Perez", "ana@x.com",
                "12345678", "Calle 1", LocalDate.of(1990, 1, 1));
    }

    @Test
    void registrarCliente_nuevo_guardaYRetornaExito() throws Exception {
        Cliente c = cliente(DUI);

        assertEquals("Cliente registrado exitosamente", service.registrarCliente(c));
        verify(clienteDAO).guardar(c);
        assertSame(c, service.buscarPorDui(DUI));
    }

    @Test
    void registrarCliente_duiDuplicado_noVuelveAGuardar() throws Exception {
        service.registrarCliente(cliente(DUI));

        assertEquals("DUI ya registrado", service.registrarCliente(cliente(DUI)));
        verify(clienteDAO, times(1)).guardar(any());
    }

    @Test
    void registrarCliente_falloDao_revierteLista() throws Exception {
        Cliente c = cliente(DUI);
        doThrow(new IOException()).when(clienteDAO).guardar(c);

        assertEquals("No se pudo guardar el cliente", service.registrarCliente(c));
        assertNull(service.buscarPorDui(DUI));
        assertTrue(service.listarClientes().isEmpty());
    }

    @Test
    void buscarPorNumeroCuenta_encuentraAlTitular() {
        Cliente c = cliente(DUI);
        c.agregarCuenta("001");
        service.registrarCliente(c);

        assertSame(c, service.buscarPorNumeroCuenta("001"));
        assertNull(service.buscarPorNumeroCuenta("999"));
    }

    @Test
    void modificarCliente_exitoso_actualizaYGuarda() throws Exception {
        Cliente c = cliente(DUI);
        service.registrarCliente(c);

        String r = service.modificarCliente(DUI, "Luis", "Gomez", "luis@x.com", "87654321", "Nueva");

        assertEquals("Cliente actualizado", r);
        assertEquals("Luis", c.getNombre());
        assertEquals("Gomez", c.getApellidos());
        assertEquals("luis@x.com", c.getCorreo());
        verify(clienteDAO, times(2)).guardar(c); // registro + modificacion
    }

    @Test
    void modificarCliente_noExiste() throws Exception {
        assertEquals("Cliente no encontrado",
                service.modificarCliente("00000000-0", "a", "b", "c@c.com", "12345678", "d"));
        verify(clienteDAO, never()).guardar(any());
    }

    @Test
    void modificarCliente_falloDao_restauraValoresAnteriores() throws Exception {
        Cliente c = cliente(DUI);
        service.registrarCliente(c);
        doThrow(new IOException()).when(clienteDAO).guardar(c);

        String r = service.modificarCliente(DUI, "Luis", "Gomez", "luis@x.com", "87654321", "Nueva");

        assertEquals("No se pudo guardar la actualización del cliente", r);
        assertEquals("Ana", c.getNombre());
        assertEquals("Perez", c.getApellidos());
        assertEquals("ana@x.com", c.getCorreo());
        assertEquals("12345678", c.getTelefono());
        assertEquals("Calle 1", c.getDireccion());
    }

    /** Documenta el comportamiento actual: los setters de Cliente ignoran datos invalidos
     *  (solo imprimen) y el servicio igual responde "Cliente actualizado". */
    @Test
    void modificarCliente_correoInvalido_seIgnoraSilenciosamente() {
        Cliente c = cliente(DUI);
        service.registrarCliente(c);

        String r = service.modificarCliente(DUI, "Ana", "Perez", "sin-arroba", "12345678", "Calle 1");

        assertEquals("Cliente actualizado", r);
        assertEquals("ana@x.com", c.getCorreo());
    }

    @Test
    void eliminarCliente_exitoso() throws Exception {
        Cliente c = cliente(DUI);
        service.registrarCliente(c);

        assertEquals("Cliente eliminado", service.eliminarCliente(DUI));
        verify(clienteDAO).eliminar(c.getId());
        assertNull(service.buscarPorDui(DUI));
    }

    @Test
    void eliminarCliente_falloDao_noLoQuitaDeLaLista() throws Exception {
        Cliente c = cliente(DUI);
        service.registrarCliente(c);
        doThrow(new IOException()).when(clienteDAO).eliminar(c.getId());

        assertEquals("No se pudo eliminar el cliente", service.eliminarCliente(DUI));
        assertNotNull(service.buscarPorDui(DUI));
    }

    @Test
    void eliminarCliente_noExiste() throws Exception {
        assertEquals("Cliente no encontrado", service.eliminarCliente("00000000-0"));
        verify(clienteDAO, never()).eliminar(any());
    }
}
