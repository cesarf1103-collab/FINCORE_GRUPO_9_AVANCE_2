package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Requiere el constructor ClienteDAO(String ruta) descrito en REFACTOR-main.md */
class ClienteDAOTest {

    @TempDir Path dir;
    String ruta;
    ClienteDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        ruta = dir.resolve("clientes.dat").toString();
        dao = new ClienteDAO(ruta);
    }

    private Cliente cliente(String id, String dui) {
        return new Cliente(id, dui, "Ana", "Perez", "ana@x.com", "12345678", "Calle 1",
                LocalDate.of(1990, 1, 1));
    }

    @Test
    void guardar_persisteYSePuedeRecargar() throws Exception {
        dao.guardar(cliente("id-1", "12345678-9"));

        ClienteDAO otro = new ClienteDAO(ruta);

        assertEquals(1, otro.listar().size());
        assertEquals("12345678-9", otro.buscarPorId("id-1").getDui());
    }

    @Test
    void buscarPorDui_yExiste() throws Exception {
        dao.guardar(cliente("id-1", "12345678-9"));

        assertNotNull(dao.buscarPorDui("12345678-9"));
        assertNull(dao.buscarPorDui("00000000-0"));
        assertTrue(dao.existe("id-1"));
        assertFalse(dao.existe("zzz"));
    }

    @Test
    void eliminar_devuelveTrueSoloSiExistia() throws Exception {
        dao.guardar(cliente("id-1", "12345678-9"));

        assertTrue(dao.eliminar("id-1"));
        assertFalse(dao.eliminar("id-1"));
        assertTrue(new ClienteDAO(ruta).listar().isEmpty());
    }
}
