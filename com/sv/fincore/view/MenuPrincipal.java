package com.sv.fincore.view;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.service.CuentaService;

import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner sc;
    private final MenuClientes menuClientes;
    private final MenuCuentas menuCuentas;
    private final MenuOperaciones menuOperaciones;

    public MenuPrincipal(ClienteController clienteController, CuentaService cuentaService) {
        this.sc = new Scanner(System.in);
        this.menuClientes = new MenuClientes(sc, clienteController);
        this.menuCuentas = new MenuCuentas(sc, cuentaService);
        this.menuOperaciones = new MenuOperaciones(sc, cuentaService);
    }

    public void iniciar() {

        int opcion;

        do {
            mostrarMenu();
            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> menuClientes.mostrar();
                case 2 -> menuCuentas.mostrar();
                case 3 -> menuOperaciones.mostrar();
                case 4 -> System.out.println("Gracias por usar FinCore");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != 4);

        sc.close();
    }

    private void mostrarMenu() {
        System.out.println("\n===== FINCORE - MENU PRINCIPAL =====");
        System.out.println("1. Clientes");
        System.out.println("2. Cuentas");
        System.out.println("3. Operaciones");
        System.out.println("4. Salir");
        System.out.print("Seleccione una opcion: ");
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}