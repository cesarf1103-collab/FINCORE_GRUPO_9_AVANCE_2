package com.sv.fincore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Cliente {
    private String id;
    private String dui;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private List<String> numerosCuenta;

    public Cliente(String id, String dui, String nombre, String apellidos, String correo, String telefono, String direccion, LocalDate fechaNacimiento) {
        this.id = id;
        this.dui = dui;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.numerosCuenta = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getDui() { return dui; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public List<String> getNumerosCuenta() { return numerosCuenta; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public void agregarCuenta(String numeroCuenta) {
        this.numerosCuenta.add(numeroCuenta);
    }
}
