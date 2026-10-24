
package com.sv.fincore.util;

import java.sql.Connection;
import java.sql.SQLException;

/** Punto de entrada JDBC compartido por DAO, servicios y tareas de fondo. */
public final class ConexionDB {
    private ConexionDB() {}

    public static Connection obtenerConexion() throws SQLException {
        return DataSourceConfig.obtenerDataSource().getConnection();
    }
}
