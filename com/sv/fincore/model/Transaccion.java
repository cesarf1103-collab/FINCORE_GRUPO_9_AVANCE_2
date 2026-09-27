package com.sv.fincore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Transaccion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idTransaccion;
    private String numeroCuenta;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal saldoPosterior;
    private LocalDateTime fecha;
    private String descripcion;
    private String cuentaOrigen;
    private String cuentaDestino;

    protected Transaccion(String idTransaccion, String numeroCuenta, String tipo,
                          BigDecimal monto, BigDecimal saldoPosterior, String descripcion) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto invalido");
        }

        this.idTransaccion = idTransaccion;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoPosterior = saldoPosterior;
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
    }

    protected Transaccion(String idTransaccion, String numeroCuenta, String tipo,
                          BigDecimal monto, BigDecimal saldoPosterior, String descripcion,
                          String cuentaOrigen, String cuentaDestino) {
        this(idTransaccion, numeroCuenta, tipo, monto, saldoPosterior, descripcion);
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
    }

    public abstract void ejecutar(Cuenta origen, Cuenta destino);

    protected void registrarSaldoPosterior(BigDecimal saldo) {
        this.saldoPosterior = saldo;
    }

    void restaurar(BigDecimal saldo, LocalDateTime fechaOriginal) {
        this.saldoPosterior = saldo;
        this.fecha = fechaOriginal;
    }

    protected static void validarCuenta(Cuenta cuenta) {
        if (cuenta == null) {
            throw new IllegalArgumentException("Cuenta no encontrada");
        }
        if (!cuenta.isActiva()) {
            throw new IllegalStateException("Cuenta inactiva");
        }
    }

    public String getIdTransaccion() { return idTransaccion; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getTipo() { return tipo; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
}
