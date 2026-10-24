package com.sv.fincore.view;

import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.validaciones.ConsolaBanco;

import java.math.BigDecimal;
import java.util.Scanner;

public class MenuOperaciones extends MenuBase {

    private final TransaccionController transaccionController;

    public MenuOperaciones(Scanner sc, TransaccionController transaccionController) {
        super(sc);
        this.transaccionController = transaccionController;
    }

    public void mostrar() {
        int opcion;

        do {
            imprimirMenu("MENU OPERACIONES", "Depositar", "Retirar",
                    "Transferir", "Volver al menu principal");

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
        String numeroCuenta = ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);
        System.out.println(transaccionController.depositar(numeroCuenta, monto));
    }

    private void retirar() {
        String numeroCuenta = ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);
        String duiPresentado = ConsolaBanco.dui(sc, "DUI del documento presentado: ");
        System.out.print("Confirma que comprobó físicamente la identidad del titular (S/N): ");
        boolean documentoComprobado = sc.nextLine().trim().equalsIgnoreCase("S");
        System.out.println(transaccionController.retirarEnVentanilla(
                numeroCuenta, monto, duiPresentado, documentoComprobado));
    }

    private void transferir() {
        String origen = ConsolaBanco.textoObligatorio(sc, "Cuenta origen: ");
        String destino = ConsolaBanco.textoObligatorio(sc, "Cuenta destino: ");
        BigDecimal monto = ConsolaBanco.montoPositivo(sc);
        System.out.println(transaccionController.transferir(origen, destino, monto));
    }

}
