
package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import com.sv.fincore.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaccionDAO implements TransaccionRepository {

    public void guardar(Transaccion t) throws SQLException {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            guardar(conn, t);
        }
    }

    @Override
    public void registrar(Transaccion registro) throws SQLException {
        guardar(registro);
    }

    @Override
    public void registrar(Connection connection, Transaccion registro) throws SQLException {
        guardar(connection, registro);
    }

    public void guardar(Connection conn, Transaccion t) throws SQLException {
        String sql = """
            INSERT INTO transacciones
            (id_transaccion, numero_cuenta, tipo, monto,
             saldo_posterior, descripcion, fecha, cuenta_origen, cuenta_destino)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getIdTransaccion());
            stmt.setString(2, t.getNumeroCuenta());
            stmt.setString(3, t.getTipo());
            stmt.setBigDecimal(4, t.getMonto());
            stmt.setBigDecimal(5, t.getSaldoPosterior());
            stmt.setString(6, t.getDescripcion());
            stmt.setTimestamp(7, Timestamp.valueOf(t.getFecha()));
            stmt.setString(8, t.getCuentaOrigen());
            stmt.setString(9, t.getCuentaDestino());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<TransaccionRegistro> listar()
            throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion, fecha, cuenta_origen, cuenta_destino
            FROM transacciones
            ORDER BY fecha, id_transaccion
            """;

        List<TransaccionRegistro> transacciones =
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

    @Override
    public List<TransaccionRegistro> listarPorCuenta(
            String numeroCuenta) throws SQLException {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return listarPorCuenta(conn, numeroCuenta);
        }
    }

    @Override
    public List<TransaccionRegistro> listarPorCuenta(
            Connection conn, String numeroCuenta) throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion, fecha, cuenta_origen, cuenta_destino
            FROM transacciones
            WHERE numero_cuenta = ?
            ORDER BY fecha, id_transaccion
            """;

        List<TransaccionRegistro> transacciones =
                new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCuenta);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transacciones.add(convertirRegistro(rs));
                }
            }
        }

        return transacciones;
    }

    @Override
    public TransaccionRegistro buscarPorId(
            String idTransaccion) throws SQLException {

        String sql = """
            SELECT id_transaccion, numero_cuenta, tipo,
                   monto, saldo_posterior, descripcion, fecha, cuenta_origen, cuenta_destino
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

    private TransaccionRegistro convertirRegistro(
            ResultSet rs) throws SQLException {

        return new TransaccionRegistro(
            rs.getString("id_transaccion"),
            rs.getString("numero_cuenta"),
            rs.getString("tipo"),
            rs.getBigDecimal("monto"),
            rs.getBigDecimal("saldo_posterior"),
            rs.getString("descripcion"),
            rs.getTimestamp("fecha").toLocalDateTime(),
            rs.getString("cuenta_origen"),
            rs.getString("cuenta_destino")
        );
    }
}
