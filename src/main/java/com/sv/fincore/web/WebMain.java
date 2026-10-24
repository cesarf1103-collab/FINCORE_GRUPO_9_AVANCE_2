package com.sv.fincore.web;

import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.DataSourceConfig;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

/** Inicia la interfaz web solo en la computadora local. */
public final class WebMain {
    private WebMain() {}

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("fincore.web.port", "8765"));
        ClienteService clientes = new ClienteService();
        CuentaService cuentas = new CuentaService(clientes);
        try (FinCoreHttpServer web = new FinCoreHttpServer(port, clientes, cuentas)) {
            web.start();
            String url = "http://127.0.0.1:" + web.port() + "/";
            System.out.println("FinCore web disponible en " + url + " — pulse Ctrl+C para cerrar.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                web.close();
                try { DataSourceConfig.cerrar(); } catch (Exception e) { System.err.println(e.getMessage()); }
            }, "FinCore-shutdown"));
            if (args.length > 0 && "--open-browser".equals(args[0])) {
                try {
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI.create(url));
                    else System.out.println("Abra manualmente: " + url);
                } catch (Exception e) { System.out.println("Abra manualmente: " + url); }
            }
            new CountDownLatch(1).await();
        }
    }
}
