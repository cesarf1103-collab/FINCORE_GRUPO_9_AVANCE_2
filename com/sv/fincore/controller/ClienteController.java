package com.sv.fincore.controller;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.ClienteService;
import java.util.List;

public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public String registrarCliente(Cliente cliente) {
        return clienteService.registrarCliente(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteService.listarClientes();
    }

    public Cliente buscarPorDui(String dui) {
        return clienteService.buscarPorDui(dui);
    }

    public Cliente buscarPorNumeroCuenta(String numeroCuenta) {
        return clienteService.buscarPorNumeroCuenta(numeroCuenta);
    }

    public String modificarCliente(
            String dui,
            String nombre,
            String apellidos,
            String correo,
            String telefono,
            String direccion) {

        return clienteService.modificarCliente(
                dui,
                nombre,
                apellidos,
                correo,
                telefono,
                direccion
        );
    }

    public String eliminarCliente(String dui) {
        return clienteService.eliminarCliente(dui);
    }
}
