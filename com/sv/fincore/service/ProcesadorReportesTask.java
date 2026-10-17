package com.sv.fincore.service;


public class ProcesadorReportesTask implements Runnable {
    private CuentaService cuentaService;

    public ProcesadorReportesTask(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Override
    public void run() {
        System.out.println("Iniciando generación de intereses...");
        cuentaService.calcularIntereses();
        System.out.println("Finalizó la tarea concurrente.");
    }
}

