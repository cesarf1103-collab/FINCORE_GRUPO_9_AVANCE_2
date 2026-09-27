package com.sv.fincore.validaciones;

import java.math.BigDecimal;
import java.util.Scanner;

public final class ConsolaBanco {

    private ConsolaBanco() {
    }

    public static String textoObligatorio(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = sc.nextLine().trim();

            if (!valor.isEmpty()) {
                return valor;
            }

            System.out.println("Este campo es obligatorio. Intente de nuevo.");
        }
    }

    public static String dui(Scanner sc, String mensaje) {
        while (true) {
            String valor = textoObligatorio(sc, mensaje);

            if (valor.matches("\\d{8}-\\d")) {
                return valor;
            }

            System.out.println("DUI inválido. Use el formato 12345678-9.");
        }
    }

    public static BigDecimal montoPositivo(Scanner sc) {
        while (true) {
            System.out.print("Monto: ");
            String valor = sc.nextLine().trim();

            try {
                BigDecimal monto = new BigDecimal(valor);

                if (monto.compareTo(BigDecimal.ZERO) > 0) {
                    return monto;
                }
            } catch (NumberFormatException e) {
                // La entrada no es un número.
            }

            System.out.println("Monto inválido. Ingrese un número mayor que cero.");
        }
    }
}
