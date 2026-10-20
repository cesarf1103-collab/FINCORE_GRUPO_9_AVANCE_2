package com.sv.fincore.view;

import com.sv.fincore.service.CuentaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuOperacionesTest {

    @Mock CuentaService cuentaService;

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

    private MenuOperaciones menu(String... lineas) {
        return new MenuOperaciones(new Scanner(String.join("\n", lineas) + "\n"), cuentaService);
    }

    @Test
    void depositar_llamaAlServicioYMuestraElResultado() {
        when(cuentaService.depositar("001", new BigDecimal("50"))).thenReturn("Deposito exitoso");

        menu("1", "001", "50", "5").mostrar();

        verify(cuentaService).depositar("001", new BigDecimal("50"));
        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("Deposito exitoso"));
    }

    @Test
    void transferir_pasaOrigenDestinoYMonto() {
        when(cuentaService.transferir("001", "002", new BigDecimal("10"))).thenReturn("Transferencia exitosa");

        menu("3", "001", "002", "10", "5").mostrar();

        verify(cuentaService).transferir("001", "002", new BigDecimal("10"));
    }

    @Test
    void opcionInvalida_noLlamaAlServicio() {
        menu("abc", "5").mostrar();

        assertTrue(salida.toString(StandardCharsets.UTF_8).contains("Opcion invalida"));
    }
}
