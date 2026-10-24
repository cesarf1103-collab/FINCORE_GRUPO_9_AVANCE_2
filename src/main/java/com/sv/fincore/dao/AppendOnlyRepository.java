package com.sv.fincore.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Registro de movimientos sin métodos de actualización ni eliminación. */
public interface AppendOnlyRepository<T> {
    void registrar(T registro) throws SQLException;
    void registrar(Connection connection, T registro) throws SQLException;
}
