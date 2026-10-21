package com.sv.fincore.model;

public class AtencionCliente {

    private String idAtencion;
    private String nombreEmpleado;
    private String telefonoContacto;
    private boolean disponible;

    public AtencionCliente(String idAtencion, String nombreEmpleado, String telefonoContacto) {
        this.idAtencion = idAtencion;
        this.nombreEmpleado = nombreEmpleado;
        this.telefonoContacto = telefonoContacto;
        this.disponible = true;
    }

    public String getIdAtencion() {
        return idAtencion;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
