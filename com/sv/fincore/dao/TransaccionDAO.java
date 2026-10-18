package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import com.sv.fincore.util.ArchivoUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TransaccionDAO implements CrudRepository<Transaccion, String> {

    private static final String RUTA = "datos/transacciones.dat";

    private List<Transaccion> transacciones;

    public TransaccionDAO() throws IOException, ClassNotFoundException {
        this.transacciones = ArchivoUtil.cargar(RUTA, new ArrayList<>());
    }

    @Override
    public void guardar(Transaccion t) throws IOException {
        transacciones.add(t);
        persistir();
    }

    @Override
    public List<Transaccion> listar() {
        return new ArrayList<>(transacciones);
    }

    public List<Transaccion> listarPorCuenta(String numeroCuenta) {
        return transacciones.stream()
                .filter(t -> t.getNumeroCuenta().equals(numeroCuenta))
                .collect(Collectors.toList());
    }

    @Override
    public Transaccion buscarPorId(String idTransaccion) {
        return transacciones.stream()
                .filter(t -> t.getIdTransaccion().equals(idTransaccion))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Long> resumenPorTipo() {
        return transacciones.stream()
                .collect(Collectors.groupingBy(
                        Transaccion::getTipo,
                        Collectors.counting()));
    }

    @Override
    public boolean eliminar(String id) throws IOException {
        Transaccion transaccion = buscarPorId(id);

        if (transaccion != null) {
            transacciones.remove(transaccion);
            persistir();
            return true;
        }

        return false;
    }

    private void persistir() throws IOException {
        ArchivoUtil.guardar(RUTA, transacciones);
    }
}