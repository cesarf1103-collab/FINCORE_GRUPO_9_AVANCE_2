package com.sv.fincore.dao;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.util.ArchivoUtil;

import java.util.*;

public class ClienteDAO {

    private static final String RUTA = "datos/clientes.dat";

    private Map<String, Cliente> clientes;

    public ClienteDAO() {
        this.clientes = ArchivoUtil.cargar(RUTA, new HashMap<>());
    }

    public void guardar(Cliente c) {
        clientes.put(c.getId(), c);
        persistir();
    }

    public Cliente buscarPorId(String id) {
        return clientes.get(id);
    }

    public Cliente buscarPorDui(String dui) {
        return clientes.values().stream()
                .filter(c -> c.getDui().equals(dui))
                .findFirst()
                .orElse(null);
    }

    public List<Cliente> listar() {
        return new ArrayList<>(clientes.values());
    }

    public boolean eliminar(String id) {
        if (clientes.remove(id) != null) {
            persistir();
            return true;
        }
        return false;
    }

    public boolean existe(String id) {
        return clientes.containsKey(id);
    }

    private void persistir() {
        ArchivoUtil.guardar(RUTA, clientes);
    }
}
