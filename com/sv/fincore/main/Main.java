package com.sv.fincore.main;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.service.ProcesadorReportesTask;
import com.sv.fincore.view.MenuPrincipal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int TIEMPO_CIERRE_SEGUNDOS = 30;

    public static void main(String[] args) {
        ClienteService clienteService = new ClienteService();
        CuentaService cuentaService = new CuentaService(clienteService);

        ClienteController clienteController =
                new ClienteController(clienteService);
        CuentaController cuentaController =
                new CuentaController(cuentaService);
        TransaccionController transaccionController =
                new TransaccionController(cuentaService);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        ProcesadorReportesTask simulacionIntereses =
                new ProcesadorReportesTask(cuentaService);

        MenuPrincipal menuPrincipal = new MenuPrincipal(
                clienteController,
                cuentaController,
                transaccionController,
                executor,
                simulacionIntereses
        );

        try {
            menuPrincipal.iniciar();
        } finally {
            cerrarExecutor(executor);
        }
    }

    private static void cerrarExecutor(ExecutorService executor) {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(
                    TIEMPO_CIERRE_SEGUNDOS,
                    TimeUnit.SECONDS
            )) {
                executor.shutdownNow();

                if (!executor.awaitTermination(
                        TIEMPO_CIERRE_SEGUNDOS,
                        TimeUnit.SECONDS
                )) {
                    System.err.println(
                            "El ejecutor no terminó dentro del tiempo esperado."
                    );
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println(
                    "Se interrumpió el cierre ordenado del ejecutor."
            );
        }
    }
}
