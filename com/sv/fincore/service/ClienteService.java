package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import java.util.ArrayList;
import java.util.List;

public class ClienteService {
    private List<Cliente> listaClientes = new ArrayList<>();

    public String registrarCliente(Cliente nuevoCliente) {
        for (Cliente c : listaClientes) {
            if (c.getDui().equals(nuevoCliente.getDui())) {
                return "DUI ya registrado";
            }
        }
        listaClientes.add(nuevoCliente);
        return "Cliente registrado exitosamente";
    }

    public List<Cliente> listarClientes() {
        return listaClientes;
    }

    public Cliente buscarPorDui(String dui) {
        for (Cliente c : listaClientes) {
            if (c.getDui().equals(dui)) {
                return c;
            }
        }
        return null;
    }

    public Cliente buscarPorNumeroCuenta(String numeroCuenta) {
        for (Cliente c : listaClientes) {
            if (c.getNumerosCuenta().contains(numeroCuenta)) {
                return c;
            }
        }
        return null;
    }

    public String modificarCliente(String dui, String nuevoNombre, String nuevosApellidos, String nuevoCorreo, String nuevoTelefono, String nuevaDireccion) {
        Cliente cliente = buscarPorDui(dui);
        if (cliente != null) {
            cliente.setNombre(nuevoNombre);
            cliente.setApellidos(nuevosApellidos);
            cliente.setCorreo(nuevoCorreo);
            cliente.setTelefono(nuevoTelefono);
            cliente.setDireccion(nuevaDireccion);
            return "Cliente actualizado";
        }
        return "Cliente no encontrado";
    }

    public String eliminarCliente(String dui) {
        Cliente cliente = buscarPorDui(dui);
        if (cliente != null) {
            listaClientes.remove(cliente);
            return "Cliente eliminado";
        }
        return "Cliente no encontrado";
    }
}
