
package com.sv.fincore.main;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.service.ProcesadorReportesTask;
import com.sv.fincore.view.MenuPrincipal;
import com.sv.fincore.util.DataSourceConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String ARGUMENTO_SIMULAR_INTERESES =
            "--simular-intereses";
    private static final int TIEMPO_CIERRE_SEGUNDOS = 30;

    public static void main(String[] args) {
        ClienteService clienteService = new ClienteService();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CuentaService cuentaService = new CuentaService(clienteService, executor);

        ClienteController clienteController =
                new ClienteController(clienteService);
        CuentaController cuentaController =
                new CuentaController(cuentaService);
        TransaccionController transaccionController =
                new TransaccionController(cuentaService);

        if (solicitoSimulacion(args)) {
            executor.submit(() -> {
                try {
                    new ProcesadorReportesTask(cuentaService).run();
                } catch (RuntimeException e) {
                    System.err.println(
                            "Falló la simulación de intereses: "
                                    + e.getMessage()
                    );
                }
            });
        }

        MenuPrincipal menuPrincipal = new MenuPrincipal(
                clienteController,
                cuentaController,
                transaccionController
        );

        try {
            menuPrincipal.iniciar();
        } finally {
            cerrarExecutor(executor);
            cuentaService.close();
            try {
                DataSourceConfig.cerrar();
            } catch (java.sql.SQLException e) {
                System.err.println("No se pudo cerrar el pool de conexiones: " + e.getMessage());
            }
        }
    }

    private static boolean solicitoSimulacion(String[] args) {
        return args != null
                && args.length == 1
                && ARGUMENTO_SIMULAR_INTERESES.equals(args[0]);
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
