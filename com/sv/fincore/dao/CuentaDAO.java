package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC account DAO. Connection overloads let a service share one transaction. */
public class CuentaDAO implements CuentaRepository {
    private final TransaccionDAO transaccionDAO = new TransaccionDAO();
    @Override
    public void guardar(Cuenta cuenta) throws SQLException {
        try (Connection connection = ConexionDB.obtenerConexion()) { guardar(connection, cuenta); }
    }

    public void guardar(Connection connection, Cuenta cuenta) throws SQLException {
        String existeSql = "SELECT 1 FROM cuentas WHERE numero_cuenta = ?";
        boolean existe;
        try (PreparedStatement statement = connection.prepareStatement(existeSql)) {
            statement.setString(1, cuenta.getNumeroCuenta());
            try (ResultSet result = statement.executeQuery()) { existe = result.next(); }
        }
        String sql = existe
                ? "UPDATE cuentas SET dui_cliente = ?, saldo = ?, tipo_cuenta = ?, activa = ? WHERE numero_cuenta = ?"
                : "INSERT INTO cuentas (dui_cliente, saldo, tipo_cuenta, activa, numero_cuenta) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cuenta.getDuiCliente());
            statement.setBigDecimal(2, cuenta.getSaldo());
            statement.setString(3, cuenta.getTipoCuenta());
            statement.setBoolean(4, cuenta.isActiva());
            statement.setString(5, cuenta.getNumeroCuenta());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean actualizar(Cuenta cuenta) throws SQLException {
        String sql = "UPDATE cuentas SET dui_cliente = ?, saldo = ?, tipo_cuenta = ?, activa = ? WHERE numero_cuenta = ?";
        try (Connection connection = ConexionDB.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cuenta.getDuiCliente());
            statement.setBigDecimal(2, cuenta.getSaldo());
            statement.setString(3, cuenta.getTipoCuenta());
            statement.setBoolean(4, cuenta.isActiva());
            statement.setString(5, cuenta.getNumeroCuenta());
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Cuenta buscarPorNumero(String numeroCuenta) throws SQLException {
        try (Connection connection = ConexionDB.obtenerConexion()) { return buscarPorNumero(connection, numeroCuenta); }
    }

    @Override
    public Cuenta buscarPorNumero(Connection connection, String numeroCuenta) throws SQLException {
        String sql = "SELECT numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa FROM cuentas WHERE numero_cuenta = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, numeroCuenta);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? convertirCuenta(result, connection) : null; }
        }
    }

    @Override
    public Cuenta bloquearPorNumero(Connection connection, String numeroCuenta) throws SQLException {
        String sql = "SELECT numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa FROM cuentas WHERE numero_cuenta = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, numeroCuenta);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? convertirCuenta(result, connection) : null; }
        }
    }

    @Override
    public Cuenta buscarPorId(String id) throws SQLException {
        return buscarPorNumero(id);
    }

    @Override
    public List<Cuenta> listar() throws SQLException {
        try (Connection connection = ConexionDB.obtenerConexion()) {
            return listar(connection);
        }
    }

    @Override
    public List<Cuenta> listar(Connection connection) throws SQLException {
        List<Cuenta> cuentas = new ArrayList<>();
        String sql = "SELECT numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa FROM cuentas";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) cuentas.add(convertirCuenta(result, connection));
        }
        return cuentas;
    }

    @Override
    public List<Cuenta> listarPorDui(String duiCliente) throws SQLException {
        List<Cuenta> cuentas = new ArrayList<>();
        String sql = "SELECT numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa FROM cuentas WHERE dui_cliente = ?";
        try (Connection connection = ConexionDB.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, duiCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) cuentas.add(convertirCuenta(result, connection));
            }
        }
        return cuentas;
    }

    @Override
    public boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) throws SQLException {
        try (Connection connection = ConexionDB.obtenerConexion()) { return actualizarSaldo(connection, numeroCuenta, nuevoSaldo); }
    }

    @Override
    public boolean actualizarSaldo(Connection connection, String numeroCuenta, BigDecimal nuevoSaldo) throws SQLException {
        String sql = "UPDATE cuentas SET saldo = ? WHERE numero_cuenta = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, nuevoSaldo);
            statement.setString(2, numeroCuenta);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean eliminar(String numeroCuenta) throws SQLException {
        String sql = "DELETE FROM cuentas WHERE numero_cuenta = ?";
        try (Connection connection = ConexionDB.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, numeroCuenta);
            return statement.executeUpdate() == 1;
        }
    }

    private Cuenta convertirCuenta(ResultSet result, Connection connection) throws SQLException {
        Cuenta cuenta = new Cuenta(result.getString("numero_cuenta"), result.getString("dui_cliente"), result.getString("tipo_cuenta"));
        cuenta.setSaldo(result.getBigDecimal("saldo"));
        cuenta.setActiva(result.getBoolean("activa"));
        for (TransaccionRegistro registro : transaccionDAO.listarPorCuenta(connection, cuenta.getNumeroCuenta())) {
            cuenta.agregarTransaccion(registro.aModelo());
        }
        return cuenta;
    }
}
