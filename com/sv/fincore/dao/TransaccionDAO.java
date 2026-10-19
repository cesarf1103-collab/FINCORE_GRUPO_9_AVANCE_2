
package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import com.sv.fincore.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaccionDAO {

    // Representacion de los datos almacenados en SQL.
    // No modifica las clases del modelo bancario.
    public record RegistroTransaccion(
            String idTransaccion,
            String numeroCuenta,
            String tipo,
            BigDecimal monto,
            BigDecimal saldoPosterior,
            String descripcion) {
    }

    public void guardar(Transaccion t) throws SQLException {
        String sql = """
            INSERT INTO transacciones
            (id_transaccion, numero_cuenta, tipo, monto,
             saldo_posterior, descripcion)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

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

    public List<RegistroTransaccion> listar()
            throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion
            FROM transacciones
            """;

        List<RegistroTransaccion> transacciones =
                new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transacciones.add(convertirRegistro(rs));
            }
        }

        return transacciones;
    }

    public List<RegistroTransaccion> listarPorCuenta(
            String numeroCuenta) throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion
            FROM transacciones
            WHERE numero_cuenta = ?
            """;

        List<RegistroTransaccion> transacciones =
                new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCuenta);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transacciones.add(convertirRegistro(rs));
                }
            }
        }

        return transacciones;
    }

    public RegistroTransaccion buscarPorId(
            String idTransaccion) throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion
            FROM transacciones
            WHERE id_transaccion = ?
            """;

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idTransaccion);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirRegistro(rs) : null;
            }
        }
    }

    public Map<String, Long> resumenPorTipo()
            throws SQLException {

        String sql = """
            SELECT tipo, COUNT(*) AS total
            FROM transacciones
            GROUP BY tipo
            """;

        Map<String, Long> resumen = new LinkedHashMap<>();

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resumen.put(
                    rs.getString("tipo"),
                    rs.getLong("total")
                );
            }
        }

        return resumen;
    }

    private RegistroTransaccion convertirRegistro(
            ResultSet rs) throws SQLException {

        return new RegistroTransaccion(
            rs.getString("id_transaccion"),
            rs.getString("numero_cuenta"),
            rs.getString("tipo"),
            rs.getBigDecimal("monto"),
            rs.getBigDecimal("saldo_posterior"),
            rs.getString("descripcion")
        );
    }
}
