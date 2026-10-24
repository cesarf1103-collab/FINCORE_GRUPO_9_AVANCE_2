package com.sv.fincore.integration;

import com.sv.fincore.model.Cliente;
import com.sv.fincore.dao.ClienteDAO;
import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import com.sv.fincore.view.MenuClientes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/** Ejecuta los 17 escenarios de docs/Matriz_de_Pruebas.md sobre H2 aislado. */
class EscenariosFuncionalesTest {
    private ClienteService clientes;
    private CuentaService cuentas;
    private Cliente cliente;
    private String origen;
    private String destino;

    @BeforeEach
    void preparar() {
        String dui = String.format("%08d-9", ThreadLocalRandom.current().nextInt(100_000_000));
        cliente = new Cliente(UUID.randomUUID().toString(), dui, "Ana", "Demo", "ana@example.invalid",
                "77778888", "San Salvador", LocalDate.of(2000, 1, 1));
        origen = UUID.randomUUID().toString();
        destino = UUID.randomUUID().toString();
        clientes = new ClienteService();
        cuentas = new CuentaService(clientes);
        assertEquals("Cliente registrado exitosamente", clientes.registrarCliente(cliente));
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(origen, dui, "Ahorro"));
        assertEquals("Cuenta creada exitosamente", cuentas.crearCuenta(destino, dui, "Corriente"));
    }

    @AfterEach
    void limpiar() throws Exception {
        cuentas.close();
        try (Connection c = ConexionDB.obtenerConexion()) {
            for (String sql : new String[] {
                    "DELETE FROM transacciones WHERE numero_cuenta IN (?, ?)",
                    "DELETE FROM cuentas WHERE numero_cuenta IN (?, ?)"}) {
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, origen);
                    p.setString(2, destino);
                    p.executeUpdate();
                }
            }
            try (PreparedStatement p = c.prepareStatement("DELETE FROM clientes WHERE id = ?")) {
                p.setString(1, cliente.getId());
                p.executeUpdate();
            }
        }
    }

    private static BigDecimal dinero(String valor) { return new BigDecimal(valor); }
    private BigDecimal saldo(String numero) { return cuentas.buscarCuenta(numero).getSaldo(); }

    @Test void tc001RegistroValido() { assertNotNull(new ClienteService().buscarPorDui(cliente.getDui())); }
    @Test void tc002DuiDuplicado() {
        Cliente repetido = new Cliente(UUID.randomUUID().toString(), cliente.getDui(), "Otra", "Persona",
                "otra@example.invalid", "77778888", "San Salvador", LocalDate.of(2000, 1, 1));
        assertEquals("DUI ya registrado", clientes.registrarCliente(repetido));
    }
    @Test void tc003DuiInvalidoEnMenu() throws Exception {
        String id = UUID.randomUUID().toString();
        String dui = String.format("%08d-9", ThreadLocalRandom.current().nextInt(100_000_000));
        try {
            new MenuClientes(new Scanner(String.join("\n", "1", id, "DUI-invalido", dui,
                    "Ana", "Demo", "ana@example.invalid", "77778888", "Ciudad",
                    "2000", "1", "1", "6") + "\n"), new ClienteController(clientes)).mostrar();
            assertNotNull(new ClienteService().buscarPorDui(dui));
        } finally {
            assertTrue(new ClienteDAO().eliminar(id));
        }
    }
    @Test void tc004CampoObligatorioEnMenu() throws Exception {
        String id = UUID.randomUUID().toString();
        String dui = String.format("%08d-9", ThreadLocalRandom.current().nextInt(100_000_000));
        try {
            new MenuClientes(new Scanner(String.join("\n", "1", " ", id, dui,
                    " ", "Ana", "Demo", "ana@example.invalid", "77778888", "Ciudad",
                    "2000", "1", "1", "6") + "\n"), new ClienteController(clientes)).mostrar();
            assertNotNull(new ClienteService().buscarPorDui(dui));
        } finally {
            assertTrue(new ClienteDAO().eliminar(id));
        }
    }
    @Test void tc005Deposito() {
        assertEquals("Deposito exitoso", cuentas.depositar(origen, dinero("20.00")));
        assertEquals(dinero("20.00"), saldo(origen));
    }
    @Test void tc006DepositoNegativo() {
        assertEquals("Monto invalido", cuentas.depositar(origen, dinero("-1.00")));
        assertEquals(dinero("0.00"), saldo(origen));
    }
    @Test void tc007Retiro() {
        cuentas.depositar(origen, dinero("20.00"));
        assertEquals("Retiro exitoso", cuentas.retirar(origen, dinero("8.00")));
        assertEquals(dinero("12.00"), saldo(origen));
    }
    @Test void tc008RetiroInsuficiente() {
        assertEquals("Saldo insuficiente", cuentas.retirar(origen, dinero("1.00")));
        assertEquals(dinero("0.00"), saldo(origen));
    }
    @Test void tc009Transferencia() {
        cuentas.depositar(origen, dinero("30.00"));
        assertEquals("Transferencia exitosa", cuentas.transferir(origen, destino, dinero("12.00")));
        assertEquals(dinero("18.00"), saldo(origen));
        assertEquals(dinero("12.00"), saldo(destino));
    }
    @Test void tc010TransferenciaCero() {
        assertEquals("Monto invalido", cuentas.transferir(origen, destino, BigDecimal.ZERO));
    }
    @Test void tc011TransferenciaInsuficiente() {
        assertEquals("Saldo insuficiente", cuentas.transferir(origen, destino, dinero("1.00")));
    }
    @Test void tc012DestinoInexistente() {
        cuentas.depositar(origen, dinero("20.00"));
        assertEquals("Cuenta no encontrada", cuentas.transferir(origen, "INEXISTENTE", dinero("5.00")));
        assertEquals(dinero("20.00"), saldo(origen));
    }
    @Test void tc013TransferenciaNegativa() {
        assertEquals("Monto invalido", cuentas.transferir(origen, destino, dinero("-1.00")));
    }
    @Test void tc014ConsultarSaldo() {
        cuentas.depositar(origen, dinero("7.50"));
        assertEquals(dinero("7.50"), new CuentaService(clientes).buscarCuenta(origen).getSaldo());
    }
    @Test void tc015Historial() {
        cuentas.depositar(origen, dinero("10.00"));
        cuentas.retirar(origen, dinero("3.00"));
        assertEquals(2, cuentas.obtenerHistorial(origen).size());
    }
    @Test void tc016DepositoTerceros() {
        assertEquals("Deposito exitoso", cuentas.depositar(destino, dinero("5.00")));
        assertEquals(dinero("5.00"), saldo(destino));
    }
    @Test void tc017RetiroVentanilla() {
        cuentas.depositar(origen, dinero("8.00"));
        assertEquals("Retiro exitoso", cuentas.retirar(origen, dinero("3.00")));
        assertEquals(dinero("5.00"), saldo(origen));
    }
}
