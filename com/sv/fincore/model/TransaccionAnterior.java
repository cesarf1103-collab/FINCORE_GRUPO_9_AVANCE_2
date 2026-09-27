package com.sv.fincore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TransaccionAnterior implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idTransaccion;
    private String numeroCuenta;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal saldoPosterior;
    private LocalDateTime fecha;
    private String descripcion;
    private String cuentaOrigen;
    private String cuentaDestino;

    public Transaccion convertir() {
        Transaccion nueva;

        switch (tipo) {
            case "DEPOSITO" ->
                    nueva = new Deposito(idTransaccion, numeroCuenta, monto, descripcion);
            case "RETIRO" ->
                    nueva = new Retiro(idTransaccion, numeroCuenta, monto, descripcion);
            case "TRANSFERENCIA" ->
                    nueva = Transferencia.restaurarRegistro(idTransaccion,
                            numeroCuenta, cuentaOrigen, cuentaDestino, monto, descripcion);
            default ->
                    throw new IllegalStateException(
                            "Tipo de transaccion anterior desconocido: " + tipo);
        }

        nueva.restaurar(saldoPosterior, fecha);
        return nueva;
    }
}
