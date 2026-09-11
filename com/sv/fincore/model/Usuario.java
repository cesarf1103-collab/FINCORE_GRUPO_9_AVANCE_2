package com.sv.fincore.model;

public class Usuario {
    private String idUsuario;
    private String username;
    private String password;
    private String rol; 
    private boolean activo;

    public Usuario(String idUsuario, String username, String password, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    public String getIdUsuario() { return idUsuario; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }

    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol) { this.rol = rol; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
