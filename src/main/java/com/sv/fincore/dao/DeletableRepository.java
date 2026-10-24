package com.sv.fincore.dao;

import java.sql.SQLException;

/** Eliminación física; no se aplica al historial bancario. */
public interface DeletableRepository<ID> {
    boolean eliminar(ID id) throws SQLException;
}
