package com.sv.fincore.controller;

import com.sv.fincore.service.CuentaService;
import java.math.BigDecimal;

public class TransaccionController {

    private final CuentaService cuentaService;

    public TransaccionController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    public String depositar(String numeroCuenta, BigDecimal monto) {
        return cuentaService.depositar(numeroCuenta, monto);
    }

    public String retirar(String numeroCuenta, BigDecimal monto) {
        return cuentaService.retirar(numeroCuenta, monto);
    }

    public String transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        return cuentaService.transferir(cuentaOrigen, cuentaDestino, monto);
    }
}
