package com.sv.fincore.dao;

import java.io.IOException;
import java.util.List;

public interface CrudRepository<T, ID> {

    void guardar(T entidad) throws IOException;

    T buscarPorId(ID id);

    List<T> listar();

    boolean eliminar(ID id) throws IOException;
}