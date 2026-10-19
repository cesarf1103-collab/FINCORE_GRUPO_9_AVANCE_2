
package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    public void guardar(Cuenta c) throws SQLException {
        String verificar =
            "SELECT 1 FROM cuentas WHERE numero_cuenta = ?";

        String insertar = """
            INSERT INTO cuentas
            (numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa)
            VALUES (?, ?, ?, ?, ?)
            """;

        String actualizar = """
            UPDATE cuentas
            SET dui_cliente = ?, saldo = ?,
                tipo_cuenta = ?, activa = ?
            WHERE numero_cuenta = ?
            """;

        try (Connection conn = ConexionDB.obtenerConexion()) {
            boolean existe;

            try (PreparedStatement stmt =
                    conn.prepareStatement(verificar)) {

                stmt.setString(1, c.getNumeroCuenta());

                try (ResultSet rs = stmt.executeQuery()) {
                    existe = rs.next();
                }
            }

            String sql = existe ? actualizar : insertar;

            try (PreparedStatement stmt =
                    conn.prepareStatement(sql)) {

                if (existe) {
                    stmt.setString(1, c.getDuiCliente());
                    stmt.setBigDecimal(2, c.getSaldo());
                    stmt.setString(3, c.getTipoCuenta());
                    stmt.setBoolean(4, c.isActiva());
                    stmt.setString(5, c.getNumeroCuenta());
                } else {
                    stmt.setString(1, c.getNumeroCuenta());
                    stmt.setString(2, c.getDuiCliente());
                    stmt.setBigDecimal(3, c.getSaldo());
                    stmt.setString(4, c.getTipoCuenta());
                    stmt.setBoolean(5, c.isActiva());
                }

                stmt.executeUpdate();
            }
        }
    }

    public Cuenta buscarPorNumero(String numeroCuenta)
            throws SQLException {

        String sql =
            "SELECT * FROM cuentas WHERE numero_cuenta = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCuenta);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return convertirCuenta(rs);
                }
            }
        }

        return null;
    }

    public List<Cuenta> listar() throws SQLException {
        List<Cuenta> cuentas = new ArrayList<>();

        String sql = "SELECT * FROM cuentas";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cuentas.add(convertirCuenta(rs));
            }
        }

        return cuentas;
    }

    public List<Cuenta> listarPorDui(String duiCliente)
            throws SQLException {

        List<Cuenta> cuentas = new ArrayList<>();

        String sql =
            "SELECT * FROM cuentas WHERE dui_cliente = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, duiCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuentas.add(convertirCuenta(rs));
                }
            }
        }

        return cuentas;
    }

    public boolean actualizarSaldo(
            String numeroCuenta,
            BigDecimal nuevoSaldo) throws SQLException {

        String sql = """
            UPDATE cuentas
            SET saldo = ?
            WHERE numero_cuenta = ?
            """;

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, nuevoSaldo);
            stmt.setString(2, numeroCuenta);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    public boolean eliminar(String numeroCuenta)
            throws SQLException {

        String sql =
            "DELETE FROM cuentas WHERE numero_cuenta = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCuenta);

            return stmt.executeUpdate() > 0;
        }
    }

    private Cuenta convertirCuenta(ResultSet rs)
            throws SQLException {

        Cuenta cuenta = new Cuenta(
            rs.getString("numero_cuenta"),
            rs.getString("dui_cliente"),
            rs.getString("tipo_cuenta")
        );

        cuenta.setSaldo(rs.getBigDecimal("saldo"));
        cuenta.setActiva(rs.getBoolean("activa"));

        return cuenta;
    }
}
