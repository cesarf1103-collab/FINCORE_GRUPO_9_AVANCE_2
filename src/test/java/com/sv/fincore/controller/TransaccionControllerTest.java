package com.sv.fincore.controller;

import com.sv.fincore.service.CuentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionControllerTest {

    @Mock CuentaService cuentaService;
    @InjectMocks TransaccionController controller;

    @Test
    void depositar_delegaAlServicio() {
        when(cuentaService.depositar("001", BigDecimal.TEN)).thenReturn("Deposito exitoso");

        assertEquals("Deposito exitoso", controller.depositar("001", BigDecimal.TEN));
        verify(cuentaService).depositar("001", BigDecimal.TEN);
    }

    @Test
    void retirar_delegaAlServicio() {
        when(cuentaService.retirar("001", BigDecimal.ONE)).thenReturn("Retiro exitoso");

        assertEquals("Retiro exitoso", controller.retirar("001", BigDecimal.ONE));
    }

    @Test
    void transferir_delegaAlServicio() {
        when(cuentaService.transferir("001", "002", BigDecimal.TEN)).thenReturn("Transferencia exitosa");

        assertEquals("Transferencia exitosa", controller.transferir("001", "002", BigDecimal.TEN));
    }
}
