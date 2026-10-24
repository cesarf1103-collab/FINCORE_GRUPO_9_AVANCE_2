package com.sv.fincore.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;

/** Datos de una transacción recuperada de la base de datos. */
public record TransaccionRegistro(String idTransaccion, String numeroCuenta, String tipo,
                                  BigDecimal monto, BigDecimal saldoPosterior, String descripcion,
                                  LocalDateTime fecha, String cuentaOrigen, String cuentaDestino) {
    public Transaccion aModelo() {
        return new MovimientoHistorico(idTransaccion, numeroCuenta, tipo, monto, saldoPosterior,
                descripcion, fecha, cuentaOrigen, cuentaDestino);
    }

    private static final class MovimientoHistorico extends Transaccion {
        private MovimientoHistorico(String id, String cuenta, String tipo, BigDecimal monto,
                                    BigDecimal saldo, String descripcion, LocalDateTime fecha,
                                    String origen, String destino) {
            super(id, cuenta, tipo, monto, saldo, descripcion, origen, destino);
            restaurar(saldo, fecha);
        }

        @Override
        public void ejecutar(Cuenta origen, Cuenta destino) {
            throw new UnsupportedOperationException("El historial bancario es inmutable");
        }
    }
}
