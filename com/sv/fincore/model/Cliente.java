package com.sv.fincore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre;
        } else {
            System.out.println("Nombre inválido");
        }
    }
    public void setApellidos(String apellidos) {
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            this.apellidos = apellidos;
        } else {
            System.out.println("Apellidos inválidos");
        }
    }
    public void setCorreo(String correo) {
        if (correo != null && correo.contains("@")) {
            this.correo = correo;
        } else {
            System.out.println("Correo inválido");
        }
    }

    public void setTelefono(String telefono) {
        if (telefono != null && telefono.matches("\\d{8}")) {
            this.telefono = telefono;
        } else {
            System.out.println("Teléfono inválido");
        }
    }
    public void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion;
        } else {
            System.out.println("Dirección inválida");
        }
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null && !fechaNacimiento.isAfter(LocalDate.now())) {
            this.fechaNacimiento = fechaNacimiento;
        } else {
            System.out.println("Fecha de nacimiento inválida");
        }
    }


    public void agregarCuenta(String numeroCuenta) {
        this.numerosCuenta.add(numeroCuenta);
    }
}