package com.sv.fincore.dao;

import java.sql.SQLException;
import java.util.List;

public interface ReadRepository<T, ID> {
    T buscarPorId(ID id) throws SQLException;
    List<T> listar() throws SQLException;
}
