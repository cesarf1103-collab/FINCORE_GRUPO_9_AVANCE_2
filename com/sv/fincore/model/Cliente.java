package com.sv.fincore.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modulo.persona.Persona;

/**
 * Entidad Cliente adaptada a la arquitectura FinCore heredando de Persona.
 */
public class Cliente extends Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dui;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private List<String> numerosCuenta;

    public Cliente(String id, String dui, String nombre, String apellidos, String correo, String telefono, String direccion, LocalDate fechaNacimiento) {
        super(id, nombre, correo, telefono, direccion);
        this.dui = dui;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.numerosCuenta = new ArrayList<>();
    }

    // Getters
    public String getDui() {
        return dui;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public List<String> getNumerosCuenta() {
        return numerosCuenta;
    }

    // Setters
    public void setDui(String dui) {
        this.dui = dui;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void agregarCuenta(String numeroCuenta) {
        this.numerosCuenta.add(numeroCuenta);
    }
}