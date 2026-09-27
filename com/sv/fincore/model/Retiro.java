package com.sv.fincore.model;

import java.math.BigDecimal;

public class Retiro extends Transaccion {
    private static final long serialVersionUID = 1L;

    public Retiro(String id, String numeroCuenta, BigDecimal monto, String descripcion) {
        super(id, numeroCuenta, "RETIRO", monto, null, descripcion);
    }

    @Override
    public void ejecutar(Cuenta origen, Cuenta destino) {
        validarCuenta(origen);

        if (origen.getSaldo().compareTo(getMonto()) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        origen.setSaldo(origen.getSaldo().subtract(getMonto()));
        registrarSaldoPosterior(origen.getSaldo());
    }
}
