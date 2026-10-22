package com.sv.fincore.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Append-only writes for audit/ledger records; deliberately has no update or delete operation. */
public interface AppendOnlyRepository<T> {
    void registrar(T registro) throws SQLException;
    void registrar(Connection connection, T registro) throws SQLException;
}
