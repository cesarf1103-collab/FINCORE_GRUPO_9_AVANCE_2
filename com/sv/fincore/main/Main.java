

package com.sv.fincore.main;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.view.MenuPrincipal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sv.fincore.service.ProcesadorReportesTask;


public class Main {

    public static void main(String[] args) {

        ClienteService clienteService = new ClienteService();
        CuentaService cuentaService = new CuentaService(clienteService);
        ClienteController clienteController = new ClienteController(clienteService);

        MenuPrincipal menuPrincipal = new MenuPrincipal(clienteController, cuentaService);
        menuPrincipal.iniciar();
        ExecutorService executor = Executors.newFixedThreadPool(2);

// Tarea 1: cálculo de intereses
        executor.submit(new ProcesadorReportesTask(cuentaService));

// Tarea 2: generación de reporte en segundo plano
        executor.submit(() -> {
            System.out.println(cuentaService.generarReporteEnSegundoPlano());
        });

        executor.shutdown();

    }
}