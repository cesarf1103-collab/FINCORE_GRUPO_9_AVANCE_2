package com.sv.fincore.controller;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.CuentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    @Mock CuentaService cuentaService;
    @InjectMocks CuentaController controller;

    @Test
    void crearCuenta_delega() {
        when(cuentaService.crearCuenta("001", "d", "Ahorro")).thenReturn("Cuenta creada exitosamente");

        assertEquals("Cuenta creada exitosamente", controller.crearCuenta("001", "d", "Ahorro"));
    }

    @Test
    void listarYBuscar_delegan() {
        List<Cuenta> lista = List.of(mock(Cuenta.class));
        Cuenta c = mock(Cuenta.class);
        when(cuentaService.listarCuentas()).thenReturn(lista);
        when(cuentaService.buscarCuenta("001")).thenReturn(c);

        assertSame(lista, controller.listarCuentas());
        assertSame(c, controller.buscarCuenta("001"));
    }

    @Test
    void historialYDesactivar_delegan() {
        List<Transaccion> h = List.of();
        when(cuentaService.obtenerHistorial("001")).thenReturn(h);
        when(cuentaService.desactivarCuenta("001")).thenReturn("Cuenta desactivada");

        assertSame(h, controller.obtenerHistorial("001"));
        assertEquals("Cuenta desactivada", controller.desactivarCuenta("001"));
    }
}
