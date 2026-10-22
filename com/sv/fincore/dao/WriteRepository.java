package com.sv.fincore.dao;

import java.sql.SQLException;

public interface WriteRepository<T> {
    /** Insert or persist the entity using the repository's established save semantics. */
    void guardar(T entidad) throws SQLException;

    /** Update an existing entity; returns false when no row matched. */
    boolean actualizar(T entidad) throws SQLException;
}
