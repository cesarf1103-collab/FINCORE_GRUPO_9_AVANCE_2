package com.sv.fincore.view;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;

import java.util.Scanner;

public class MenuPrincipal {

    private static final int OPCION_SALIR = 5;

    private final Scanner sc;
    private final MenuClientes menuClientes;
    private final MenuCuentas menuCuentas;
    private final MenuOperaciones menuOperaciones;
    private final CuentaController cuentaController;

    public MenuPrincipal(ClienteController clienteController,
                         CuentaController cuentaController,
                         TransaccionController transaccionController) {
        this.sc = new Scanner(System.in);
        this.cuentaController = cuentaController;
        this.menuClientes = new MenuClientes(sc, clienteController);
        this.menuCuentas = new MenuCuentas(sc, cuentaController);
        this.menuOperaciones = new MenuOperaciones(sc, transaccionController);
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
                case 4 -> System.out.println(cuentaController.generarReporteEnSegundoPlano());
                case OPCION_SALIR -> System.out.println("Gracias por usar FinCore");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != OPCION_SALIR);

        sc.close();
    }

    private void mostrarMenu() {
        System.out.println("\n===== FINCORE - MENU PRINCIPAL =====");
        System.out.println("1. Clientes");
        System.out.println("2. Cuentas");
        System.out.println("3. Operaciones");
        System.out.println("4. Generar reporte bancario (segundo plano)");
        System.out.println("5. Salir");
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