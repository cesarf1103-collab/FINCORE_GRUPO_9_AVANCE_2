package com.sv.fincore.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sv.fincore.controller.ClienteController;
import com.sv.fincore.controller.CuentaController;
import com.sv.fincore.controller.TransaccionController;
import com.sv.fincore.dao.TransaccionDAO;
import com.sv.fincore.dao.TransaccionRegistro;
import com.sv.fincore.model.Cliente;
import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.service.ClienteService;
import com.sv.fincore.service.CuentaService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Interfaz HTTP local; las operaciones bancarias permanecen en los servicios. */
public final class FinCoreHttpServer implements AutoCloseable {
    private static final int MAX_BODY = 16_384;
    private final HttpServer server;
    private final ExecutorService requests = Executors.newFixedThreadPool(4);
    private final ObjectMapper json = new ObjectMapper();
    private final ClienteController clientes;
    private final CuentaController cuentas;
    private final TransaccionController operaciones;
    private final CuentaService cuentaService;
    private final Map<String, CompletableFuture<Path>> reportes = new ConcurrentHashMap<>();

    public FinCoreHttpServer(int port, ClienteService clienteService, CuentaService cuentaService) throws IOException {
        this.cuentaService = cuentaService;
        clientes = new ClienteController(clienteService);
        cuentas = new CuentaController(cuentaService);
        operaciones = new TransaccionController(cuentaService);
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(requests);
    }

    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                if (!validOrigin(exchange)) { send(exchange, 403, Map.of("error", "Origen no permitido")); return; }
                api(exchange, path);
            } else {
                staticFile(exchange, path);
            }
        } catch (IllegalArgumentException e) {
            send(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Solicitud web fallida: " + e);
            send(exchange, 500, Map.of("error", "No se pudo completar la solicitud. Consulte la consola."));
        } finally {
            exchange.close();
        }
    }

    private boolean validOrigin(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin == null) return true; // CLI/local tests; server still binds only to loopback.
        return origin.equals("http://127.0.0.1:" + port()) || origin.equals("http://localhost:" + port());
    }

    private void api(HttpExchange x, String path) throws Exception {
        String method = x.getRequestMethod();
        if (path.equals("/api/salud") && method.equals("GET")) { send(x, 200, Map.of("estado", "ok")); return; }
        if (path.equals("/api/resumen") && method.equals("GET")) {
            List<Cuenta> lista = cuentas.listarCuentas();
            BigDecimal total = lista.stream().filter(Cuenta::isActiva).map(Cuenta::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
            List<Map<String, Object>> recientes = new ArrayList<>();
            for (TransaccionRegistro t : new TransaccionDAO().listar()) recientes.add(transaccion(t.aModelo()));
            recientes.sort(Comparator.comparing(m -> (String) m.get("fecha"), Comparator.reverseOrder()));
            send(x, 200, Map.of("clientes", clientes.listarClientes().size(), "cuentas", lista.size(),
                    "cuentasActivas", lista.stream().filter(Cuenta::isActiva).count(), "saldoTotal", total,
                    "recientes", recientes.subList(0, Math.min(6, recientes.size())))); return;
        }
        if (path.equals("/api/clientes")) {
            if (method.equals("GET")) { send(x, 200, clientes.listarClientes().stream().map(this::cliente).toList()); return; }
            if (method.equals("POST")) {
                JsonNode b = body(x);
                String dui = required(b, "dui");
                if (!dui.matches("\\d{8}-\\d")) throw new IllegalArgumentException("DUI inválido (########-#).");
                String correo = required(b, "correo"), telefono = required(b, "telefono");
                if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("Correo inválido.");
                if (!telefono.matches("\\d{8}")) throw new IllegalArgumentException("Teléfono inválido (8 dígitos).");
                LocalDate fecha;
                try { fecha = LocalDate.parse(required(b, "fechaNacimiento")); }
                catch (DateTimeParseException e) { throw new IllegalArgumentException("Fecha de nacimiento inválida."); }
                if (fecha.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
                Cliente c = new Cliente(UUID.randomUUID().toString(), dui, required(b, "nombre"),
                        required(b, "apellidos"), correo, telefono, required(b, "direccion"), fecha);
                result(x, clientes.registrarCliente(c), "Cliente registrado exitosamente", 201); return;
            }
        }
        if (path.startsWith("/api/clientes/")) {
            String dui = decode(path.substring("/api/clientes/".length()));
            if (method.equals("GET")) { Cliente c = clientes.buscarPorDui(dui); if (c == null) notFound(x); else send(x, 200, cliente(c)); return; }
            if (method.equals("PUT")) {
                JsonNode b = body(x);
                String correo = required(b, "correo"), telefono = required(b, "telefono");
                if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || !telefono.matches("\\d{8}"))
                    throw new IllegalArgumentException("Correo o teléfono inválido.");
                result(x, clientes.modificarCliente(dui, required(b, "nombre"), required(b, "apellidos"),
                        correo, telefono, required(b, "direccion")), "Cliente actualizado", 200); return;
            }
            if (method.equals("DELETE")) { result(x, clientes.eliminarCliente(dui), "Cliente eliminado", 200); return; }
        }
        if (path.equals("/api/cuentas")) {
            if (method.equals("GET")) { send(x, 200, cuentas.listarCuentas().stream().map(this::cuenta).toList()); return; }
            if (method.equals("POST")) {
                JsonNode b = body(x);
                String tipo = required(b, "tipoCuenta");
                if (!tipo.equalsIgnoreCase("Ahorro") && !tipo.equalsIgnoreCase("Corriente")) throw new IllegalArgumentException("Tipo de cuenta inválido.");
                result(x, cuentas.crearCuenta(required(b, "numeroCuenta"), required(b, "duiCliente"), tipo), "Cuenta creada exitosamente", 201); return;
            }
        }
        if (path.startsWith("/api/cuentas/")) {
            String tail = path.substring("/api/cuentas/".length());
            if (tail.endsWith("/historial") && method.equals("GET")) {
                String numero = decode(tail.substring(0, tail.length() - "/historial".length()));
                if (cuentas.buscarCuenta(numero) == null) notFound(x);
                else send(x, 200, cuentas.obtenerHistorial(numero).stream().map(this::transaccion).toList());
                return;
            }
            if (tail.endsWith("/desactivar") && method.equals("POST")) {
                result(x, cuentas.desactivarCuenta(decode(tail.substring(0, tail.length() - "/desactivar".length()))), "Cuenta desactivada", 200); return;
            }
            if (method.equals("GET")) { Cuenta c = cuentas.buscarCuenta(decode(tail)); if (c == null) notFound(x); else send(x, 200, cuenta(c)); return; }
        }
        if (path.startsWith("/api/operaciones/") && method.equals("POST")) {
            JsonNode b = body(x);
            BigDecimal monto;
            try { monto = new BigDecimal(required(b, "monto")); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Monto inválido."); }
            String ok, respuesta;
            switch (path.substring("/api/operaciones/".length())) {
                case "depositos" -> { ok = "Deposito exitoso"; respuesta = operaciones.depositar(required(b, "numeroCuenta"), monto); }
                case "retiros" -> { ok = "Retiro exitoso"; respuesta = operaciones.retirar(required(b, "numeroCuenta"), monto); }
                case "transferencias" -> { ok = "Transferencia exitosa"; respuesta = operaciones.transferir(required(b, "cuentaOrigen"), required(b, "cuentaDestino"), monto); }
                default -> { notFound(x); return; }
            }
            result(x, respuesta, ok, 200); return;
        }
        if (path.equals("/api/reportes") && method.equals("POST")) {
            String id = UUID.randomUUID().toString();
            reportes.put(id, cuentaService.generarReporteAsync(Path.of("datos", "reportes", id + ".txt")));
            send(x, 202, Map.of("id", id, "estado", "procesando")); return;
        }
        if (path.startsWith("/api/reportes/") && method.equals("GET")) {
            String tail = path.substring("/api/reportes/".length());
            boolean archivo = tail.endsWith("/archivo");
            String id = archivo ? tail.substring(0, tail.length() - "/archivo".length()) : tail;
            CompletableFuture<Path> futuro = reportes.get(id);
            if (futuro == null) { notFound(x); return; }
            if (!futuro.isDone()) { send(x, 200, Map.of("estado", "procesando")); return; }
            if (futuro.isCompletedExceptionally()) { send(x, 500, Map.of("error", "No se pudo generar el reporte.")); return; }
            if (archivo) {
                byte[] data = Files.readAllBytes(futuro.join());
                x.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                x.getResponseHeaders().set("Content-Disposition", "attachment; filename=finCore-reporte.txt");
                x.sendResponseHeaders(200, data.length); x.getResponseBody().write(data);
            } else send(x, 200, Map.of("estado", "listo", "url", "/api/reportes/" + id + "/archivo"));
            return;
        }
        notFound(x);
    }

    private JsonNode body(HttpExchange x) throws IOException {
        String type = x.getRequestHeaders().getFirst("Content-Type");
        if (type == null || !type.toLowerCase().startsWith("application/json")) throw new IllegalArgumentException("Se requiere JSON.");
        byte[] data = x.getRequestBody().readNBytes(MAX_BODY + 1);
        if (data.length > MAX_BODY) throw new IllegalArgumentException("Solicitud demasiado grande.");
        try { JsonNode node = json.readTree(data); if (node == null || !node.isObject()) throw new IllegalArgumentException("JSON inválido."); return node; }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalArgumentException("JSON inválido."); }
    }

    private static String required(JsonNode b, String name) {
        String value = b.path(name).asText("").trim();
        if (value.isEmpty() || value.length() > 160) throw new IllegalArgumentException("Campo obligatorio o demasiado largo: " + name);
        return value;
    }

    private static String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
    private void result(HttpExchange x, String actual, String expected, int success) throws IOException {
        send(x, actual.equals(expected) ? success : 409, actual.equals(expected) ? Map.of("mensaje", actual) : Map.of("error", actual));
    }
    private void notFound(HttpExchange x) throws IOException { send(x, 404, Map.of("error", "Recurso no encontrado")); }
    private void send(HttpExchange x, int status, Object value) throws IOException {
        byte[] data = json.writeValueAsBytes(value);
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        x.getResponseHeaders().set("Cache-Control", "no-store");
        x.sendResponseHeaders(status, data.length); x.getResponseBody().write(data);
    }
    private Map<String, Object> cliente(Cliente c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("dui", c.getDui()); m.put("nombre", c.getNombre());
        m.put("apellidos", c.getApellidos()); m.put("correo", c.getCorreo()); m.put("telefono", c.getTelefono());
        m.put("direccion", c.getDireccion()); m.put("fechaNacimiento", c.getFechaNacimiento().toString());
        return m;
    }
    private Map<String, Object> cuenta(Cuenta c) {
        return Map.of("numeroCuenta", c.getNumeroCuenta(), "duiCliente", c.getDuiCliente(),
                "saldo", c.getSaldo(), "tipoCuenta", c.getTipoCuenta(), "activa", c.isActiva(),
                "fechaApertura", c.getFechaApertura().toString());
    }
    private Map<String, Object> transaccion(Transaccion t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getIdTransaccion()); m.put("numeroCuenta", t.getNumeroCuenta()); m.put("tipo", t.getTipo());
        m.put("monto", t.getMonto()); m.put("saldoPosterior", t.getSaldoPosterior());
        m.put("fecha", t.getFecha().toString()); m.put("descripcion", t.getDescripcion());
        m.put("cuentaOrigen", t.getCuentaOrigen()); m.put("cuentaDestino", t.getCuentaDestino());
        return m;
    }
    private void staticFile(HttpExchange x, String path) throws IOException {
        if (!x.getRequestMethod().equals("GET")) { send(x, 405, Map.of("error", "Método no permitido")); return; }
        String name = path.equals("/") ? "index.html" : path.startsWith("/assets/") ? path.substring(8) : "";
        if (!List.of("index.html", "app.css", "brand.css", "app.js", "fincore-logo.png").contains(name)) { notFound(x); return; }
        try (var input = getClass().getResourceAsStream("/web/" + name)) {
            if (input == null) { notFound(x); return; }
            byte[] data = input.readAllBytes();
            String mime = name.endsWith(".css") ? "text/css; charset=utf-8"
                    : name.endsWith(".js") ? "text/javascript; charset=utf-8"
                    : name.endsWith(".png") ? "image/png" : "text/html; charset=utf-8";
            x.getResponseHeaders().set("Content-Type", mime);
            x.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'self'");
            x.sendResponseHeaders(200, data.length); x.getResponseBody().write(data);
        }
    }
    @Override public void close() {
        server.stop(1);
        requests.shutdown();
        try { if (!requests.awaitTermination(5, TimeUnit.SECONDS)) requests.shutdownNow(); }
        catch (InterruptedException e) { requests.shutdownNow(); Thread.currentThread().interrupt(); }
        cuentaService.close();
    }
}
