package com.sv.fincore.view;

import java.util.Scanner;

public abstract class MenuBase {

    protected final Scanner sc;

    protected MenuBase(Scanner sc) {
        this.sc = sc;
    }

    protected void imprimirMenu(String titulo, String... opciones) {
        System.out.println("\n----- " + titulo + " -----");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ". " + opciones[i]);
        }
        System.out.print("Seleccione una opcion: ");
    }

    protected int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected int leerEntero(String mensaje) {
        System.out.print(mensaje);
        return Integer.parseInt(sc.nextLine().trim());
    }

    protected void imprimirSeparador() {
        System.out.println("-------------------------");
    }
}