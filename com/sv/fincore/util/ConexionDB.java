
package com.sv.fincore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static String obtenerVariable(String nombre)
            throws SQLException {

        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            throw new SQLException(
                "Falta configurar la variable de entorno: " + nombre
            );
        }

        return valor;
    }

    public static Connection obtenerConexion()
            throws SQLException {

        String url = obtenerVariable("FINCORE_DB_URL");
        String usuario = obtenerVariable("FINCORE_DB_USER");
        String password = obtenerVariable("FINCORE_DB_PASSWORD");

        return DriverManager.getConnection(
            url,
            usuario,
            password
        );
    }
}
