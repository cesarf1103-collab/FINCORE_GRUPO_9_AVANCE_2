package com.sv.fincore.service;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CuentaService {

    private List<Cuenta> listaCuentas = new ArrayList<>();
    private ClienteService clienteService;

    public CuentaService(ClienteService clienteService) {
        this.clienteService = clienteService;
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

        listaCuentas.add(cuenta);
        cliente.agregarCuenta(numeroCuenta);

        return "Cuenta creada exitosamente";
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

        return "Transferencia exitosa";
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

        return "Cuenta desactivada";
    }
}
