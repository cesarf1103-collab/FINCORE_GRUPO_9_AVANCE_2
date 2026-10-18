package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import com.sv.fincore.util.ConexionDB;

import java.sql.*;

public class TransaccionDAO {

    public void guardar(Transaccion t) throws SQLException {
        String sql = "INSERT INTO transacciones (id_transaccion, numero_cuenta, tipo, monto, saldo_posterior, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, t.getIdTransaccion());
            stmt.setString(2, t.getNumeroCuenta());
            stmt.setString(3, t.getTipo());
            stmt.setBigDecimal(4, t.getMonto());
            stmt.setBigDecimal(5, t.getSaldoPosterior());
            stmt.setString(6, t.getDescripcion());
            
            stmt.executeUpdate();
        }
    }
}
