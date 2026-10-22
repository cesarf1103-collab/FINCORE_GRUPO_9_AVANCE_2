package com.sv.fincore.dao;

import java.sql.SQLException;

/** Physical deletion is opt-in and must not be used for financial ledger records. */
public interface DeletableRepository<ID> {
    boolean eliminar(ID id) throws SQLException;
}
