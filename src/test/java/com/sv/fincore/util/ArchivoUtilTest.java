package com.sv.fincore.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchivoUtilTest {

    @Test
    void cargar_archivoInexistente_devuelveValorPorDefecto(@TempDir Path dir) throws Exception {
        Map<String, String> porDefecto = new HashMap<>();

        Map<String, String> r = ArchivoUtil.cargar(dir.resolve("no-existe.dat").toString(), porDefecto);

        assertSame(porDefecto, r);
    }

    @Test
    void guardarYCargar_roundTrip(@TempDir Path dir) throws Exception {
        String ruta = dir.resolve("datos.dat").toString();
        Map<String, String> original = new HashMap<>();
        original.put("a", "1");

        ArchivoUtil.guardar(ruta, original);
        Map<String, String> leido = ArchivoUtil.cargar(ruta, new HashMap<>());

        assertEquals(original, leido);
        assertNotSame(original, leido);
    }
}
