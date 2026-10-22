package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CuentaRepository extends CrudRepository<Cuenta, String> {
    Cuenta buscarPorNumero(String numeroCuenta) throws SQLException;
    Cuenta buscarPorNumero(Connection connection, String numeroCuenta) throws SQLException;
    Cuenta bloquearPorNumero(Connection connection, String numeroCuenta) throws SQLException;
    List<Cuenta> listar(Connection connection) throws SQLException;
    List<Cuenta> listarPorDui(String duiCliente) throws SQLException;
    boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) throws SQLException;
    boolean actualizarSaldo(Connection connection, String numeroCuenta, BigDecimal nuevoSaldo) throws SQLException;
}
