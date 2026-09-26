package com.sv.fincore.service;

import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.model.Cliente;
import java.io.IOException;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CuentaService {
    
    private final CuentaDAO cuentaDAO;
    private List<Cuenta> listaCuentas = new ArrayList<>();
    private ClienteService clienteService;

public CuentaService(ClienteService clienteService) {
    this.clienteService = clienteService;
    try {
        this.cuentaDAO = new CuentaDAO();
        this.listaCuentas = cuentaDAO.listar();
    } catch (IOException | ClassNotFoundException e) {
        throw new IllegalStateException("No se pudieron cargar las cuentas", e);
    }
}

    public String crearCuenta(String numeroCuenta, String duiCliente, String tipoCuenta) {

        if (buscarCuenta(numeroCuenta) != null) {
            return "Cuenta ya registrada";
        }

        Cliente cliente = clienteService.buscarPorDui(duiCliente);

        if (cliente == null) {
            return "Cliente no encontrado";
        }

        Cuenta cuenta = new Cuenta(numeroCuenta, duiCliente, tipoCuenta);

    try {
        cuentaDAO.guardar(cuenta);
        listaCuentas.add(cuenta);
        cliente.agregarCuenta(numeroCuenta);
        clienteService.guardarCliente(cliente);
    return "Cuenta creada exitosamente";
} catch (IOException e) {
        cliente.getNumerosCuenta().remove(numeroCuenta);
        listaCuentas.remove(cuenta);
    try {
        cuentaDAO.eliminar(numeroCuenta);
    } catch (IOException errorAlRevertir) {
        return "No se pudo completar ni revertir la creación de la cuenta";
    }
    return "No se pudo guardar la cuenta";
}
    }

    public List<Cuenta> listarCuentas() {
        return listaCuentas;
    }

    public Cuenta buscarCuenta(String numeroCuenta) {

        for (Cuenta cuenta : listaCuentas) {
            if (cuenta.getNumeroCuenta().equals(numeroCuenta)) {
                return cuenta;
            }
        }

        return null;
    }

    public String depositar(String numeroCuenta, BigDecimal monto) {

        Cuenta cuenta = buscarCuenta(numeroCuenta);

        if (cuenta == null) {
            return "Cuenta no encontrada";
        }

        if (!cuenta.isActiva()) {
            return "Cuenta inactiva";
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            return "Monto invalido";
        }

        cuenta.setSaldo(cuenta.getSaldo().add(monto));

        Transaccion transaccion = new Transaccion(
                UUID.randomUUID().toString(),
                numeroCuenta,
                "DEPOSITO",
                monto,
                cuenta.getSaldo(),
                "Deposito realizado"
        );

        cuenta.agregarTransaccion(transaccion);
try {
    cuentaDAO.guardar(cuenta);
} catch (IOException e) {
    cuenta.getHistorial().remove(transaccion);
    cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
    return "No se pudo guardar el deposito";
}

        return "Deposito exitoso";
    }

    public String retirar(String numeroCuenta, BigDecimal monto) {

        Cuenta cuenta = buscarCuenta(numeroCuenta);

        if (cuenta == null) {
            return "Cuenta no encontrada";
        }

        if (!cuenta.isActiva()) {
            return "Cuenta inactiva";
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            return "Monto invalido";
        }

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            return "Saldo insuficiente";
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));

        Transaccion transaccion = new Transaccion(
                UUID.randomUUID().toString(),
                numeroCuenta,
                "RETIRO",
                monto,
                cuenta.getSaldo(),
                "Retiro realizado"
        );

        cuenta.agregarTransaccion(transaccion);
try {
    cuentaDAO.guardar(cuenta);
} catch (IOException e) {
    cuenta.getHistorial().remove(transaccion);
    cuenta.setSaldo(cuenta.getSaldo().add(monto));
    return "No se pudo guardar el retiro";
}

        return "Retiro exitoso";
    }

    public String transferir(String cuentaOrigen,
                             String cuentaDestino,
                             BigDecimal monto) {

        Cuenta origen = buscarCuenta(cuentaOrigen);
        Cuenta destino = buscarCuenta(cuentaDestino);

        if (origen == null || destino == null) {
            return "Cuenta no encontrada";
        }

        if (cuentaOrigen.equals(cuentaDestino)) {
            return "Las cuentas deben ser diferentes";
        }
        
        if (!origen.isActiva() || !destino.isActiva()) {
            return "Cuenta inactiva";
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            return "Monto invalido";
        }

        if (origen.getSaldo().compareTo(monto) < 0) {
            return "Saldo insuficiente";
        }

        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        Transaccion salida = new Transaccion(
                UUID.randomUUID().toString(),
                cuentaOrigen,
                "TRANSFERENCIA",
                monto,
                origen.getSaldo(),
                "Transferencia enviada",
                cuentaOrigen,
                cuentaDestino
        );

        Transaccion entrada = new Transaccion(
                UUID.randomUUID().toString(),
                cuentaDestino,
                "TRANSFERENCIA",
                monto,
                destino.getSaldo(),
                "Transferencia recibida",
                cuentaOrigen,
                cuentaDestino
        );

        origen.agregarTransaccion(salida);
        destino.agregarTransaccion(entrada);

        try {
    cuentaDAO.guardar(origen);
    return "Transferencia exitosa";
} catch (IOException e) {
    origen.getHistorial().remove(salida);
    destino.getHistorial().remove(entrada);
    origen.setSaldo(origen.getSaldo().add(monto));
    destino.setSaldo(destino.getSaldo().subtract(monto));
    return "No se pudo guardar la transferencia";
}
    }

    public List<Transaccion> obtenerHistorial(String numeroCuenta) {

        Cuenta cuenta = buscarCuenta(numeroCuenta);

        if (cuenta != null) {
            return cuenta.getHistorial();
        }

        return new ArrayList<>();
    }

    public String desactivarCuenta(String numeroCuenta) {

        Cuenta cuenta = buscarCuenta(numeroCuenta);

        if (cuenta == null) {
            return "Cuenta no encontrada";
        }

        cuenta.setActiva(false);
    try {
    cuentaDAO.guardar(cuenta);
    return "Cuenta desactivada";
    } catch (IOException e) {
    cuenta.setActiva(true);
    return "No se pudo guardar la desactivación";
}
    }
}
