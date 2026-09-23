package com.sv.fincore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Cuenta {
    private String numeroCuenta;
    private String duiCliente;
    private BigDecimal saldo;
    private String tipoCuenta; 
    private LocalDateTime fechaApertura;
    private boolean activa;
    private List<Transaccion> historial;

    public Cuenta(String numeroCuenta, String duiCliente, String tipoCuenta) {
        this.numeroCuenta = numeroCuenta;
        this.duiCliente = duiCliente;
        this.saldo = BigDecimal.ZERO;
        this.tipoCuenta = tipoCuenta;
        this.fechaApertura = LocalDateTime.now();
        this.activa = true;
        this.historial = new ArrayList<>();
    }

    // Getters y Setters
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getDuiCliente() { return duiCliente; }
    public BigDecimal getSaldo() { return saldo; }
    public String getTipoCuenta() { return tipoCuenta; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public boolean isActiva() { return activa; }
    public List<Transaccion> getHistorial() { return historial; }

    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public void agregarTransaccion(Transaccion transaccion) {
        this.historial.add(transaccion);
    }
}
