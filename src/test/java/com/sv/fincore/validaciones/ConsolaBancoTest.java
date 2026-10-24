package com.sv.fincore.validaciones;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolaBancoTest {

    private Scanner entrada(String... lineas) {
        return new Scanner(String.join("\n", lineas) + "\n");
    }

    @Test
    void textoObligatorio_reintentaHastaTenerValor() {
        assertEquals("hola", ConsolaBanco.textoObligatorio(entrada("", "   ", " hola "), "Campo: "));
    }

    @Test
    void dui_rechazaFormatosInvalidos() {
        assertEquals("12345678-9", ConsolaBanco.dui(entrada("123", "12345678", "1234567-89", "12345678-9"), "DUI: "));
    }

    @Test
    void montoPositivo_rechazaTextoCeroYNegativos() {
        BigDecimal monto = ConsolaBanco.montoPositivo(entrada("abc", "-5", "0", "10.5"));

        assertEquals(0, new BigDecimal("10.5").compareTo(monto));
    }
}
