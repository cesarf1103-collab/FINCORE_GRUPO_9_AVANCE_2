package com.sv.fincore.util;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Aplica scripts versionados; nunca borra ni reemplaza datos bancarios. */
final class SchemaInitializer {
    private SchemaInitializer() {}

    static void ejecutar(DataSource dataSource, String recurso) throws SQLException {
        String sql;
        try (InputStream entrada = SchemaInitializer.class.getResourceAsStream("/" + recurso)) {
            if (entrada == null) {
                throw new SQLException("Falta el recurso SQL: " + recurso);
            }
            sql = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer " + recurso, e);
        }

        String sinComentarios = sql.replaceAll("(?m)^\\s*--[^\\r\\n]*", "");
        try (Connection conexion = dataSource.getConnection();
             Statement sentencia = conexion.createStatement()) {
            for (String fragmento : sinComentarios.split(";")) {
                if (!fragmento.isBlank()) {
                    sentencia.execute(fragmento.trim());
                }
            }
        }
    }
}
