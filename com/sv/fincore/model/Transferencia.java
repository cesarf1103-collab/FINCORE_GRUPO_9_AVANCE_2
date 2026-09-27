package com.sv.fincore.model;

import java.math.BigDecimal;

public class Transferencia extends Transaccion {
    private static final long serialVersionUID = 1L;

    public Transferencia(String id, String origen, String destino,
                         BigDecimal monto, String descripcion) {
        this(id, origen, origen, destino, monto, null, descripcion);
    }

    private Transferencia(String id, String numeroCuenta, String origen, String destino,
                          BigDecimal monto, BigDecimal saldoPosterior, String descripcion) {
        super(id, numeroCuenta, "TRANSFERENCIA", monto, saldoPosterior,
                descripcion, origen, destino);
    }

    @Override
    public void ejecutar(Cuenta origen, Cuenta destino) {
        validarCuenta(origen);
        validarCuenta(destino);

        if (origen.getNumeroCuenta().equals(destino.getNumeroCuenta())) {
            throw new IllegalArgumentException("Las cuentas deben ser diferentes");
        }
        if (origen.getSaldo().compareTo(getMonto()) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        origen.setSaldo(origen.getSaldo().subtract(getMonto()));
        destino.setSaldo(destino.getSaldo().add(getMonto()));
        registrarSaldoPosterior(origen.getSaldo());
    }

    public Transferencia registroDestino(String id, BigDecimal saldoDestino) {
        return new Transferencia(id, getCuentaDestino(), getCuentaOrigen(),
                getCuentaDestino(), getMonto(), saldoDestino,
                "Transferencia recibida");
    }

    static Transferencia restaurarRegistro(String id, String numeroCuenta,
                                           String origen, String destino,
                                           BigDecimal monto, String descripcion) {
        return new Transferencia(id, numeroCuenta, origen, destino,
                monto, null, descripcion);
    }
}
