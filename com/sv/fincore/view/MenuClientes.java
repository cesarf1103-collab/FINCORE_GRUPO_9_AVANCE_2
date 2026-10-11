package com.sv.fincore.view;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.validaciones.ConsolaBanco;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Scanner;

public class MenuClientes extends MenuBase {

    private static final int OPCION_VOLVER = 6;

    private final ClienteController clienteController;

    public MenuClientes(Scanner sc, ClienteController clienteController) {
        super(sc);
        this.clienteController = clienteController;
    }

    public void mostrar() {
        int opcion;

        do {
            imprimirMenu("MENU CLIENTES",
                    "Registrar cliente",
                    "Listar clientes",
                    "Buscar cliente por DUI",
                    "Modificar cliente",
                    "Eliminar cliente",
                    "Volver al menu principal");

            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> listarClientes();
                case 3 -> buscarCliente();
                case 4 -> modificarCliente();
                case 5 -> eliminarCliente();
                case OPCION_VOLVER -> System.out.println("Volviendo al menu principal...");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != OPCION_VOLVER);
    }

    private void registrarCliente() {
        String id = ConsolaBanco.textoObligatorio(sc, "ID: ");
        String dui = ConsolaBanco.dui(sc, "DUI: ");
        String nombre = ConsolaBanco.textoObligatorio(sc, "Nombre: ");
        String apellidos = ConsolaBanco.textoObligatorio(sc, "Apellidos: ");
        String correo = ConsolaBanco.textoObligatorio(sc, "Correo: ");
        String telefono = ConsolaBanco.textoObligatorio(sc, "Telefono: ");
        String direccion = ConsolaBanco.textoObligatorio(sc, "Direccion: ");
        LocalDate fechaNacimiento = leerFecha();

        Cliente cliente = new Cliente(
                id, dui, nombre, apellidos, correo, telefono, direccion, fechaNacimiento);

        System.out.println(clienteController.registrarCliente(cliente));
    }

    private void listarClientes() {
        var clientes = clienteController.listarClientes();  

        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados");
            return;
        }

        for (Cliente c : clientes) {
            imprimirSeparador();
            System.out.println("DUI: " + c.getDui());
            System.out.println("Nombre: " + c.getNombre() + " " + c.getApellidos());
            imprimirContacto(c);
        }
    }

    private void buscarCliente() {
        String dui = ConsolaBanco.dui(sc, "Ingrese DUI: ");
        Cliente cliente = clienteController.buscarPorDui(dui);

        if (cliente == null) {
            System.out.println("Cliente no encontrado");
            return;
        }

        System.out.println("Nombre: " + cliente.getNombre());
        System.out.println("Apellidos: " + cliente.getApellidos());
        imprimirContacto(cliente);
        System.out.println("Direccion: " + cliente.getDireccion());
        System.out.println("Cuentas asociadas: " + cliente.getNumerosCuenta());
    }

    private void modificarCliente() {
        String dui = ConsolaBanco.dui(sc, "DUI del cliente a modificar: ");
        String nombre = ConsolaBanco.textoObligatorio(sc, "Nuevo nombre: ");
        String apellidos = ConsolaBanco.textoObligatorio(sc, "Nuevos apellidos: ");
        String correo = ConsolaBanco.textoObligatorio(sc, "Nuevo correo: ");
        String telefono = ConsolaBanco.textoObligatorio(sc, "Nuevo telefono: ");
        String direccion = ConsolaBanco.textoObligatorio(sc, "Nueva direccion: ");

        System.out.println(
                clienteController.modificarCliente(dui, nombre, apellidos, correo, telefono, direccion));
    }

    private void eliminarCliente() {
        String dui = ConsolaBanco.dui(sc, "DUI del cliente a eliminar: ");
        System.out.println(clienteController.eliminarCliente(dui));
    }

    private void imprimirContacto(Cliente c) {
        System.out.println("Correo: " + c.getCorreo());
        System.out.println("Telefono: " + c.getTelefono());
    }

    private LocalDate leerFecha() {
        while (true) {
            try {
                int anio = leerEntero("Anio de nacimiento: ");
                int mes = leerEntero("Mes de nacimiento: ");
                int dia = leerEntero("Dia de nacimiento: ");
                return LocalDate.of(anio, mes, dia);
            } catch (NumberFormatException | DateTimeException e) {
                System.out.println("Fecha invalida, intente de nuevo");
            }
        }
    }
}