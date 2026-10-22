package com.sv.fincore.view;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.model.Cliente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuClientesTest {

    @Mock ClienteController controller;

    final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    final PrintStream original = System.out;

    @BeforeEach
    void redirigir() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurar() {
        System.setOut(original);
    }

    private MenuClientes menu(String... lineas) {
        return new MenuClientes(new Scanner(String.join("\n", lineas) + "\n"), controller);
    }

    private String texto() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void registrarCliente_construyeElClienteConLosDatosIngresados() {
        when(controller.registrarCliente(any())).thenReturn("Cliente registrado exitosamente");

        menu("1", "id-1", "12345678-9", "Ana", "Perez", "ana@x.com", "12345678", "Calle 1",
                "1990", "1", "1", "6").mostrar();

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(controller).registrarCliente(captor.capture());
        assertEquals("12345678-9", captor.getValue().getDui());
        assertEquals(LocalDate.of(1990, 1, 1), captor.getValue().getFechaNacimiento());
        assertTrue(texto().contains("Cliente registrado exitosamente"));
    }

    @Test
    void listarClientes_sinClientes() {
        when(controller.listarClientes()).thenReturn(List.of());

        menu("2", "6").mostrar();

        assertTrue(texto().contains("No hay clientes registrados"));
    }

    @Test
    void buscarCliente_noEncontrado() {
        when(controller.buscarPorDui("12345678-9")).thenReturn(null);

        menu("3", "12345678-9", "6").mostrar();

        assertTrue(texto().contains("Cliente no encontrado"));
    }

    @Test
    void eliminarCliente_delegaAlController() {
        when(controller.eliminarCliente("12345678-9")).thenReturn("Cliente eliminado");

        menu("5", "12345678-9", "6").mostrar();

        verify(controller).eliminarCliente("12345678-9");
    }

    @Test
    void fechaInvalida_serepiteHastaSerValida() {
        when(controller.registrarCliente(any())).thenReturn("ok");

        menu("1", "id-1", "12345678-9", "Ana", "Perez", "ana@x.com", "12345678", "Calle 1",
                "1990", "13", "1",           // mes 13 -> invalida
                "1990", "2", "3", "6").mostrar();

        assertTrue(texto().contains("Fecha invalida"));
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(controller).registrarCliente(captor.capture());
        assertEquals(LocalDate.of(1990, 2, 3), captor.getValue().getFechaNacimiento());
    }
}
