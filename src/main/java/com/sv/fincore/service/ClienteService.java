package com.sv.fincore.service;

import com.sv.fincore.dao.ClienteDAO;
import com.sv.fincore.dao.ClienteRepository;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.model.Cliente;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ClienteService {
    private final ClienteRepository clienteDAO;

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(ClienteRepository clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public synchronized String registrarCliente(Cliente nuevoCliente) {
        if (!datosValidos(nuevoCliente)) return "Datos de cliente invalidos";
        try {
            if (clienteDAO.buscarPorDui(nuevoCliente.getDui()) != null) return "DUI ya registrado";
            clienteDAO.guardar(nuevoCliente);
            return "Cliente registrado exitosamente";
        } catch (SQLException e) {
            // La restriccion UNIQUE tambien protege dos registros concurrentes.
            if ("23505".equals(e.getSQLState())) return "DUI ya registrado";
            throw new DatabaseConnectionException("No se pudo guardar el cliente.", e);
        }
    }

    public synchronized List<Cliente> listarClientes() {
        try {
            return clienteDAO.listar();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudieron consultar los clientes.", e);
        }
    }

    public synchronized Cliente buscarPorDui(String dui) {
        if (dui == null) return null;
        try {
            return clienteDAO.buscarPorDui(dui);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo buscar el cliente.", e);
        }
    }

    public synchronized Cliente buscarPorNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null) return null;
        for (Cliente c : listarClientes())
            if (c.getNumerosCuenta().contains(numeroCuenta)) return c;
        return null;
    }

    public synchronized String modificarCliente(String dui, String nombre, String apellidos,
                                                 String correo, String telefono, String direccion) {
        if (!contactoValido(nombre, apellidos, correo, telefono, direccion))
            return "Datos de cliente invalidos";
        Cliente cliente = buscarPorDui(dui);
        if (cliente == null) return "Cliente no encontrado";
        cliente.setNombre(nombre);
        cliente.setApellidos(apellidos);
        cliente.setCorreo(correo);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        try {
            return clienteDAO.actualizar(cliente) ? "Cliente actualizado" : "Cliente no encontrado";
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo actualizar el cliente.", e);
        }
    }

    public void guardarCliente(Cliente cliente) throws SQLException {
        if (!datosValidos(cliente)) throw new IllegalArgumentException("Datos de cliente invalidos");
        clienteDAO.guardar(cliente);
    }
    public synchronized String eliminarCliente(String dui) {
        Cliente cliente = buscarPorDui(dui);
        if (cliente == null) return "Cliente no encontrado";
        if (!cliente.getNumerosCuenta().isEmpty())
            return "No se puede eliminar un cliente con cuentas asociadas";
        try {
            return clienteDAO.eliminar(cliente.getId()) ? "Cliente eliminado" : "Cliente no encontrado";
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo eliminar el cliente.", e);
        }
    }

    private static boolean datosValidos(Cliente cliente) {
        return cliente != null && texto(cliente.getId())
                && cliente.getDui() != null && cliente.getDui().matches("\\d{8}-\\d")
                && contactoValido(cliente.getNombre(), cliente.getApellidos(), cliente.getCorreo(),
                        cliente.getTelefono(), cliente.getDireccion())
                && cliente.getFechaNacimiento() != null
                && !cliente.getFechaNacimiento().isAfter(LocalDate.now());
    }

    private static boolean contactoValido(String nombre, String apellidos, String correo,
                                           String telefono, String direccion) {
        return texto(nombre) && texto(apellidos) && texto(direccion)
                && correo != null && correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
                && telefono != null && telefono.matches("\\d{8}");
    }

    private static boolean texto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
