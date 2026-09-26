package com.sv.fincore.view;

import com.sv.fincore.service.CuentaService;

import java.math.BigDecimal;
import java.util.Scanner;

public class MenuOperaciones {

    private final Scanner sc;
    private final CuentaService cuentaService;

    public MenuOperaciones(Scanner sc, CuentaService cuentaService) {
        this.sc = sc;
        this.cuentaService = cuentaService;
    }

    public void mostrar() {

        int opcion;

        do {
            System.out.println("\n----- MENU OPERACIONES -----");
            System.out.println("1. Depositar");
            System.out.println("2. Retirar");
            System.out.println("3. Transferir");
            System.out.println("4. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> depositar();
                case 2 -> retirar();
                case 3 -> transferir();
                case 4 -> System.out.println("Volviendo al menu principal...");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != 4);
    }

    private void depositar() {

        System.out.print("Numero de cuenta: ");
        String numeroCuenta = sc.nextLine();

        BigDecimal monto = leerMonto();

        System.out.println(cuentaService.depositar(numeroCuenta, monto));
    }

    private void retirar() {

        System.out.print("Numero de cuenta: ");
        String numeroCuenta = sc.nextLine();

        BigDecimal monto = leerMonto();

        System.out.println(cuentaService.retirar(numeroCuenta, monto));
    }

    private void transferir() {

        System.out.print("Cuenta origen: ");
        String origen = sc.nextLine();

        System.out.print("Cuenta destino: ");
        String destino = sc.nextLine();

        BigDecimal monto = leerMonto();

        System.out.println(cuentaService.transferir(origen, destino, monto));
    }

    private BigDecimal leerMonto() {

        while (true) {
            try {
                System.out.print("Monto: ");
                return new BigDecimal(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Monto invalido, intente de nuevo");
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
