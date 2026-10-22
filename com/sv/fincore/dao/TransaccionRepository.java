package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Read and append API for immutable financial history; no physical delete or update. */
public interface TransaccionRepository extends ReadRepository<TransaccionRegistro, String>, AppendOnlyRepository<Transaccion> {
    List<TransaccionRegistro> listarPorCuenta(String numeroCuenta) throws SQLException;
    List<TransaccionRegistro> listarPorCuenta(Connection connection, String numeroCuenta) throws SQLException;
}
