package com.sv.fincore.service;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ReporteBancario implements Runnable {
    private final List<String> lineas;
    private final Path destino;

    public ReporteBancario(List<Cuenta> cuentas, Path destino) {
        this.destino = destino;
        this.lineas = new ArrayList<>();
        lineas.add("REPORTE BANCARIO FINCORE");

        for (Cuenta cuenta : cuentas) {
            lineas.add("Cuenta: " + cuenta.getNumeroCuenta()
                    + " | DUI: " + cuenta.getDuiCliente()
                    + " | Saldo: " + cuenta.getSaldo()
                    + " | Estado: " + (cuenta.isActiva() ? "Activa" : "Inactiva"));

            for (Transaccion transaccion : cuenta.getHistorial()) {
                lineas.add("  " + transaccion.getFecha()
                        + " | " + transaccion.getTipo()
                        + " | Monto: " + transaccion.getMonto()
                        + " | Saldo posterior: " + transaccion.getSaldoPosterior());
            }
        }
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, lineas, StandardCharsets.UTF_8);
            System.out.println("Reporte generado: " + destino);
        } catch (IOException e) {
            System.err.println("No se pudo generar el reporte: " + e.getMessage());
        }
    }
}
