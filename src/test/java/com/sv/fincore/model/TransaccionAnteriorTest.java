package com.sv.fincore.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransaccionAnteriorTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2024, 5, 1, 10, 30);

    private TransaccionAnterior anterior(String tipo) throws Exception {
        TransaccionAnterior a = new TransaccionAnterior();
        set(a, "idTransaccion", "id-1");
        set(a, "numeroCuenta", "001");
        set(a, "tipo", tipo);
        set(a, "monto", new BigDecimal("25"));
        set(a, "saldoPosterior", new BigDecimal("75"));
        set(a, "fecha", FECHA);
        set(a, "descripcion", "legado");
        set(a, "cuentaOrigen", "001");
        set(a, "cuentaDestino", "002");
        return a;
    }

    private static void set(Object o, String campo, Object valor) throws Exception {
        Field f = o.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(o, valor);
    }

    @Test
    void convertir_deposito() throws Exception {
        Transaccion t = anterior("DEPOSITO").convertir();

        assertInstanceOf(Deposito.class, t);
        assertEquals(FECHA, t.getFecha());
        assertEquals(0, new BigDecimal("75").compareTo(t.getSaldoPosterior()));
    }

    @Test
    void convertir_retiro() throws Exception {
        assertInstanceOf(Retiro.class, anterior("RETIRO").convertir());
    }

    @Test
    void convertir_transferencia_conservaOrigenYDestino() throws Exception {
        Transaccion t = anterior("TRANSFERENCIA").convertir();

        assertInstanceOf(Transferencia.class, t);
        assertEquals("001", t.getCuentaOrigen());
        assertEquals("002", t.getCuentaDestino());
        assertEquals("001", t.getNumeroCuenta());
    }

    @Test
    void convertir_tipoDesconocido_lanzaExcepcion() throws Exception {
        TransaccionAnterior a = anterior("OTRO");

        assertThrows(IllegalStateException.class, a::convertir);
    }
}
