package com.sv.fincore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

public class Transaccion {
    private String idTransaccion;
    private String numeroCuenta;
    private String tipo; 
    private BigDecimal monto;
    private BigDecimal saldoPosterior;
    private LocalDateTime fecha;
    private String descripcion;
    private String cuentaOrigen; 
    private String cuentaDestino; 

    public Transaccion(String idTransaccion, String numeroCuenta, String tipo, 
                       BigDecimal monto, BigDecimal saldoPosterior, String descripcion) {
        this.idTransaccion = idTransaccion;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoPosterior = saldoPosterior;
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
    }

    // Constructor para transferencias
    public Transaccion(String idTransaccion, String numeroCuenta, String tipo, 
                       BigDecimal monto, BigDecimal saldoPosterior, String descripcion,
                       String cuentaOrigen, String cuentaDestino) {
        this(idTransaccion, numeroCuenta, tipo, monto, saldoPosterior, descripcion);
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
    }

    // Getters
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
