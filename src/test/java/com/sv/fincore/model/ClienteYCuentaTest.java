package com.sv.fincore.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClienteYCuentaTest {

    private Cliente cliente() {
        return new Cliente("id-1", "12345678-9", "Ana", "Perez", "ana@x.com",
                "12345678", "Calle 1", LocalDate.of(1990, 1, 1));
    }

    // ---------- Cliente ----------

    @Test
    void cliente_setters_aceptanDatosValidos() {
        Cliente c = cliente();
        c.setNombre("Luis");
        c.setApellidos("Gomez");
        c.setCorreo("luis@x.com");
        c.setTelefono("87654321");
        c.setDireccion("Otra");
        c.setFechaNacimiento(LocalDate.of(2000, 2, 2));

        assertEquals("Luis", c.getNombre());
        assertEquals("Gomez", c.getApellidos());
        assertEquals("luis@x.com", c.getCorreo());
        assertEquals("87654321", c.getTelefono());
        assertEquals("Otra", c.getDireccion());
        assertEquals(LocalDate.of(2000, 2, 2), c.getFechaNacimiento());
    }

    @Test
    void cliente_setters_ignoranDatosInvalidos() {
        Cliente c = cliente();
        c.setNombre("  ");
        c.setApellidos(null);
        c.setCorreo("sin-arroba");
        c.setTelefono("123");
        c.setDireccion("");
        c.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertEquals("Ana", c.getNombre());
        assertEquals("Perez", c.getApellidos());
        assertEquals("ana@x.com", c.getCorreo());
        assertEquals("12345678", c.getTelefono());
        assertEquals("Calle 1", c.getDireccion());
        assertEquals(LocalDate.of(1990, 1, 1), c.getFechaNacimiento());
    }

    @Test
    void cliente_agregarCuentaYTipo() {
        Cliente c = cliente();
        c.agregarCuenta("001");

        assertEquals(1, c.getNumerosCuenta().size());
        assertEquals("Cliente", c.getTipoPersona());
    }

    // ---------- Cuenta ----------

    @Test
    void cuenta_valoresIniciales() {
        Cuenta cu = new Cuenta("001", "12345678-9", "Ahorro");

        assertEquals(0, BigDecimal.ZERO.compareTo(cu.getSaldo()));
        assertTrue(cu.isActiva());
        assertTrue(cu.getHistorial().isEmpty());
        assertNotNull(cu.getFechaApertura());
    }

    @Test
    void cuenta_setSaldo_rechazaNegativosYNulos() {
        Cuenta cu = new Cuenta("001", "12345678-9", "Ahorro");
        cu.setSaldo(new BigDecimal("50"));

        cu.setSaldo(new BigDecimal("-1"));
        cu.setSaldo(null);

        assertEquals(0, new BigDecimal("50").compareTo(cu.getSaldo()));
    }

    @Test
    void cuenta_setTipoCuenta_soloAhorroOCorriente() {
        Cuenta cu = new Cuenta("001", "12345678-9", "Ahorro");

        cu.setTipoCuenta("corriente");
        assertEquals("corriente", cu.getTipoCuenta());

        cu.setTipoCuenta("Plazo fijo");
        assertEquals("corriente", cu.getTipoCuenta());
    }

    /** Documenta un hueco: el constructor no valida el tipo, solo el setter. */
    @Test
    void cuenta_constructor_noValidaTipo() {
        Cuenta cu = new Cuenta("001", "12345678-9", "Cualquiera");

        assertEquals("Cualquiera", cu.getTipoCuenta());
    }
}
