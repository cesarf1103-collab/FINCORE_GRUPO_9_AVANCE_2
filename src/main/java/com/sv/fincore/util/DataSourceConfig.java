package com.sv.fincore.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;

/** Pool único de conexiones. Cada DAO devuelve su conexión con try-with-resources. */
public final class DataSourceConfig {
    private static BasicDataSource dataSource;

    private DataSourceConfig() {}

    public static synchronized BasicDataSource obtenerDataSource() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource;
        }

        String url = valor("fincore.db.url", "FINCORE_DB_URL",
                "jdbc:h2:file:./datos/fincore");
        if (url.equals("jdbc:h2:file:./datos/fincore")) {
            try {
                Files.createDirectories(Path.of("datos"));
            } catch (IOException e) {
                throw new SQLException("No se pudo preparar el directorio de datos", e);
            }
        }

        BasicDataSource pool = new BasicDataSource();
        pool.setUrl(url);
        pool.setUsername(valor("fincore.db.user", "FINCORE_DB_USER", "sa"));
        pool.setPassword(valor("fincore.db.password", "FINCORE_DB_PASSWORD", ""));

        // Los 15 parámetros del pool solicitados por Arquitectura (#40).
        pool.setInitialSize(entero("initialSize", "INITIAL_SIZE", 2));
        pool.setMaxTotal(entero("maxTotal", "MAX_TOTAL", 10)); // maxActive en DBCP 1.x
        pool.setMinIdle(entero("minIdle", "MIN_IDLE", 2));
        pool.setMaxIdle(entero("maxIdle", "MAX_IDLE", 5));
        pool.setMaxWait(Duration.ofMillis(entero("maxWaitMillis", "MAX_WAIT_MILLIS", 5000)));
        pool.setTestOnBorrow(booleano("testOnBorrow", "TEST_ON_BORROW", true));
        pool.setTestOnReturn(booleano("testOnReturn", "TEST_ON_RETURN", true));
        pool.setTestWhileIdle(booleano("testWhileIdle", "TEST_WHILE_IDLE", true));
        pool.setValidationQuery(valor("fincore.pool.validationQuery",
                "FINCORE_POOL_VALIDATION_QUERY", "SELECT 1"));
        pool.setValidationQueryTimeout(Duration.ofSeconds(
                entero("validationQueryTimeoutSeconds", "VALIDATION_QUERY_TIMEOUT_SECONDS", 3)));
        pool.setDurationBetweenEvictionRuns(Duration.ofSeconds(
                entero("timeBetweenEvictionRunsSeconds", "TIME_BETWEEN_EVICTION_RUNS_SECONDS", 30)));
        pool.setMinEvictableIdle(Duration.ofSeconds(
                entero("minEvictableIdleTimeSeconds", "MIN_EVICTABLE_IDLE_TIME_SECONDS", 60)));
        pool.setNumTestsPerEvictionRun(entero("numTestsPerEvictionRun", "NUM_TESTS_PER_EVICTION_RUN", 3));
        pool.setRemoveAbandonedOnBorrow(booleano("removeAbandonedOnBorrow",
                "REMOVE_ABANDONED_ON_BORROW", true));
        pool.setRemoveAbandonedTimeout(Duration.ofSeconds(
                entero("removeAbandonedTimeoutSeconds", "REMOVE_ABANDONED_TIMEOUT_SECONDS", 120)));

        try {
            boolean inicializar = Boolean.parseBoolean(valor("fincore.db.init",
                    "FINCORE_DB_INIT", Boolean.toString(url.startsWith("jdbc:h2:"))));
            if (inicializar) {
                SchemaInitializer.ejecutar(pool, "schema.sql");
            }
            if (Boolean.parseBoolean(valor("fincore.db.seed", "FINCORE_DB_SEED", "false"))) {
                SchemaInitializer.ejecutar(pool, "data.sql");
            }
            dataSource = pool;
            return pool;
        } catch (SQLException e) {
            try {
                pool.close();
            } catch (SQLException cierre) {
                e.addSuppressed(cierre);
            }
            throw e;
        }
    }

    public static synchronized void cerrar() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static int entero(String nombre, String entorno, int defecto) {
        return Integer.parseInt(valor("fincore.pool." + nombre,
                "FINCORE_POOL_" + entorno, Integer.toString(defecto)));
    }

    private static boolean booleano(String nombre, String entorno, boolean defecto) {
        return Boolean.parseBoolean(valor("fincore.pool." + nombre,
                "FINCORE_POOL_" + entorno, Boolean.toString(defecto)));
    }

    private static String valor(String propiedad, String entorno, String defecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null) {
            valor = System.getenv(entorno);
        }
        return valor == null ? defecto : valor;
    }
}
