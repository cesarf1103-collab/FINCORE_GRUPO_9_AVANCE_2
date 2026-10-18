
package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void guardar(Cliente cliente) throws SQLException {
        String verificar = "SELECT id FROM clientes WHERE id = ?";

        String insertar = """
            INSERT INTO clientes
            (id, dui, nombre, apellidos, correo, telefono,
             direccion, fecha_nacimiento)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String actualizar = """
            UPDATE clientes
            SET dui = ?, nombre = ?, apellidos = ?,
                correo = ?, telefono = ?, direccion = ?,
                fecha_nacimiento = ?
            WHERE id = ?
            """;

        try (Connection conn = ConexionDB.obtenerConexion()) {
            boolean existe;

            try (PreparedStatement stmt =
                     conn.prepareStatement(verificar)) {
                stmt.setString(1, cliente.getId());

                try (ResultSet rs = stmt.executeQuery()) {
                    existe = rs.next();
                }
            }

            String sql = existe ? actualizar : insertar;

            try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

                int posicion = 1;

                if (!existe) {
                    stmt.setString(posicion++, cliente.getId());
                }

                stmt.setString(posicion++, cliente.getDui());
                stmt.setString(posicion++, cliente.getNombre());
                stmt.setString(posicion++, cliente.getApellidos());
                stmt.setString(posicion++, cliente.getCorreo());
                stmt.setString(posicion++, cliente.getTelefono());
                stmt.setString(posicion++, cliente.getDireccion());

                if (cliente.getFechaNacimiento() != null) {
                    stmt.setDate(
                        posicion++,
                        Date.valueOf(cliente.getFechaNacimiento())
                    );
                } else {
                    stmt.setNull(posicion++, Types.DATE);
                }

                if (existe) {
                    stmt.setString(posicion, cliente.getId());
                }

                stmt.executeUpdate();
            }
        }
    }

    public Cliente buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return convertirCliente(rs, conn);
                }
            }
        }

        return null;
    }

    public Cliente buscarPorDui(String dui) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE dui = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dui);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return convertirCliente(rs, conn);
                }
            }
        }

        return null;
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clientes.add(convertirCliente(rs, conn));
            }
        }

        return clientes;
    }

    public boolean eliminar(String id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existe(String id) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Cliente convertirCliente(
            ResultSet rs, Connection conn) throws SQLException {

        Date fecha = rs.getDate("fecha_nacimiento");

        Cliente cliente = new Cliente(
            rs.getString("id"),
            rs.getString("dui"),
            rs.getString("nombre"),
            rs.getString("apellidos"),
            rs.getString("correo"),
            rs.getString("telefono"),
            rs.getString("direccion"),
            fecha != null ? fecha.toLocalDate() : null
        );

        String sql = """
            SELECT numero_cuenta
            FROM cuentas
            WHERE dui_cliente = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getDui());

            try (ResultSet cuentas = stmt.executeQuery()) {
                while (cuentas.next()) {
                    cliente.agregarCuenta(
                        cuentas.getString("numero_cuenta")
                    );
                }
            }
        }

        return cliente;
    }
}
