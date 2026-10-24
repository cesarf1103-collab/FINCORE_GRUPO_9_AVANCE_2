package com.sv.fincore.dao;

import java.sql.SQLException;

public interface WriteRepository<T> {
    /** Guarda una entidad según las reglas del repositorio. */
    void guardar(T entidad) throws SQLException;

    /** Actualiza una entidad; devuelve false si no existe. */
    boolean actualizar(T entidad) throws SQLException;
}
