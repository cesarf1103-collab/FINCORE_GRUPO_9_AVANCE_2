package com.sv.fincore.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sv.fincore.util.ConexionDB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FinCoreHttpServerTest {
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private FinCoreHttpServer web;
    private String dui, first, second, reportId;

    @AfterEach void cleanup() throws Exception {
        if (web != null) web.close();
        if (first != null) {
            try (Connection c = ConexionDB.obtenerConexion()) {
                for (String sql : new String[]{"DELETE FROM transacciones WHERE numero_cuenta IN (?, ?)", "DELETE FROM cuentas WHERE numero_cuenta IN (?, ?)"}) {
                    try (PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, first); s.setString(2, second); s.executeUpdate(); }
                }
                try (PreparedStatement s = c.prepareStatement("DELETE FROM clientes WHERE dui = ?")) { s.setString(1, dui); s.executeUpdate(); }
            }
        }
        if (reportId != null) Files.deleteIfExists(Path.of("datos", "reportes", reportId + ".txt"));
    }

    @Test void webApiPreservaSaldosHistorialYTransferencia() throws Exception {
        start();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        dui = "8" + String.format("%07d", Math.abs(suffix.hashCode()) % 10_000_000) + "-1";
        first = "WEB-A-" + suffix; second = "WEB-B-" + suffix;
        assertEquals(201, send("POST", "/api/clientes", """
                {"dui":"%s","nombre":"Prueba","apellidos":"Web","correo":"web@example.invalid","telefono":"12345678","direccion":"Temporal","fechaNacimiento":"2000-01-01"}
                """.formatted(dui)).statusCode());
        assertEquals(dui, get("/api/clientes/" + dui).path("dui").asText());
        assertEquals(201, send("POST", "/api/cuentas", "{" + "\"numeroCuenta\":\"" + first + "\",\"duiCliente\":\"" + dui + "\",\"tipoCuenta\":\"Ahorro\"}").statusCode());
        assertEquals(201, send("POST", "/api/cuentas", "{" + "\"numeroCuenta\":\"" + second + "\",\"duiCliente\":\"" + dui + "\",\"tipoCuenta\":\"Corriente\"}").statusCode());
        assertEquals(200, send("POST", "/api/operaciones/depositos", "{" + "\"numeroCuenta\":\"" + first + "\",\"monto\":\"100.00\"}").statusCode());
        assertEquals(200, send("POST", "/api/operaciones/retiros", "{" + "\"numeroCuenta\":\"" + first + "\",\"monto\":\"20.00\"}").statusCode());
        assertEquals(200, send("POST", "/api/operaciones/transferencias", "{" + "\"cuentaOrigen\":\"" + first + "\",\"cuentaDestino\":\"" + second + "\",\"monto\":\"30.00\"}").statusCode());
        assertEquals(409, send("POST", "/api/operaciones/transferencias", "{" + "\"cuentaOrigen\":\"" + first + "\",\"cuentaDestino\":\"" + second + "\",\"monto\":\"999.00\"}").statusCode());
        assertEquals(409, send("POST", "/api/operaciones/depositos", "{" + "\"numeroCuenta\":\"" + first + "\",\"monto\":\"-1.00\"}").statusCode());
        assertEquals(0, new java.math.BigDecimal(get("/api/cuentas/" + first).path("saldo").asText()).compareTo(new java.math.BigDecimal("50.00")));
        assertEquals(0, new java.math.BigDecimal(get("/api/cuentas/" + second).path("saldo").asText()).compareTo(new java.math.BigDecimal("30.00")));
        assertEquals(3, get("/api/cuentas/" + first + "/historial").size());
        assertEquals(1, get("/api/cuentas/" + second + "/historial").size());
        assertEquals(200, send("GET", "/", null).statusCode());
        assertEquals(200, send("GET", "/assets/app.js", null).statusCode());
        HttpResponse<String> logo = send("GET", "/assets/fincore-logo.png", null);
        assertEquals(200, logo.statusCode());
        assertEquals("image/png", logo.headers().firstValue("Content-Type").orElseThrow());
        web.close(); web = null;
        start(); // New services, same H2 memory database: read persisted SQL state again.
        assertEquals(0, new java.math.BigDecimal(get("/api/cuentas/" + first).path("saldo").asText()).compareTo(new java.math.BigDecimal("50.00")));
        assertEquals(3, get("/api/cuentas/" + first + "/historial").size());
    }

    @Test void reporteSoloSeGeneraAlSolicitarloYNoCambiaSaldos() throws Exception {
        start();
        JsonNode before = get("/api/resumen");
        JsonNode created = json.readTree(send("POST", "/api/reportes", "{}").body());
        reportId = created.path("id").asText();
        assertFalse(reportId.isBlank());
        JsonNode status = null;
        for (int i = 0; i < 30; i++) {
            status = get("/api/reportes/" + reportId);
            if ("listo".equals(status.path("estado").asText())) break;
            Thread.sleep(100);
        }
        assertEquals("listo", status.path("estado").asText());
        assertEquals(200, send("GET", status.path("url").asText(), null).statusCode());
        JsonNode after = get("/api/resumen");
        assertEquals(before.path("saldoTotal"), after.path("saldoTotal"));
        assertEquals(before.path("recientes"), after.path("recientes"));
    }

    private void start() throws Exception {
        ClienteService clients = new ClienteService();
        web = new FinCoreHttpServer(0, clients, new CuentaService(clients));
        web.start();
    }
    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + web.port() + path));
        if (body == null) request.GET(); else request.method(method, HttpRequest.BodyPublishers.ofString(body)).header("Content-Type", "application/json");
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    private JsonNode get(String path) throws Exception { return json.readTree(send("GET", path, null).body()); }
}
