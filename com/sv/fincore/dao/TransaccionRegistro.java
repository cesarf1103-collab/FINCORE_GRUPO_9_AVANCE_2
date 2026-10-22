package com.sv.fincore.dao;

import java.math.BigDecimal;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;

/** Immutable SQL projection of a persisted transaction. */
public record TransaccionRegistro(String idTransaccion, String numeroCuenta, String tipo,
                                  BigDecimal monto, BigDecimal saldoPosterior, String descripcion) {
    public Transaccion aModelo() {
        return new MovimientoHistorico(idTransaccion, numeroCuenta, tipo, monto, saldoPosterior, descripcion);
    }

    private static final class MovimientoHistorico extends Transaccion {
        private MovimientoHistorico(String id, String cuenta, String tipo, BigDecimal monto,
                                    BigDecimal saldo, String descripcion) {
            super(id, cuenta, tipo, monto, saldo, descripcion);
        }

        @Override
        public void ejecutar(Cuenta origen, Cuenta destino) {
            throw new UnsupportedOperationException("El historial bancario es inmutable");
        }
    }
}
