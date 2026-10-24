package com.sv.fincore.ui;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Deposito;
import com.sv.fincore.model.Transaccion;
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
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ConsultaHistorialTest {

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

    private String ejecutar(String numero) {
        new ConsultaHistorial(cuentaService).mostrarConsulta(new Scanner(numero + "\n"));
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void cuentaNoExiste() {
        when(cuentaService.buscarCuenta("999")).thenReturn(null);

        assertTrue(ejecutar("999").contains("Cuenta no encontrada."));
        verify(cuentaService, never()).obtenerHistorial("999");
    }

    @Test
    void sinMovimientos() {
        when(cuentaService.buscarCuenta("001")).thenReturn(new Cuenta("001", "d", "Ahorro"));
        when(cuentaService.obtenerHistorial("001")).thenReturn(List.of());

        assertTrue(ejecutar("001").contains("No hay movimientos registrados."));
    }

    @Test
    void conMovimientos_muestraTipoYMonto() {
        Transaccion dep = new Deposito("t1", "001", new BigDecimal("50"), "x");
        when(cuentaService.buscarCuenta("001")).thenReturn(new Cuenta("001", "d", "Ahorro"));
        when(cuentaService.obtenerHistorial("001")).thenReturn(List.of(dep));

        assertTrue(ejecutar("001").contains("DEPOSITO - $50"));
    }
}
