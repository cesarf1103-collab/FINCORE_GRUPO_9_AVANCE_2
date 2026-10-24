package com.sv.fincore.audit;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Persona;
import modulo.empleado.Administrador;
import modulo.empleado.AtencionCliente;
import modulo.empleado.Cajero;
import modulo.empleado.Empleado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaJerarquiaTest {
    @Test
    void clienteYLosTresRolesCompartenPersona() {
        Persona cliente = new Cliente("1", "DUI", "Ana", "Pérez", "ana@example.test",
                "12345678", "San Salvador", LocalDate.of(1990, 1, 1));
        Administrador administrador = new Administrador("2", "Admin", "admin@example.test", 1000, "TOTAL");
        Cajero cajero = new Cajero("3", "Caja", "caja@example.test", 800, "C1");
        AtencionCliente atencion = new AtencionCliente("4", "Atención", "atencion@example.test", 700, "M1");

        assertEquals(Persona.class, Cliente.class.getSuperclass());
        assertEquals(Persona.class, Empleado.class.getSuperclass());
        assertEquals(Empleado.class, Administrador.class.getSuperclass());
        assertEquals(Empleado.class, Cajero.class.getSuperclass());
        assertEquals(Empleado.class, AtencionCliente.class.getSuperclass());
        assertEquals("Cliente", cliente.getTipoPersona());
        assertEquals("ana@example.test", cliente.getCorreo());
        for (Persona persona : List.of(administrador, cajero, atencion)) {
            assertEquals("EMPLEADO", persona.getTipoPersona());
            assertNotNull(persona.getCorreo());
        }
        assertEquals("TOTAL", administrador.getNivelAcceso());
        assertEquals("C1", cajero.getNumCaja());
        assertEquals("M1", atencion.getModuloAtencion());
    }
}
