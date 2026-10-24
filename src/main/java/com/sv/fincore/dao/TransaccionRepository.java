package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Consulta y registra historial bancario; no permite borrarlo ni editarlo. */
public interface TransaccionRepository extends ReadRepository<TransaccionRegistro, String>, AppendOnlyRepository<Transaccion> {
    List<TransaccionRegistro> listarPorCuenta(String numeroCuenta) throws SQLException;
    List<TransaccionRegistro> listarPorCuenta(Connection connection, String numeroCuenta) throws SQLException;
}
