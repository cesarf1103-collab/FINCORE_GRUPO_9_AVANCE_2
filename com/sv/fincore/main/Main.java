package com.sv.fincore.main;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ClienteService clienteService = new ClienteService();
        CuentaService cuentaService = new CuentaService(clienteService);

        int opcion;

        do {

            System.out.println("\n  FINCORE   ");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Crear cuenta");
            System.out.println("4. Listar cuentas");
            System.out.println("5. Depositar");
            System.out.println("6. Retirar");
            System.out.println("7. Transferir");
            System.out.println("8. Buscar cliente");
            System.out.println("9. Ver historial de cuenta");
            System.out.println("10. Salir");
            System.out.print("Seleccione una opcion: ");

            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {

                case 1:

                    System.out.print("ID: ");
                    String id = sc.nextLine();

                    System.out.print("DUI: ");
                    String dui = sc.nextLine();

                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();

                    System.out.print("Apellidos: ");
                    String apellidos = sc.nextLine();

                    System.out.print("Correo: ");
                    String correo = sc.nextLine();

                    System.out.print("Telefono: ");
                    String telefono = sc.nextLine();

                    System.out.print("Direccion: ");
                    String direccion = sc.nextLine();

                    System.out.print("Anio de nacimiento: ");
                    int anio = sc.nextInt();

                    System.out.print("Mes de nacimiento: ");
                    int mes = sc.nextInt();

                    System.out.print("Dia de nacimiento: ");
                    int dia = sc.nextInt();
                    sc.nextLine();

                    Cliente cliente = new Cliente(
                            id,
                            dui,
                            nombre,
                            apellidos,
                            correo,
                            telefono,
                            direccion,
                            LocalDate.of(anio, mes, dia)
                    );

                    System.out.println(
                            clienteService.registrarCliente(cliente)
                    );

                    break;

                case 2:

                    for (Cliente c : clienteService.listarClientes()) {

                        System.out.println("DUI: " + c.getDui());
                        System.out.println("Nombre: " + c.getNombre());
                        System.out.println("Apellidos: " + c.getApellidos());
                    }

                    break;

                case 3:

                    System.out.print("Numero de cuenta: ");
                    String numeroCuenta = sc.nextLine();

                    System.out.print("DUI del cliente: ");
                    String duiCliente = sc.nextLine();

                    System.out.print("Tipo de cuenta: ");
                    String tipoCuenta = sc.nextLine();

                    System.out.println(
                            cuentaService.crearCuenta(
                                    numeroCuenta,
                                    duiCliente,
                                    tipoCuenta
                            )
                    );

                    break;

                case 4:

                    for (Cuenta cuenta : cuentaService.listarCuentas()) {

                        System.out.println("Cuenta: " + cuenta.getNumeroCuenta());
                        System.out.println("DUI Cliente: " + cuenta.getDuiCliente());
                        System.out.println("Saldo: $" + cuenta.getSaldo());
                        System.out.println("Tipo: " + cuenta.getTipoCuenta());
                    }

                    break;

                case 5:

                    System.out.print("Numero de cuenta: ");
                    String cuentaDeposito = sc.nextLine();

                    System.out.print("Monto: ");
                    BigDecimal montoDeposito = sc.nextBigDecimal();
                    sc.nextLine();

                    System.out.println(
                            cuentaService.depositar(
                                    cuentaDeposito,
                                    montoDeposito
                            )
                    );

                    break;

                case 6:

                    System.out.print("Numero de cuenta: ");
                    String cuentaRetiro = sc.nextLine();

                    System.out.print("Monto: ");
                    BigDecimal montoRetiro = sc.nextBigDecimal();
                    sc.nextLine();

                    System.out.println(
                            cuentaService.retirar(
                                    cuentaRetiro,
                                    montoRetiro
                            )
                    );

                    break;

                case 7:

                    System.out.print("Cuenta origen: ");
                    String origen = sc.nextLine();

                    System.out.print("Cuenta destino: ");
                    String destino = sc.nextLine();

                    System.out.print("Monto: ");
                    BigDecimal montoTransferencia = sc.nextBigDecimal();
                    sc.nextLine();

                    System.out.println(
                            cuentaService.transferir(
                                    origen,
                                    destino,
                                    montoTransferencia
                            )
                    );

                    break;

                case 8:

                    System.out.print("Ingrese DUI: ");
                    String duiBuscar = sc.nextLine();

                    Cliente encontrado =
                            clienteService.buscarPorDui(duiBuscar);

                    if (encontrado != null) {

                        System.out.println("Nombre: " + encontrado.getNombre());
                        System.out.println("Apellidos: " + encontrado.getApellidos());
                        System.out.println("Correo: " + encontrado.getCorreo());
                        System.out.println("Telefono: " + encontrado.getTelefono());

                    } else {

                        System.out.println("Cliente no encontrado");
                    }

                    break;

                case 9:

                    System.out.print("Numero de cuenta: ");
                    String cuentaHistorial = sc.nextLine();

                    for (Transaccion t :
                            cuentaService.obtenerHistorial(cuentaHistorial)) {

                        System.out.println("Tipo: " + t.getTipo());
                        System.out.println("Monto: " + t.getMonto());
                        System.out.println("Saldo: " + t.getSaldoPosterior());
                        System.out.println("Fecha: " + t.getFecha());
                    }

                    break;

                case 10:

                    System.out.println("Gracias por usar FinCore");
                    break;

                default:

                    System.out.println("Opcion invalida");
            }

        } while (opcion != 10);

        sc.close();
    }
}
