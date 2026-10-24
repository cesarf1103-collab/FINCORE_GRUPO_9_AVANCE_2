package com.sv.fincore.view;

import com.sv.fincore.validaciones.ConsolaBanco;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.controller.CuentaController;

import java.util.List;
import java.util.Scanner;

public class MenuCuentas extends MenuBase {

    private final CuentaController cuentaController;

    public MenuCuentas(Scanner sc, CuentaController cuentaController) {
        super(sc);
        this.cuentaController = cuentaController;
    }

    public void mostrar() {

        int opcion;

        do {
            imprimirMenu("MENU CUENTAS", "Crear cuenta", "Listar cuentas",
                    "Ver historial de cuenta", "Desactivar cuenta", "Volver al menu principal");

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

        String numeroCuenta = ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");

        String duiCliente = ConsolaBanco.dui(sc, "DUI del cliente: ");

        String tipoCuenta = ConsolaBanco.textoObligatorio(sc, "Tipo de cuenta: ");

        System.out.println(cuentaController.crearCuenta(numeroCuenta, duiCliente, tipoCuenta));
    }

    private void listarCuentas() {

        List<Cuenta> cuentas = cuentaController.listarCuentas();
        if (cuentas.isEmpty()) {
            System.out.println("No hay cuentas registradas");
            return;
        }

        for (Cuenta cuenta : cuentas) {
            imprimirSeparador();
            System.out.println("Cuenta: " + cuenta.getNumeroCuenta());
            System.out.println("DUI Cliente: " + cuenta.getDuiCliente());
            System.out.println("Saldo: " + FormatoSalida.moneda(cuenta.getSaldo()));
            System.out.println("Tipo: " + cuenta.getTipoCuenta());
            System.out.println("Estado: " + (cuenta.isActiva() ? "Activa" : "Inactiva"));
        }
    }

    private void verHistorial() {

        String numeroCuenta = ConsolaBanco.textoObligatorio(sc, "Numero de cuenta: ");

        List<Transaccion> historial = cuentaController.obtenerHistorial(numeroCuenta);

        if (historial.isEmpty()) {
            System.out.println("No hay transacciones para esta cuenta");
            return;
        }

        for (Transaccion t : historial) {
            imprimirSeparador();
            System.out.println("Tipo: " + t.getTipo());
            System.out.println("Monto: " + FormatoSalida.moneda(t.getMonto()));
            System.out.println("Saldo posterior: " + FormatoSalida.moneda(t.getSaldoPosterior()));
            System.out.println("Fecha: " + FormatoSalida.fecha(t.getFecha()));
        }
    }

    private void desactivarCuenta() {

        String numeroCuenta = ConsolaBanco.textoObligatorio(sc, "Numero de cuenta a desactivar: ");

        System.out.println(cuentaController.desactivarCuenta(numeroCuenta));
    }

}
