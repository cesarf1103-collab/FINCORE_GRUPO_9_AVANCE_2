package com.sv.fincore.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modulo.persona.Persona;

public class Cliente extends Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dui;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private List<String> numerosCuenta;

    public Cliente(String id, String dui, String nombre, String apellidos,
                   String correo, String telefono, String direccion,
                   LocalDate fechaNacimiento) {
        super(id, nombre, correo, telefono, direccion);
        this.dui = dui;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.numerosCuenta = new ArrayList<>();
    }

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

    public void setDui(String dui) {
        this.dui = dui;
    }

    @Override
    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            super.setNombre(nombre);
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

    @Override
    public void setCorreo(String correo) {
        if (correo != null && correo.contains("@")) {
            super.setCorreo(correo);
        } else {
            System.out.println("Correo inválido");
        }
    }

    @Override
    public void setTelefono(String telefono) {
        if (telefono != null && telefono.matches("\\d{8}")) {
            super.setTelefono(telefono);
        } else {
            System.out.println("Teléfono inválido");
        }
    }

    @Override
    public void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            super.setDireccion(direccion);
        } else {
            System.out.println("Dirección inválida");
        }
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null
                && !fechaNacimiento.isAfter(LocalDate.now())) {
            this.fechaNacimiento = fechaNacimiento;
        } else {
            System.out.println("Fecha de nacimiento inválida");
        }
    }

    public void agregarCuenta(String numeroCuenta) {
        this.numerosCuenta.add(numeroCuenta);
    }

    @Override
    public String getTipoPersona() {
        return "Cliente";
    }
}