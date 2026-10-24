
package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements ClienteRepository {

    @Override
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

    @Override
    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET dui = ?, nombre = ?, apellidos = ?, correo = ?, telefono = ?, direccion = ?, fecha_nacimiento = ? WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getDui());
            stmt.setString(2, cliente.getNombre());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getCorreo());
            stmt.setString(5, cliente.getTelefono());
            stmt.setString(6, cliente.getDireccion());
            if (cliente.getFechaNacimiento() == null) stmt.setNull(7, Types.DATE);
            else stmt.setDate(7, Date.valueOf(cliente.getFechaNacimiento()));
            stmt.setString(8, cliente.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean eliminar(String id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
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
