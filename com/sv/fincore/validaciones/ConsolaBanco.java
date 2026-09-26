package com.sv.fincore.validaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsolaBanco {

    static class Cliente {
        private String dui;
        private String nombre;
        private double saldo;

        public Cliente(String dui, String nombre, double saldoInicial) {
            this.dui = dui;
            this.nombre = nombre;
            this.saldo = saldoInicial;
        }

        public String getDui() { return dui; }
        public String getNombre() { return nombre; }
        public double getSaldo() { return saldo; }

        public void depositar(double monto) {
            this.saldo += monto;
        }

        public boolean retirar(double monto) {
            if (monto <= this.saldo) {
                this.saldo -= monto;
                return true;
            }
            return false;
        }
    }

    private static List<Cliente> clientes = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion = 0;
        do {
            mostrarMenu();
            System.out.print("Seleccione una opción: ");
            
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                scanner.nextLine();
                procesarOpcion(opcion);
            } else {
                System.out.println(" Error: Ingrese un número válido.");
                scanner.nextLine();
            }
        } while (opcion != 5);
    }

    private static void mostrarMenu() {
        System.out.println("\n==============================================");
        System.out.println("                  FINCORE                  ");
        System.out.println("==============================================");
        System.out.println("1.  Registrar Cliente");
        System.out.println("2.  Realizar Depósito");
        System.out.println("3.  Realizar Retiro");
        System.out.println("4.  Consultar Clientes y Saldos");
        System.out.println("5.  Salir");
        System.out.println("----------------------------------------------");
    }

    private static void procesarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> registrarCliente();
            case 2 -> realizarDeposito();
            case 3 -> realizarRetiro();
            case 4 -> listarClientes();
            case 5 -> {}
            default -> System.out.println("\n Opción no válida.");
        }
    }

    private static void registrarCliente() {
        System.out.println("\n--- REGISTRO DE CLIENTE ---");

        String dui = "";
        while (true) {
            System.out.print("Ingrese DUI del cliente: ");
            dui = scanner.nextLine().trim();

            if (dui.isEmpty()) {
                System.out.println(" El DUI es obligatorio.");
            } else if (dui.length() < 9) {
                System.out.println(" DUI inválido.");
            } else {
                break;
            }
        }

        if (buscarClientePorDui(dui) != null) {
            System.out.println(" Ya existe un cliente con ese DUI.");
            return;
        }

        String nombre = "";
        while (true) {
            System.out.print("Ingrese Nombre completo: ");
            nombre = scanner.nextLine().trim();

            if (nombre.isEmpty()) {
                System.out.println(" El nombre no puede estar vacío.");
            } else if (!nombre.contains(" ")) {
                System.out.println(" Debe ingresar al menos un nombre y un apellido.");
            } else {
                break;
            }
        }

        double saldoInicial = 0;
        while (true) {
            System.out.print("Ingrese Saldo Inicial ($): ");
            if (scanner.hasNextDouble()) {
                saldoInicial = scanner.nextDouble();
                scanner.nextLine();

                if (saldoInicial > 0) {
                    break;
                } else {
                    System.out.println(" El monto debe ser mayor a 0.");
                }
            } else {
                System.out.println(" Debe ingresar un número válido.");
                scanner.nextLine();
            }
        }

        clientes.add(new Cliente(dui, nombre, saldoInicial));
        System.out.println(" Cliente registrado exitosamente.");
    }

    private static void realizarDeposito() {
        System.out.println("\n--- DEPÓSITO BANCARIO ---");
        Cliente cliente = buscarClientePorDui(pedirDui());

        if (cliente != null) {
            double monto = pedirMontoPositivo("Monto a depositar ($): ");
            cliente.depositar(monto);
            System.out.printf(" Depósito exitoso. Nuevo Saldo: $%.2f%n", cliente.getSaldo());
        } else {
            System.out.println(" Cliente no encontrado.");
        }
    }

    private static void realizarRetiro() {
        System.out.println("\n--- RETIRO BANCARIO ---");
        Cliente cliente = buscarClientePorDui(pedirDui());

        if (cliente != null) {
            double monto = pedirMontoPositivo("Monto a retirar ($): ");
            if (cliente.retirar(monto)) {
                System.out.printf(" Retiro exitoso. Nuevo Saldo: $%.2f%n", cliente.getSaldo());
            } else {
                System.out.println(" FONDOS INSUFICIENTES");
            }
        } else {
            System.out.println(" Cliente no encontrado.");
        }
    }

    private static void listarClientes() {
        System.out.println("\n--- ESTADO DE CUENTAS ---");
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Cliente c : clientes) {
            System.out.printf("DUI: %s | Nombre: %s | Saldo: $%.2f%n", c.getDui(), c.getNombre(), c.getSaldo());
        }
    }

    private static String pedirDui() {
        System.out.print("Ingrese DUI del cliente: ");
        return scanner.nextLine().trim();
    }

    private static double pedirMontoPositivo(String mensaje) {
        double monto = 0;
        while (true) {
            System.out.print(mensaje);
            if (scanner.hasNextDouble()) {
                monto = scanner.nextDouble();
                scanner.nextLine();

                if (monto > 0) {
                    return monto;
                } else {
                    System.out.println(" El monto debe ser mayor a 0.");
                }
            } else {
                System.out.println(" Ingrese un número válido.");
                scanner.nextLine();
            }
        }
    }

    private static Cliente buscarClientePorDui(String dui) {
        for (Cliente c : clientes) {
            if (c.getDui().equalsIgnoreCase(dui)) {
                return c;
            }
        }
        return null;
    }
}