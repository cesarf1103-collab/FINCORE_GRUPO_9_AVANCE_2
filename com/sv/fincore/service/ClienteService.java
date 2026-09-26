package com.sv.fincore.service;

import com.sv.fincore.dao.ClienteDAO;
import java.io.IOException;
import com.sv.fincore.model.Cliente;
import java.util.ArrayList;
import java.util.List;

public class ClienteService {
    private final ClienteDAO clienteDAO;
    private List<Cliente> listaClientes = new ArrayList<>();

    public ClienteService() {
    try {
        clienteDAO = new ClienteDAO();
        listaClientes = clienteDAO.listar();
    } catch (IOException | ClassNotFoundException e) {
        throw new IllegalStateException("No se pudieron cargar los clientes", e);
    }
}

    public String registrarCliente(Cliente nuevoCliente) {
        for (Cliente c : listaClientes) {
            if (c.getDui().equals(nuevoCliente.getDui())) {
                return "DUI ya registrado";
            }
        }
        listaClientes.add(nuevoCliente);
        try {
    clienteDAO.guardar(nuevoCliente);
} catch (IOException e) {
    listaClientes.remove(nuevoCliente);
    return "No se pudo guardar el cliente";
}
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

public String modificarCliente(String dui, String nuevoNombre, String nuevosApellidos,
                               String nuevoCorreo, String nuevoTelefono, String nuevaDireccion) {
    Cliente cliente = buscarPorDui(dui);
    if (cliente == null) {
        return "Cliente no encontrado";
    }

    String nombreAnterior = cliente.getNombre();
    String apellidosAnteriores = cliente.getApellidos();
    String correoAnterior = cliente.getCorreo();
    String telefonoAnterior = cliente.getTelefono();
    String direccionAnterior = cliente.getDireccion();

    cliente.setNombre(nuevoNombre);
    cliente.setApellidos(nuevosApellidos);
    cliente.setCorreo(nuevoCorreo);
    cliente.setTelefono(nuevoTelefono);
    cliente.setDireccion(nuevaDireccion);

    try {
        clienteDAO.guardar(cliente);
        return "Cliente actualizado";
    } catch (IOException e) {
        cliente.setNombre(nombreAnterior);
        cliente.setApellidos(apellidosAnteriores);
        cliente.setCorreo(correoAnterior);
        cliente.setTelefono(telefonoAnterior);
        cliente.setDireccion(direccionAnterior);
        return "No se pudo guardar la actualización del cliente";
    }
}

    public void guardarCliente(Cliente cliente) throws IOException {
    clienteDAO.guardar(cliente);
}
    public String eliminarCliente(String dui) {
        Cliente cliente = buscarPorDui(dui);
        if (cliente != null) {
            try {
    clienteDAO.eliminar(cliente.getId());
    listaClientes.remove(cliente);
} catch (IOException e) {
    return "No se pudo eliminar el cliente";
}
            return "Cliente eliminado";
        }
        return "Cliente no encontrado";
    }
}
