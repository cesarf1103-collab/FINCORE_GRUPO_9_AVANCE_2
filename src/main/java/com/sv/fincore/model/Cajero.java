package com.sv.fincore.model;

import java.math.BigDecimal;

public class Cajero {
    private String idCajero;
    private String ubicacion;
    private BigDecimal saldoEfectivo;
    private boolean activo;

    public Cajero(String idCajero, String ubicacion, BigDecimal saldoInicial) {
        this.idCajero = idCajero;
        this.ubicacion = ubicacion;
        this.saldoEfectivo = saldoInicial;
        this.activo = true;
    }

    public String getIdCajero() { return idCajero; }
    public String getUbicacion() { return ubicacion; }
    public BigDecimal getSaldoEfectivo() { return saldoEfectivo; }
    public boolean isActivo() { return activo; }

    public void setSaldoEfectivo(BigDecimal saldoEfectivo) { this.saldoEfectivo = saldoEfectivo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
