package com.sv.fincore.model;

import java.math.BigDecimal;

public class Deposito extends Transaccion {
    private static final long serialVersionUID = 1L;

    public Deposito(String id, String numeroCuenta, BigDecimal monto, String descripcion) {
        super(id, numeroCuenta, "DEPOSITO", monto, null, descripcion);
    }

    @Override
    public void ejecutar(Cuenta origen, Cuenta destino) {
        validarCuenta(origen);
        origen.setSaldo(origen.getSaldo().add(getMonto()));
        registrarSaldoPosterior(origen.getSaldo());
    }
}
