package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;

public class CuentaDAO {

    public void guardar(Cuenta c) throws SQLException {
        String sql = "INSERT INTO cuentas (numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, c.getNumeroCuenta());
            stmt.setString(2, c.getDuiCliente());
            stmt.setBigDecimal(3, c.getSaldo());
            stmt.setString(4, c.getTipoCuenta());
            stmt.setBoolean(5, c.isActiva());
            
            stmt.executeUpdate();
        }
    }

    public boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) throws SQLException {
        String sql = "UPDATE cuentas SET saldo = ? WHERE numero_cuenta = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nuevoSaldo);
            stmt.setString(2, numeroCuenta);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}