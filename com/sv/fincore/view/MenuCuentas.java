package com.sv.fincore.view;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.CuentaService;

import java.util.List;
import java.util.Scanner;

public class MenuCuentas {

    private final Scanner sc;
    private final CuentaService cuentaService;

    public MenuCuentas(Scanner sc, CuentaService cuentaService) {
        this.sc = sc;
        this.cuentaService = cuentaService;
    }

    public void mostrar() {

        int opcion;

        do {
            System.out.println("\n----- MENU CUENTAS -----");
            System.out.println("1. Crear cuenta");
            System.out.println("2. Listar cuentas");
            System.out.println("3. Ver historial de cuenta");
            System.out.println("4. Desactivar cuenta");
            System.out.println("5. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> crearCuenta();
                case 2 -> listarCuentas();
                case 3 -> verHistorial();
                case 4 -> desactivarCuenta();
                case 5 -> System.out.println("Volviendo al menu principal...");
                default -> System.out.println("Opcion invalida");
            }

        } while (opcion != 5);
    }

    private void crearCuenta() {

        System.out.print("Numero de cuenta: ");
        String numeroCuenta = sc.nextLine();

        System.out.print("DUI del cliente: ");
        String duiCliente = sc.nextLine();

        System.out.print("Tipo de cuenta: ");
        String tipoCuenta = sc.nextLine();

        System.out.println(cuentaService.crearCuenta(numeroCuenta, duiCliente, tipoCuenta));
    }

    private void listarCuentas() {

        if (cuentaService.listarCuentas().isEmpty()) {
            System.out.println("No hay cuentas registradas");
            return;
        }

        for (Cuenta cuenta : cuentaService.listarCuentas()) {
            System.out.println("-------------------------");
            System.out.println("Cuenta: " + cuenta.getNumeroCuenta());
            System.out.println("DUI Cliente: " + cuenta.getDuiCliente());
            System.out.println("Saldo: $" + cuenta.getSaldo());
            System.out.println("Tipo: " + cuenta.getTipoCuenta());
            System.out.println("Estado: " + (cuenta.isActiva() ? "Activa" : "Inactiva"));
        }
    }

    private void verHistorial() {

        System.out.print("Numero de cuenta: ");
        String numeroCuenta = sc.nextLine();

        List<Transaccion> historial = cuentaService.obtenerHistorial(numeroCuenta);

        if (historial.isEmpty()) {
            System.out.println("No hay transacciones para esta cuenta");
            return;
        }

        for (Transaccion t : historial) {
            System.out.println("-------------------------");
            System.out.println("Tipo: " + t.getTipo());
            System.out.println("Monto: " + t.getMonto());
            System.out.println("Saldo posterior: " + t.getSaldoPosterior());
            System.out.println("Fecha: " + t.getFecha());
        }
    }

    private void desactivarCuenta() {

        System.out.print("Numero de cuenta a desactivar: ");
        String numeroCuenta = sc.nextLine();

        System.out.println(cuentaService.desactivarCuenta(numeroCuenta));
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
