package com.sv.fincore.service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Tarea de fondo para generar un reporte o simular intereses. */
public final class ProcesadorReportesTask implements Runnable {
    private final CuentaService cuentaService;
    private final Path destino;
    private final CompletableFuture<Path> resultado;

    public ProcesadorReportesTask(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
        this.destino = null;
        this.resultado = null;
    }

    public ProcesadorReportesTask(CuentaService cuentaService, Path destino,
                                  CompletableFuture<Path> resultado) {
        this.cuentaService = cuentaService;
        this.destino = destino;
        this.resultado = resultado;
    }

    @Override
    public void run() {
        if (resultado == null) {
            cuentaService.calcularIntereses();
            return;
        }
        try {
            resultado.complete(cuentaService.generarReporte(destino));
        } catch (Exception e) {
            resultado.completeExceptionally(e);
        }
    }
}
