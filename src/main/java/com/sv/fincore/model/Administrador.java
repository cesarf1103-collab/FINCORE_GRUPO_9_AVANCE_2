package com.sv.fincore.model;

public class Administrador {

    private String idAdministrador;
    private String nombre;
    private String correo;
    private boolean activo;

    public Administrador(String idAdministrador, String nombre, String correo) {
        this.idAdministrador = idAdministrador;
        this.nombre = nombre;
        this.correo = correo;
        this.activo = true;
    }

    public String getIdAdministrador() {
        return idAdministrador;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
