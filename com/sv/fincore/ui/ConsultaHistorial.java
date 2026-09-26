package com.sv.fincore.ui;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.CuentaService;
import java.util.List;
import java.util.Scanner;
public class ConsultaHistorial {
    private CuentaService cuentaService;
    public ConsultaHistorial(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }
    public void mostrarConsulta(Scanner sc) {
        System.out.print("Ingrese numero de cuenta: ");
        String numeroCuenta = sc.nextLine();
        Cuenta cuenta = cuentaService.buscarCuenta(numeroCuenta);
        if (cuenta == null) {
            System.out.println("Cuenta no encontrada.");
        } else {
            System.out.println("Saldo: $" + cuenta.getSaldo());
            System.out.println("Historial:");
            List<Transaccion> historial = cuentaService.obtenerHistorial(numeroCuenta);
            if (historial != null && !historial.isEmpty()) {
                for (Transaccion t : historial) {
                    System.out.println(t.getTipo() + " - $" + t.getMonto() + " (Saldo: $" + t.getSaldoPosterior() + ")");
                }
            } else {
                System.out.println("  No hay movimientos registrados.");
            }
        }
    }
}