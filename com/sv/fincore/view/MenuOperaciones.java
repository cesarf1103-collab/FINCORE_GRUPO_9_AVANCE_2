package com.sv.fincore.view;

import com.sv.fincore.service.CuentaService;
import com.sv.fincore.validaciones.ConsolaBanco;

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
            System.out.println("4. Generar reporte bancario");
            System.out.println("5. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> depositar();
                case 2 -> retirar();
                case 3 -> transferir();
                case 4 -> System.out.println(
                        cuentaService.generarReporteEnSegundoPlano());
                case 5 -> System.out.println("Volviendo al menu principal...");
                default -> System.out.println("Opcion invalida");
            }
        } while (opcion != 5);
    }

    private void depositar() {
        String numeroCuenta =
                ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);

        System.out.println(cuentaService.depositar(numeroCuenta, monto));
    }

    private void retirar() {
        String numeroCuenta =
                ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);

        System.out.println(cuentaService.retirar(numeroCuenta, monto));
    }

    private void transferir() {
        String origen =
                ConsolaBanco.textoObligatorio(sc, "Cuenta origen: ");
        String destino =
                ConsolaBanco.textoObligatorio(sc, "Cuenta destino: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);

        System.out.println(cuentaService.transferir(origen, destino, monto));
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
