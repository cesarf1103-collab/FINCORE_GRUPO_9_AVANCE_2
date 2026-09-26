package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ArchivoUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CuentaDAO {

    private static final String RUTA = "datos/cuentas.dat";

    private Map<String, Cuenta> cuentas;

    public CuentaDAO() throws IOException, ClassNotFoundException {
        this.cuentas = ArchivoUtil.cargar(RUTA, new HashMap<>());
    }

    public void guardar(Cuenta c) throws IOException {
        cuentas.put(c.getNumeroCuenta(), c);
        persistir();
    }

    public Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    public List<Cuenta> listar() {
        return new ArrayList<>(cuentas.values());
    }

    public List<Cuenta> listarPorDui(String duiCliente) {
        return cuentas.values().stream()
                .filter(c -> c.getDuiCliente().equals(duiCliente))
                .collect(Collectors.toList());
    }

    public boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) throws IOException {
        Cuenta c = cuentas.get(numeroCuenta);
        if (c == null) return false;
        c.setSaldo(nuevoSaldo);
        persistir();
        return true;
    }

    public boolean eliminar(String numeroCuenta) throws IOException {
        if (cuentas.remove(numeroCuenta) != null) {
            persistir();
            return true;
        }
        return false;
    }

    private void persistir() throws IOException {
        ArchivoUtil.guardar(RUTA, cuentas);
    }
}
