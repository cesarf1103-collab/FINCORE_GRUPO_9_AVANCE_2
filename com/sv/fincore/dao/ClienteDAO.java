package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void guardar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (id, dui, nombre, apellidos, correo, telefono, direccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, c.getId());
            stmt.setString(2, c.getDui());
            stmt.setString(3, c.getNombre());
            stmt.setString(4, c.getApellidos());
            stmt.setString(5, c.getCorreo());
            stmt.setString(6, c.getTelefono());
            stmt.setString(7, c.getDireccion());
            
            stmt.executeUpdate();
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("id"),
                    rs.getString("dui"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("correo"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null
                );
                lista.add(c);
            }
        }
        return lista;
    }
}
