package com.sv.fincore.integration;

import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.dao.CuentaRepository;
import com.sv.fincore.dao.CrudRepository;
import com.sv.fincore.dao.ClienteDAO;
import com.sv.fincore.dao.DeletableRepository;
import com.sv.fincore.dao.AppendOnlyRepository;
import com.sv.fincore.dao.TransaccionDAO;
import com.sv.fincore.exception.CuentaNoEncontradaException;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.exception.SaldoInsuficienteException;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import modulo.empleado.Cajero;
import modulo.persona.Persona;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Isolated H2-backed integration checks; no real bank database is used. */
class CuentaServiceAtomicityTest {
    private static CuentaService service;
    private static CuentaDAO cuentaDAO;

    @BeforeAll
    static void createSchema() throws Exception {
        cuentaDAO = new CuentaDAO();
        service = new CuentaService(cuentaDAO, new TransaccionDAO(), null);
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS cuentas (numero_cuenta VARCHAR(30) PRIMARY KEY, dui_cliente VARCHAR(20), saldo DECIMAL(18,2) NOT NULL, tipo_cuenta VARCHAR(20), activa BOOLEAN NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS transacciones (id_transaccion VARCHAR(60) PRIMARY KEY, numero_cuenta VARCHAR(30) NOT NULL, tipo VARCHAR(30) NOT NULL, monto DECIMAL(18,2) NOT NULL, saldo_posterior DECIMAL(18,2), descripcion VARCHAR(200))");
            s.execute("CREATE TABLE IF NOT EXISTS clientes (id VARCHAR(30) PRIMARY KEY, dui VARCHAR(20), nombre VARCHAR(80), apellidos VARCHAR(80), correo VARCHAR(120), telefono VARCHAR(30), direccion VARCHAR(160), fecha_nacimiento DATE)");
        }
    }

    @BeforeEach
    void resetData() throws Exception {
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM transacciones");
            s.executeUpdate("DELETE FROM cuentas");
            s.executeUpdate("DELETE FROM clientes");
            s.execute("ALTER TABLE cuentas DROP CONSTRAINT IF EXISTS LIMITE_CREDITO");
            s.execute("ALTER TABLE transacciones DROP CONSTRAINT IF EXISTS BLOQUEAR_HISTORIAL_DESTINO");
            s.execute("ALTER TABLE transacciones DROP CONSTRAINT IF EXISTS BLOQUEAR_HISTORIAL_ORIGEN");
        }
    }

    @Test
    void successfulTransferDebitsAndCreditsAndPersistsHistory() throws Exception {
        account("O", "100.00"); account("D", "20.00");
        assertEquals("Transferencia exitosa", service.transferir("O", "D", bd("35.00")));
        assertEquals(bd("65.00"), balance("O"));
        assertEquals(bd("55.00"), balance("D"));
        assertEquals(2, service.obtenerHistorial("O").size() + service.obtenerHistorial("D").size());
        assertEquals(1, cuentaDAO.buscarPorNumero("O").getHistorial().size());
        assertEquals(1, cuentaDAO.buscarPorNumero("D").getHistorial().size());
    }

    @Test
    void insufficientBalanceDoesNotChangeEitherAccount() throws Exception {
        account("O", "5.00"); account("D", "20.00");
        assertThrows(SaldoInsuficienteException.class, () -> service.transferirConExcepciones("O", "D", bd("6.00")));
        assertEquals(bd("5.00"), balance("O")); assertEquals(bd("20.00"), balance("D"));
    }

    @Test
    void missingOriginDoesNotCreditDestination() throws Exception {
        account("D", "20.00");
        assertThrows(CuentaNoEncontradaException.class, () -> service.transferirConExcepciones("MISSING", "D", bd("5.00")));
        assertEquals(bd("20.00"), balance("D"));
    }

    @Test
    void missingDestinationDoesNotDebitOrigin() throws Exception {
        account("O", "20.00");
        assertThrows(CuentaNoEncontradaException.class, () -> service.transferirConExcepciones("O", "MISSING", bd("5.00")));
        assertEquals(bd("20.00"), balance("O"));
    }

    @Test
    void zeroAndNegativeAmountsAreRejected() throws Exception {
        account("O", "20.00"); account("D", "10.00");
        assertThrows(IllegalArgumentException.class, () -> service.transferirConExcepciones("O", "D", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> service.transferirConExcepciones("O", "D", bd("-1.00")));
        assertEquals(bd("20.00"), balance("O")); assertEquals(bd("10.00"), balance("D"));
    }

    @Test
    void sameAccountTransferCannotCreateMoney() throws Exception {
        account("O", "20.00");
        assertThrows(IllegalArgumentException.class, () -> service.transferirConExcepciones("O", "O", bd("5.00")));
        assertEquals(bd("20.00"), balance("O"));
        assertEquals(0, historyCount());
    }

    @Test
    void concurrentTransfersCannotOverspendTheSameOrigin() throws Exception {
        account("O", "100.00"); account("D", "0.00");
        CuentaService secondInstance = new CuentaService(new CuentaDAO(), new TransaccionDAO(), null);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> first = () -> succeedsOrInsufficient(service);
            Callable<Boolean> second = () -> succeedsOrInsufficient(secondInstance);
            var results = pool.invokeAll(java.util.List.of(first, second), 10, TimeUnit.SECONDS);
            assertEquals(1, results.stream().filter(future -> {
                try { return future.get(); } catch (Exception e) { throw new RuntimeException(e); }
            }).count());
        } finally {
            pool.shutdownNow();
        }
        assertEquals(bd("20.00"), balance("O"));
        assertEquals(bd("80.00"), balance("D"));
    }

    private static boolean succeedsOrInsufficient(CuentaService instance) {
        try { instance.transferirConExcepciones("O", "D", bd("80.00")); return true; }
        catch (SaldoInsuficienteException expected) { return false; }
    }

    @Test
    void failedCreditRollsBackDebit() throws Exception {
        account("O", "50.00"); account("D", "95.00");
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE cuentas ADD CONSTRAINT LIMITE_CREDITO CHECK (saldo <= 100.00)");
        }
        assertThrows(DatabaseConnectionException.class, () -> service.transferirConExcepciones("O", "D", bd("10.00")));
        assertEquals(bd("50.00"), balance("O")); assertEquals(bd("95.00"), balance("D"));
        assertEquals(0, historyCount());
    }

    @Test
    void failedHistoryInsertRollsBackBothAccountUpdatesAndEarlierHistoryInsert() throws Exception {
        account("O", "50.00"); account("D", "20.00");
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE transacciones ADD CONSTRAINT BLOQUEAR_HISTORIAL_DESTINO CHECK (numero_cuenta <> 'D')");
        }
        assertThrows(DatabaseConnectionException.class, () -> service.transferirConExcepciones("O", "D", bd("10.00")));
        assertEquals(bd("50.00"), balance("O")); assertEquals(bd("20.00"), balance("D"));
        assertEquals(0, historyCount());
    }

    @Test
    void failedDepositHistoryRollsBackBalance() throws Exception {
        account("O", "50.00");
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE transacciones ADD CONSTRAINT BLOQUEAR_HISTORIAL_ORIGEN CHECK (numero_cuenta <> 'O')");
        }
        assertThrows(DatabaseConnectionException.class, () -> service.depositar("O", bd("10.00")));
        assertEquals(bd("50.00"), balance("O"));
        assertEquals(0, historyCount());
    }

    @Test
    void failedWithdrawalHistoryRollsBackBalance() throws Exception {
        account("O", "50.00");
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE transacciones ADD CONSTRAINT BLOQUEAR_HISTORIAL_ORIGEN CHECK (numero_cuenta <> 'O')");
        }
        assertThrows(DatabaseConnectionException.class, () -> service.retirar("O", bd("10.00")));
        assertEquals(bd("50.00"), balance("O"));
        assertEquals(0, historyCount());
    }

    @Test
    void depositsWithdrawalsAndFreshServiceReadsUseDatabaseBalances() throws Exception {
        account("O", "30.00");
        assertEquals("Deposito exitoso", service.depositar("O", bd("12.50")));
        assertEquals("Retiro exitoso", service.retirar("O", bd("2.50")));
        CuentaService reopened = new CuentaService(new CuentaDAO(), new TransaccionDAO(), null);
        assertEquals(bd("40.00"), reopened.buscarCuenta("O").getSaldo());
        assertEquals(2, reopened.obtenerHistorial("O").size());
        assertEquals(2, cuentaDAO.buscarPorNumero("O").getHistorial().size());
    }

    @Test
    void genericRepositoriesSupportTypedCrudAndExplicitUpdate() throws Exception {
        CrudRepository<Cuenta, String> cuentas = new CuentaDAO();
        Cuenta cuenta = new Cuenta("CRUD-C", "DUI-C", "Ahorro");
        cuenta.setSaldo(bd("10.00"));
        cuentas.guardar(cuenta);
        assertEquals("CRUD-C", cuentas.buscarPorId("CRUD-C").getNumeroCuenta());
        cuenta.setSaldo(bd("14.00"));
        assertTrue(cuentas.actualizar(cuenta));
        assertEquals(bd("14.00"), cuentas.buscarPorId("CRUD-C").getSaldo());
        assertEquals(1, cuentas.listar().size());
        assertTrue(cuentas.eliminar("CRUD-C"));
        assertNull(cuentas.buscarPorId("CRUD-C"));

        CrudRepository<Cliente, String> clientes = new ClienteDAO();
        Cliente cliente = new Cliente("CRUD-U", "DUI-U", "Ana", "Prueba", "ana@test.local",
                "555", "Local", LocalDate.of(1990, 1, 1));
        clientes.guardar(cliente);
        cliente.setNombre("Ana Actualizada");
        assertTrue(clientes.actualizar(cliente));
        assertEquals("Ana Actualizada", clientes.buscarPorId("CRUD-U").getNombre());
        assertTrue(clientes.eliminar("CRUD-U"));
    }

    @Test
    void transactionRepositoryIsAppendOnlyAndCannotDeleteFinancialHistory() {
        assertTrue(AppendOnlyRepository.class.isAssignableFrom(TransaccionDAO.class));
        assertFalse(DeletableRepository.class.isAssignableFrom(TransaccionDAO.class));
        assertFalse(CrudRepository.class.isAssignableFrom(TransaccionDAO.class));
    }

    @Test
    void existingPersonaPolymorphismProvidesTypeForClientAndEmployee() {
        Persona cliente = new Cliente("P-1", "DUI-P", "Cliente", "Demo", "p@test.local",
                "555", "Local", LocalDate.of(1991, 1, 1));
        Persona cajero = new Cajero("E-1", "Cajero", "e@test.local", 800.0, "Caja-1");
        assertEquals("Cliente", cliente.getTipoPersona());
        assertEquals("EMPLEADO", cajero.getTipoPersona());
    }

    @Test
    void backgroundReportsAreNonBlockingAndRunConcurrentlyAlongsideAtomicTransfers(
            @TempDir Path tempDir) throws Exception {
        account("O", "100.00");
        account("D", "20.00");

        CountDownLatch reportsAtJdbcRead = new CountDownLatch(2);
        CountDownLatch allowReportsToContinue = new CountDownLatch(1);
        List<Connection> reportConnections = Collections.synchronizedList(new ArrayList<>());
        CuentaRepository concurrentReadRepository = reportBarrierRepository(
                reportsAtJdbcRead, allowReportsToContinue, reportConnections);
        var reportExecutor = Executors.newFixedThreadPool(2);
        CuentaService concurrentService = new CuentaService(
                concurrentReadRepository, new TransaccionDAO(), null, reportExecutor);
        var transferExecutor = Executors.newSingleThreadExecutor();

        try {
            var reportOne = concurrentService.generarReporteAsync(tempDir.resolve("report-one.txt"));
            var reportTwo = concurrentService.generarReporteAsync(tempDir.resolve("report-two.txt"));

            assertTrue(reportsAtJdbcRead.await(5, TimeUnit.SECONDS),
                    "Both report tasks should reach JDBC concurrently");
            assertFalse(reportOne.isDone());
            assertFalse(reportTwo.isDone());
            assertNotSame(reportConnections.get(0), reportConnections.get(1),
                    "Each report task must use its own JDBC connection");

            var transfer = transferExecutor.submit(
                    () -> service.transferir("O", "D", bd("30.00")));
            assertEquals("Transferencia exitosa", transfer.get(5, TimeUnit.SECONDS));
            allowReportsToContinue.countDown();

            assertEquals(tempDir.resolve("report-one.txt"), reportOne.get(5, TimeUnit.SECONDS));
            assertEquals(tempDir.resolve("report-two.txt"), reportTwo.get(5, TimeUnit.SECONDS));
            String reportSnapshot = Files.readString(tempDir.resolve("report-one.txt"));
            assertTrue(reportSnapshot.contains("Cuenta: O"));
            assertTrue(reportSnapshot.contains("Saldo: 100.00"));
            assertFalse(reportSnapshot.contains("TRANSFERENCIA"),
                    "Repeatable-read reports should not mix pre-transfer balances with post-transfer history");
            assertEquals(bd("70.00"), balance("O"));
            assertEquals(bd("50.00"), balance("D"));
            assertEquals(2, historyCount());
        } finally {
            allowReportsToContinue.countDown();
            transferExecutor.shutdown();
            assertTrue(transferExecutor.awaitTermination(5, TimeUnit.SECONDS));
            reportExecutor.shutdown();
            assertTrue(reportExecutor.awaitTermination(5, TimeUnit.SECONDS),
                    "Report executor threads should terminate after shutdown");
        }
    }

    @Test
    void backgroundReportPropagatesJdbcFailureAndExecutorCanClose(@TempDir Path tempDir) throws Exception {
        CuentaRepository failingRepository = (CuentaRepository) Proxy.newProxyInstance(
                CuentaRepository.class.getClassLoader(), new Class<?>[]{CuentaRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("listar") && args != null && args.length == 1) {
                        throw new SQLException("fallo JDBC de prueba");
                    }
                    try {
                        return method.invoke(cuentaDAO, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        var backgroundExecutor = Executors.newFixedThreadPool(2);
        CuentaService failingService = new CuentaService(
                failingRepository, new TransaccionDAO(), null, backgroundExecutor);
        try {
            var failed = failingService.generarReporteAsync(tempDir.resolve("never-created.txt"));
            var error = assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> failed.get(5, TimeUnit.SECONDS));
            assertTrue(error.getCause() instanceof SQLException);
            assertFalse(Files.exists(tempDir.resolve("never-created.txt")));
        } finally {
            backgroundExecutor.shutdown();
            assertTrue(backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS),
                    "Executor threads should terminate after shutdown");
        }
    }

    @Test
    void hypotheticalInterestSimulationDoesNotChangeBalancesOrLedger(@TempDir Path tempDir)
            throws Exception {
        account("SIM", "125.00");
        var backgroundExecutor = Executors.newFixedThreadPool(2);
        CuentaService simulationService = new CuentaService(
                new CuentaDAO(), new TransaccionDAO(), null, backgroundExecutor);
        Path output = tempDir.resolve("interest-simulation.txt");
        try {
            assertEquals(output, simulationService.simularInteresesAsync(
                    new BigDecimal("0.02"), output).get(5, TimeUnit.SECONDS));
            String report = Files.readString(output);
            assertTrue(report.contains("NO APLICADA A SALDOS"));
            assertTrue(report.contains("Interés hipotético: 2.50"));
            assertEquals(bd("125.00"), balance("SIM"));
            assertEquals(0, historyCount());
        } finally {
            backgroundExecutor.shutdown();
            assertTrue(backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static CuentaRepository reportBarrierRepository(CountDownLatch readers,
                                                            CountDownLatch release,
                                                            List<Connection> connections) {
        CuentaDAO delegate = new CuentaDAO();
        return (CuentaRepository) Proxy.newProxyInstance(
                CuentaRepository.class.getClassLoader(), new Class<?>[]{CuentaRepository.class},
                (proxy, method, args) -> {
                    Object result;
                    try {
                        result = method.invoke(delegate, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    if (method.getName().equals("listar") && args != null && args.length == 1) {
                        connections.add((Connection) args[0]);
                        readers.countDown();
                        if (!release.await(5, TimeUnit.SECONDS)) {
                            throw new SQLException("Timed out waiting for concurrent report tasks");
                        }
                    }
                    return result;
                });
    }

    private static void account(String number, String balance) throws Exception {
        Cuenta account = new Cuenta(number, "DUI-TEST", "Ahorro");
        account.setSaldo(bd(balance));
        cuentaDAO.guardar(account);
    }
    private static BigDecimal balance(String number) throws Exception {
        return cuentaDAO.buscarPorNumero(number).getSaldo();
    }
    private static int historyCount() throws Exception {
        try (Connection c = ConexionDB.obtenerConexion(); Statement s = c.createStatement();
             var r = s.executeQuery("SELECT COUNT(*) FROM transacciones")) { r.next(); return r.getInt(1); }
    }
    private static BigDecimal bd(String value) { return new BigDecimal(value); }
}
