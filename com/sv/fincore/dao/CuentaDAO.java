package com.sv.fincore.dao;

import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.model.Cuenta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CuentaDAO {

    public Cuenta buscarPorNumero(Connection conn, String numeroCuenta) throws DatabaseConnectionException {
        String sql = "SELECT numero_cuenta, dui_cliente, saldo FROM cuentas WHERE numero_cuenta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroCuenta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cuenta c = new Cuenta();
                    c.setNumeroCuenta(rs.getString("numero_cuenta"));
                    c.setDuiCliente(rs.getString("dui_cliente"));
                    c.setSaldo(rs.getBigDecimal("saldo"));
                    return c;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error al consultar la cuenta: " + numeroCuenta, e);
        }
    }

    public void actualizarSaldo(Connection conn, String numeroCuenta, BigDecimal nuevoSaldo) throws DatabaseConnectionException {
        String sql = "UPDATE cuentas SET saldo = ? WHERE numero_cuenta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, nuevoSaldo);
            stmt.setString(2, numeroCuenta);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error al actualizar saldo de la cuenta: " + numeroCuenta, e);
        }
    }
}