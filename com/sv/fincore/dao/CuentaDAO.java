package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ArchivoUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CuentaDAO {

    private static final String RUTA = "datos/cuentas.dat";

    private Map<String, Cuenta> cuentas;

    public CuentaDAO() {
        this.cuentas = ArchivoUtil.cargar(RUTA, new HashMap<>());
    }

    public void guardar(Cuenta c) {
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

    public boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) {
        Cuenta c = cuentas.get(numeroCuenta);
        if (c == null) return false;
        c.setSaldo(nuevoSaldo);
        persistir();
        return true;
    }

    public boolean eliminar(String numeroCuenta) {
        if (cuentas.remove(numeroCuenta) != null) {
            persistir();
            return true;
        }
        return false;
    }

    private void persistir() {
        ArchivoUtil.guardar(RUTA, cuentas);
    }
}
