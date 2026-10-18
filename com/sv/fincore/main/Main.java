

package com.sv.fincore.main;

import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.view.MenuPrincipal;

public class Main {

    public static void main(String[] args) {

        ClienteService clienteService = new ClienteService();
        CuentaService cuentaService = new CuentaService(clienteService);
        ClienteController clienteController = new ClienteController(clienteService);

        MenuPrincipal menuPrincipal = new MenuPrincipal(clienteController, cuentaService);
        menuPrincipal.iniciar();
    }
}