package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.dao.ClienteDAO;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteServiceTest {

    private ClienteService clienteService;
    private Cliente cliente;
    private String dui;

    @BeforeEach
    void setUp() {
        dui = String.format("%08d-9", Math.floorMod(UUID.randomUUID().hashCode(), 100000000));
        clienteService = new ClienteService();
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
    }

    @AfterEach
    void limpiarDatos() throws Exception {
        new ClienteDAO().eliminar(cliente.getId());
    }
    @Test
    void registrarClienteExitosamente() {
        String resultado = clienteService.registrarCliente(cliente);

        assertEquals("Cliente registrado exitosamente", resultado);
        assertEquals(1L, clienteService.listarClientes().stream().filter(x -> x.getDui().equals(dui)).count());
    }

    @Test
    void registrarClienteConDuiDuplicado() {
        clienteService.registrarCliente(cliente);

        Cliente duplicado = new Cliente(
            UUID.randomUUID().toString(),
            dui,
            "Carlos",
            "Perez",
            "carlos@gmail.com",
            "70001122",
            "Santa Ana",
            LocalDate.of(1999, 3, 15)
        );

        String resultado = clienteService.registrarCliente(duplicado);

        assertEquals("DUI ya registrado", resultado);
        assertEquals(1L, clienteService.listarClientes().stream().filter(x -> x.getDui().equals(dui)).count());
    }

    @Test
    void buscarClientePorDui() {
        clienteService.registrarCliente(cliente);

        Cliente encontrado = clienteService.buscarPorDui(dui);

        assertNotNull(encontrado);
        assertEquals("Ana", encontrado.getNombre());
    }

    @Test
    void eliminarClienteExistente() {
        clienteService.registrarCliente(cliente);

        String resultado = clienteService.eliminarCliente(dui);

        assertEquals("Cliente eliminado", resultado);
        assertNull(clienteService.buscarPorDui(dui));
    }
}