package com.sv.fincore.dao;

import com.sv.fincore.model.Transaccion;
import com.sv.fincore.util.ArchivoUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TransaccionDAO {

    private static final String RUTA = "datos/transacciones.dat";

    private List<Transaccion> transacciones;

    public TransaccionDAO() {
        this.transacciones = ArchivoUtil.cargar(RUTA, new ArrayList<>());
    }

    public void guardar(Transaccion t) {
        transacciones.add(t);
        persistir();
    }

    public List<Transaccion> listar() {
        return new ArrayList<>(transacciones);
    }

    public List<Transaccion> listarPorCuenta(String numeroCuenta) {
        return transacciones.stream()
                .filter(t -> t.getNumeroCuenta().equals(numeroCuenta))
                .collect(Collectors.toList());
    }

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

    private void persistir() {
        ArchivoUtil.guardar(RUTA, transacciones);
    }
}
