
package com.sv.fincore.view;

import com.sv.fincore.validaciones.ConsolaBanco;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.controller.ClienteController;

import java.time.LocalDate;
import java.util.Scanner;

public class MenuClientes {

    private final Scanner sc;
    private final ClienteController clienteController;

    public MenuClientes(Scanner sc, ClienteController clienteController) {
        this.sc = sc;
        this.clienteController = clienteController;
    }

    public void mostrar() {

        int opcion;

        do {
            System.out.println("\n----- MENU CLIENTES -----");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar cliente por DUI");
            System.out.println("4. Modificar cliente");
            System.out.println("5. Eliminar cliente");
            System.out.println("6. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> listarClientes();
                case 3 -> buscarCliente();
                case 4 -> modificarCliente();
                case 5 -> eliminarCliente();
                case 6 -> System.out.println("Volviendo al menu principal...");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != 6);
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
                id, dui, nombre, apellidos, correo, telefono, direccion, fechaNacimiento
        );

        System.out.println(clienteController.registrarCliente(cliente));
    }

    private void listarClientes() {

        if (clienteController.listarClientes().isEmpty()) {
            System.out.println("No hay clientes registrados");
            return;
        }

        for (Cliente c : clienteController.listarClientes()) {
            System.out.println("-------------------------");
            System.out.println("DUI: " + c.getDui());
            System.out.println("Nombre: " + c.getNombre() + " " + c.getApellidos());
            System.out.println("Correo: " + c.getCorreo());
            System.out.println("Telefono: " + c.getTelefono());
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
        System.out.println("Correo: " + cliente.getCorreo());
        System.out.println("Telefono: " + cliente.getTelefono());
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
                clienteController.modificarCliente(dui, nombre, apellidos, correo, telefono, direccion)
        );
    }

    private void eliminarCliente() {

        String dui = ConsolaBanco.dui(sc, "DUI del cliente a eliminar: ");

        System.out.println(clienteController.eliminarCliente(dui));
    }

    private LocalDate leerFecha() {

        while (true) {
            try {
                System.out.print("Anio de nacimiento: ");
                int anio = Integer.parseInt(sc.nextLine().trim());

                System.out.print("Mes de nacimiento: ");
                int mes = Integer.parseInt(sc.nextLine().trim());

                System.out.print("Dia de nacimiento: ");
                int dia = Integer.parseInt(sc.nextLine().trim());

                return LocalDate.of(anio, mes, dia);

            } catch (Exception e) {
                System.out.println("Fecha invalida, intente de nuevo");
            }
        }
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
