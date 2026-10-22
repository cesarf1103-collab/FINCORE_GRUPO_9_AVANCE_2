package com.sv.fincore.service;

import com.sv.fincore.dao.CuentaDAO;
import com.sv.fincore.dao.CuentaRepository;
import com.sv.fincore.dao.TransaccionDAO;
import com.sv.fincore.dao.TransaccionRegistro;
import com.sv.fincore.dao.TransaccionRepository;
import com.sv.fincore.exception.CuentaNoEncontradaException;
import com.sv.fincore.exception.DatabaseConnectionException;
import com.sv.fincore.exception.SaldoInsuficienteException;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Deposito;
import com.sv.fincore.model.Retiro;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.model.Transferencia;
import com.sv.fincore.util.ConexionDB;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Integration version: keeps the existing service API and uses JDBC as source of truth. */
public class CuentaService implements AutoCloseable {
    private final CuentaRepository cuentaDAO;
    private final TransaccionRepository transaccionDAO;
    private final ClienteService clienteService;
    private final ExecutorService backgroundExecutor;
    private final boolean ownsBackgroundExecutor;

    public CuentaService(ClienteService clienteService) {
        this(new CuentaDAO(), new TransaccionDAO(), clienteService, newBackgroundExecutor(), true);
    }

    public CuentaService(CuentaRepository cuentaDAO) {
        this(cuentaDAO, new TransaccionDAO(), null, newBackgroundExecutor(), true);
    }

    public CuentaService(CuentaRepository cuentaDAO, TransaccionRepository transaccionDAO, ClienteService clienteService) {
        this(cuentaDAO, transaccionDAO, clienteService, newBackgroundExecutor(), true);
    }

    public CuentaService(ClienteService clienteService, ExecutorService backgroundExecutor) {
        this(new CuentaDAO(), new TransaccionDAO(), clienteService, backgroundExecutor, false);
    }

    public CuentaService(CuentaRepository cuentaDAO, TransaccionRepository transaccionDAO,
                         ClienteService clienteService, ExecutorService backgroundExecutor) {
        this(cuentaDAO, transaccionDAO, clienteService, backgroundExecutor, false);
    }

    private CuentaService(CuentaRepository cuentaDAO, TransaccionRepository transaccionDAO,
                          ClienteService clienteService, ExecutorService backgroundExecutor,
                          boolean ownsBackgroundExecutor) {
        this.cuentaDAO = cuentaDAO;
        this.transaccionDAO = transaccionDAO;
        this.clienteService = clienteService;
        this.backgroundExecutor = backgroundExecutor;
        this.ownsBackgroundExecutor = ownsBackgroundExecutor;
    }

    public synchronized String crearCuenta(String numeroCuenta, String duiCliente, String tipoCuenta) {
        try {
            if (cuentaDAO.buscarPorNumero(numeroCuenta) != null) return "Cuenta ya registrada";
            Cliente cliente = clienteService == null ? null : clienteService.buscarPorDui(duiCliente);
            if (cliente == null) return "Cliente no encontrado";
            Cuenta cuenta = new Cuenta(numeroCuenta, duiCliente, tipoCuenta);
            cuentaDAO.guardar(cuenta);
            cliente.agregarCuenta(numeroCuenta);
            return "Cuenta creada exitosamente";
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo crear la cuenta.", e);
        }
    }

    public List<Cuenta> listarCuentas() {
        try { return cuentaDAO.listar(); }
        catch (SQLException e) { throw new DatabaseConnectionException("No se pudieron consultar las cuentas.", e); }
    }

    public Cuenta buscarCuenta(String numeroCuenta) {
        try { return cuentaDAO.buscarPorNumero(numeroCuenta); }
        catch (SQLException e) { throw new DatabaseConnectionException("No se pudo consultar la cuenta " + numeroCuenta, e); }
    }

    public synchronized String depositar(String numeroCuenta, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) return "Monto invalido";
        try {
            return ejecutarMovimiento(numeroCuenta, monto, true);
        } catch (CuentaNoEncontradaException e) {
            return "Cuenta no encontrada";
        } catch (IllegalStateException e) {
            return "Cuenta inactiva";
        }
    }

    public synchronized String retirar(String numeroCuenta, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) return "Monto invalido";
        try {
            return ejecutarMovimiento(numeroCuenta, monto, false);
        } catch (CuentaNoEncontradaException e) {
            return "Cuenta no encontrada";
        } catch (SaldoInsuficienteException e) {
            return "Saldo insuficiente";
        } catch (IllegalStateException e) {
            return "Cuenta inactiva";
        }
    }

    private String ejecutarMovimiento(String numeroCuenta, BigDecimal monto, boolean deposito) {
        try (Connection connection = ConexionDB.obtenerConexion()) {
            connection.setAutoCommit(false);
            try {
                Cuenta cuenta = cuentaDAO.bloquearPorNumero(connection, numeroCuenta);
                if (cuenta == null) throw new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta);
                if (!cuenta.isActiva()) throw new IllegalStateException("La cuenta está inactiva: " + numeroCuenta);
                Transaccion movimiento = deposito
                        ? new Deposito(UUID.randomUUID().toString(), numeroCuenta, monto, "Depósito realizado")
                        : new Retiro(UUID.randomUUID().toString(), numeroCuenta, monto, "Retiro realizado");
                if (!deposito && cuenta.getSaldo().compareTo(monto) < 0)
                    throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta " + numeroCuenta);
                movimiento.ejecutar(cuenta, null);
                if (!cuentaDAO.actualizarSaldo(connection, numeroCuenta, cuenta.getSaldo()))
                    throw new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta);
                transaccionDAO.registrar(connection, movimiento);
                connection.commit();
                return deposito ? "Deposito exitoso" : "Retiro exitoso";
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            } catch (SQLException e) {
                rollback(connection, e);
                throw new DatabaseConnectionException("Falló la operación bancaria; se revirtió la transacción.", e);
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo abrir/cerrar la conexión bancaria.", e);
        }
    }

    public synchronized String transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) return "Monto invalido";
        if (cuentaOrigen == null || cuentaDestino == null) return "Cuenta no encontrada";
        if (cuentaOrigen.equals(cuentaDestino)) return "Las cuentas deben ser diferentes";
        try {
            return transferirConExcepciones(cuentaOrigen, cuentaDestino, monto);
        } catch (CuentaNoEncontradaException e) {
            return "Cuenta no encontrada";
        } catch (SaldoInsuficienteException e) {
            return "Saldo insuficiente";
        } catch (IllegalStateException e) {
            return "Cuenta inactiva";
        }
    }

    /** Transactional core, retaining Josue's typed business exceptions for API callers. */
    public synchronized String transferirConExcepciones(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        validarMonto(monto);
        if (cuentaOrigen == null || cuentaDestino == null || cuentaOrigen.equals(cuentaDestino))
            throw new IllegalArgumentException("Las cuentas de origen y destino deben ser distintas y válidas.");

        try (Connection connection = ConexionDB.obtenerConexion()) {
            connection.setAutoCommit(false);
            try {
                String primero = cuentaOrigen.compareTo(cuentaDestino) < 0 ? cuentaOrigen : cuentaDestino;
                String segundo = cuentaOrigen.compareTo(cuentaDestino) < 0 ? cuentaDestino : cuentaOrigen;
                Cuenta cuentaPrimera = cuentaDAO.bloquearPorNumero(connection, primero);
                Cuenta cuentaSegunda = cuentaDAO.bloquearPorNumero(connection, segundo);
                Cuenta origen = cuentaOrigen.equals(primero) ? cuentaPrimera : cuentaSegunda;
                Cuenta destino = cuentaDestino.equals(primero) ? cuentaPrimera : cuentaSegunda;
                if (origen == null) throw new CuentaNoEncontradaException("La cuenta de origen no existe: " + cuentaOrigen);
                if (destino == null) throw new CuentaNoEncontradaException("La cuenta de destino no existe: " + cuentaDestino);
                if (!origen.isActiva() || !destino.isActiva()) throw new IllegalStateException("No se puede transferir desde/hacia una cuenta inactiva.");
                if (origen.getSaldo().compareTo(monto) < 0) throw new SaldoInsuficienteException("Saldo insuficiente en la cuenta origen.");

                Transferencia salida = new Transferencia(UUID.randomUUID().toString(), cuentaOrigen,
                        cuentaDestino, monto, "Transferencia enviada");
                salida.ejecutar(origen, destino);
                Transaccion entrada = salida.registroDestino(UUID.randomUUID().toString(), destino.getSaldo());
                if (!cuentaDAO.actualizarSaldo(connection, cuentaOrigen, origen.getSaldo())
                        || !cuentaDAO.actualizarSaldo(connection, cuentaDestino, destino.getSaldo()))
                    throw new SQLException("No se actualizaron ambas cuentas.");
                transaccionDAO.registrar(connection, salida);
                transaccionDAO.registrar(connection, entrada);
                connection.commit();
                return "Transferencia exitosa";
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            } catch (SQLException e) {
                rollback(connection, e);
                throw new DatabaseConnectionException("Falló la transferencia; se revirtió la transacción.", e);
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo abrir/cerrar la conexión bancaria.", e);
        }
    }

    public List<Transaccion> obtenerHistorial(String numeroCuenta) {
        try {
            List<Transaccion> historial = new ArrayList<>();
            for (TransaccionRegistro registro : transaccionDAO.listarPorCuenta(numeroCuenta)) {
                historial.add(registro.aModelo());
            }
            return historial;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo consultar el historial.", e);
        }
    }

    public synchronized String desactivarCuenta(String numeroCuenta) {
        Cuenta cuenta = buscarCuenta(numeroCuenta);
        if (cuenta == null) return "Cuenta no encontrada";
        cuenta.setActiva(false);
        try { cuentaDAO.guardar(cuenta); return "Cuenta desactivada"; }
        catch (SQLException e) { throw new DatabaseConnectionException("No se pudo desactivar la cuenta.", e); }
    }

    public String generarReporteEnSegundoPlano() {
        Path ruta = Path.of("datos", "reporte_bancario.txt");
        generarReporteAsync(ruta).whenComplete((generado, error) -> {
            if (error != null) {
                System.err.println("No se pudo generar el reporte: " + error.getMessage());
            }
        });
        return "Generando reporte en segundo plano: " + ruta;
    }

    public CompletableFuture<Path> generarReporteAsync() {
        return generarReporteAsync(Path.of("datos", "reporte_bancario.txt"));
    }

    public CompletableFuture<Path> generarReporteAsync(Path destino) {
        CompletableFuture<Path> resultado = new CompletableFuture<>();
        try {
            backgroundExecutor.execute(new ProcesadorReportesTask(this, destino, resultado));
        } catch (RuntimeException e) {
            resultado.completeExceptionally(e);
        }
        return resultado;
    }

    /** Calculates a clearly labeled, read-only example; it never changes balances or ledger rows. */
    public CompletableFuture<Path> simularInteresesAsync(BigDecimal tasaHipotetica, Path destino) {
        CompletableFuture<Path> resultado = new CompletableFuture<>();
        try {
            backgroundExecutor.execute(() -> {
                try {
                    resultado.complete(generarSimulacionIntereses(tasaHipotetica, destino));
                } catch (Exception e) {
                    resultado.completeExceptionally(e);
                }
            });
        } catch (RuntimeException e) {
            resultado.completeExceptionally(e);
        }
        return resultado;
    }

    /** Compatibility with Vidal's Runnable: a read-only simulation, never a balance update. */
    public void calcularIntereses() {
        BigDecimal tasa = new BigDecimal("0.02");
        try {
            System.out.println("Simulacion de intereses (no se aplicaran a las cuentas):");
            for (Cuenta cuenta : cuentaDAO.listar()) {
                if (cuenta.isActiva()) {
                    BigDecimal estimado = cuenta.getSaldo().multiply(tasa)
                            .setScale(2, RoundingMode.HALF_UP);
                    System.out.printf("Cuenta %s | saldo actual: %s | interes hipotetico: %s (no aplicado)%n",
                            cuenta.getNumeroCuenta(), cuenta.getSaldo(), estimado);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException("No se pudo simular el calculo de intereses.", e);
        }
    }

    private Path generarSimulacionIntereses(BigDecimal tasaHipotetica, Path destino)
            throws SQLException, java.io.IOException {
        if (tasaHipotetica == null || tasaHipotetica.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa hipotética no puede ser negativa ni nula.");
        }
        List<String> lineas = new ArrayList<>();
        lineas.add("SIMULACION DE INTERESES — NO APLICADA A SALDOS");
        lineas.add("Tasa hipotética: " + tasaHipotetica.toPlainString());
        for (Cuenta cuenta : cuentaDAO.listar()) {
            if (cuenta.isActiva()) {
                BigDecimal estimado = cuenta.getSaldo().multiply(tasaHipotetica)
                        .setScale(2, RoundingMode.HALF_UP);
                lineas.add("Cuenta: " + cuenta.getNumeroCuenta() + " | Saldo actual: "
                        + cuenta.getSaldo() + " | Interés hipotético: " + estimado);
            }
        }
        Path parent = destino.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(destino, lineas, StandardCharsets.UTF_8);
        return destino;
    }

    Path generarReporte(Path destino) throws SQLException, java.io.IOException {
        List<Cuenta> cuentas;
        try (Connection connection = ConexionDB.obtenerConexion()) {
            connection.setReadOnly(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                cuentas = cuentaDAO.listar(connection);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
        return new ReporteBancario(cuentas, destino).generar();
    }

    @Override
    public void close() {
        if (!ownsBackgroundExecutor) return;
        backgroundExecutor.shutdown();
        try {
            if (!backgroundExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ExecutorService newBackgroundExecutor() {
        return Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("FinCore-background-" + thread.getId());
            return thread;
        });
    }

    private static void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
    }

    private static void rollback(Connection connection, Exception original) {
        try { connection.rollback(); }
        catch (SQLException rollbackError) { original.addSuppressed(rollbackError); }
    }

}
