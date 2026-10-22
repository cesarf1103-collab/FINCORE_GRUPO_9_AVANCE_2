package com.sv.fincore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransaccionesTest {

    Cuenta origen;
    Cuenta destino;

    @BeforeEach
    void setUp() {
        origen = new Cuenta("001", "12345678-9", "Ahorro");
        destino = new Cuenta("002", "98765432-1", "Corriente");
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    // ---------- Deposito ----------

    @Test
    void deposito_sumaSaldoYRegistraSaldoPosterior() {
        Deposito d = new Deposito("t1", "001", bd("100"), "x");

        d.ejecutar(origen, null);

        assertEquals(0, bd("100").compareTo(origen.getSaldo()));
        assertEquals(0, bd("100").compareTo(d.getSaldoPosterior()));
        assertEquals("DEPOSITO", d.getTipo());
    }

    @Test
    void deposito_montoInvalido_lanzaExcepcionEnConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Deposito("t1", "001", BigDecimal.ZERO, "x"));
        assertThrows(IllegalArgumentException.class, () -> new Deposito("t1", "001", bd("-5"), "x"));
        assertThrows(IllegalArgumentException.class, () -> new Deposito("t1", "001", null, "x"));
    }

    @Test
    void deposito_cuentaNulaOInactiva() {
        Deposito d = new Deposito("t1", "001", BigDecimal.TEN, "x");
        assertThrows(IllegalArgumentException.class, () -> d.ejecutar(null, null));

        origen.setActiva(false);
        assertThrows(IllegalStateException.class, () -> d.ejecutar(origen, null));
    }

    // ---------- Retiro ----------

    @Test
    void retiro_restaSaldo() {
        origen.setSaldo(bd("100"));
        Retiro r = new Retiro("t2", "001", bd("30"), "x");

        r.ejecutar(origen, null);

        assertEquals(0, bd("70").compareTo(origen.getSaldo()));
        assertEquals(0, bd("70").compareTo(r.getSaldoPosterior()));
    }

    @Test
    void retiro_saldoInsuficiente() {
        Retiro r = new Retiro("t2", "001", BigDecimal.TEN, "x");

        assertThrows(IllegalStateException.class, () -> r.ejecutar(origen, null));
        assertEquals(0, BigDecimal.ZERO.compareTo(origen.getSaldo()));
    }

    // ---------- Transferencia ----------

    @Test
    void transferencia_muevaFondosEntreCuentas() {
        origen.setSaldo(bd("100"));
        Transferencia t = new Transferencia("t3", "001", "002", bd("40"), "x");

        t.ejecutar(origen, destino);

        assertEquals(0, bd("60").compareTo(origen.getSaldo()));
        assertEquals(0, bd("40").compareTo(destino.getSaldo()));
        assertEquals("001", t.getCuentaOrigen());
        assertEquals("002", t.getCuentaDestino());
    }

    @Test
    void transferencia_mismaCuenta() {
        Transferencia t = new Transferencia("t3", "001", "001", BigDecimal.TEN, "x");

        assertThrows(IllegalArgumentException.class, () -> t.ejecutar(origen, origen));
    }

    @Test
    void transferencia_saldoInsuficienteOCuentaInactiva() {
        Transferencia t = new Transferencia("t3", "001", "002", BigDecimal.TEN, "x");
        assertThrows(IllegalStateException.class, () -> t.ejecutar(origen, destino));

        origen.setSaldo(bd("100"));
        destino.setActiva(false);
        assertThrows(IllegalStateException.class, () -> t.ejecutar(origen, destino));
    }

    @Test
    void transferencia_registroDestino_invierteLaPerspectiva() {
        Transferencia salida = new Transferencia("t3", "001", "002", bd("40"), "enviada");

        Transferencia entrada = salida.registroDestino("t4", bd("40"));

        assertEquals("002", entrada.getNumeroCuenta());
        assertEquals("Transferencia recibida", entrada.getDescripcion());
        assertEquals(0, bd("40").compareTo(entrada.getSaldoPosterior()));
        assertEquals("TRANSFERENCIA", entrada.getTipo());
    }
}
