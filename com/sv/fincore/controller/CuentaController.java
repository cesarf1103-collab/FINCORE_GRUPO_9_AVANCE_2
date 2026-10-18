package com.sv.fincore.controller;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.CuentaService;
import java.util.List;

public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    public String crearCuenta(String numeroCuenta, String duiCliente, String tipoCuenta) {
        return cuentaService.crearCuenta(numeroCuenta, duiCliente, tipoCuenta);
    }

    public List<Cuenta> listarCuentas() {
        return cuentaService.listarCuentas();
    }

    public Cuenta buscarCuenta(String numeroCuenta) {
        return cuentaService.buscarCuenta(numeroCuenta);
    }

    public List<Transaccion> obtenerHistorial(String numeroCuenta) {
        return cuentaService.obtenerHistorial(numeroCuenta);
    }

    public String desactivarCuenta(String numeroCuenta) {
        return cuentaService.desactivarCuenta(numeroCuenta);
    }
}
