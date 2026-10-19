package com.sv.fincore.service;

import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.exception.CuentaNoEncontradaException;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.exception.SaldoInsuficienteException;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class CuentaService {

    private final CuentaDAO cuentaDAO;

    public CuentaService(CuentaDAO cuentaDAO) {
        this.cuentaDAO = cuentaDAO;
    }

    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a cero.");
        }

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                Cuenta origen = cuentaDAO.buscarPorNumero(conn, cuentaOrigen);
                Cuenta destino = cuentaDAO.buscarPorNumero(conn, cuentaDestino);

                if (origen == null) {
                    throw new CuentaNoEncontradaException("La cuenta de origen no existe: " + cuentaOrigen);
                }
                if (destino == null) {
                    throw new CuentaNoEncontradaException("La cuenta de destino no existe: " + cuentaDestino);
                }
                if (origen.getSaldo().compareTo(monto) < 0) {
                    throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta origen.");
                }


                BigDecimal nuevoSaldoOrigen = origen.getSaldo().subtract(monto);
                BigDecimal nuevoSaldoDestino = destino.getSaldo().add(monto);


                cuentaDAO.actualizarSaldo(conn, cuentaOrigen, nuevoSaldoOrigen);
                cuentaDAO.actualizarSaldo(conn, cuentaDestino, nuevoSaldoDestino);

                conn.commit();

            } catch (Exception e) {

                conn.rollback();
                if (e instanceof CuentaNoEncontradaException || e instanceof SaldoInsuficienteException) {
                    throw e; // Re-lanzar excepciones de negocio
                }
                throw new DatabaseConnectionException("Error durante la transferencia. Se realizo rollback.", e);
            }

        } catch (SQLException e) {
            throw new DatabaseConnectionException("Error al establecer la conexion con la base de datos.", e);
        }
    }
}