package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteServiceTest {

    private ClienteService clienteService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService();
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
    }

    @Test
    void registrarClienteExitosamente() {
        String resultado = clienteService.registrarCliente(cliente);

        assertEquals("Cliente registrado exitosamente", resultado);
        assertEquals(1, clienteService.listarClientes().size());
    }

    @Test
    void registrarClienteConDuiDuplicado() {
        clienteService.registrarCliente(cliente);

        Cliente duplicado = new Cliente(
            "2",
            "12345678-9",
            "Carlos",
            "Perez",
            "carlos@gmail.com",
            "70001122",
            "Santa Ana",
            LocalDate.of(1999, 3, 15)
        );

        String resultado = clienteService.registrarCliente(duplicado);

        assertEquals("DUI ya registrado", resultado);
        assertEquals(1, clienteService.listarClientes().size());
    }

    @Test
    void buscarClientePorDui() {
        clienteService.registrarCliente(cliente);

        Cliente encontrado = clienteService.buscarPorDui("12345678-9");

        assertNotNull(encontrado);
        assertEquals("Ana", encontrado.getNombre());
    }

    @Test
    void eliminarClienteExistente() {
        clienteService.registrarCliente(cliente);

        String resultado = clienteService.eliminarCliente("12345678-9");

        assertEquals("Cliente eliminado", resultado);
        assertTrue(clienteService.listarClientes().isEmpty());
    }
}