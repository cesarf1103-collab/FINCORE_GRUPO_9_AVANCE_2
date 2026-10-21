package com.sv.fincore.main;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.view.MenuPrincipal;

public class Main {

    public static void main(String[] args) {

        ClienteService clienteService = new ClienteService();
        CuentaService cuentaService = new CuentaService(clienteService);

        ClienteController clienteController = new ClienteController(clienteService);
        CuentaController cuentaController = new CuentaController(cuentaService);
        TransaccionController transaccionController = new TransaccionController(cuentaService);

        MenuPrincipal menuPrincipal =
                new MenuPrincipal(clienteController, cuentaController, transaccionController);
        menuPrincipal.iniciar();
    }
}