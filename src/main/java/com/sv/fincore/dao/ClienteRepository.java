package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import java.sql.SQLException;

public interface ClienteRepository extends CrudRepository<Cliente, String> {
    Cliente buscarPorDui(String dui) throws SQLException;
    boolean existe(String id) throws SQLException;
}
