package com.sv.fincore.dao;

import com.sv.fincore.model.Cuenta;
import com.sv.fincore.model.Transaccion;
import com.sv.fincore.model.TransaccionAnterior;
import com.sv.fincore.util.ArchivoUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CuentaDAO {

    private static final String RUTA = "datos/cuentas.dat";

    private Map<String, Cuenta> cuentas;

    public CuentaDAO() throws IOException, ClassNotFoundException {
        try {
            this.cuentas = ArchivoUtil.cargar(RUTA, new HashMap<>());
        } catch (InvalidClassException formatoAnterior) {
            this.cuentas = cargarFormatoAnterior();

            for (Cuenta cuenta : cuentas.values()) {
                List<Transaccion> nuevos = new ArrayList<>();

                for (Object movimiento : cuenta.getHistorial()) {
                    if (!(movimiento instanceof TransaccionAnterior anterior)) {
                        throw new IOException("Movimiento anterior no reconocido");
                    }
                    nuevos.add(anterior.convertir());
                }

                cuenta.getHistorial().clear();
                cuenta.getHistorial().addAll(nuevos);
            }

            Path respaldo = Path.of(RUTA + ".bak");
            if (!Files.exists(respaldo)) {
                Files.copy(Path.of(RUTA), respaldo);
            }
            persistir();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Cuenta> cargarFormatoAnterior()
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new FileInputStream(RUTA)) {
            @Override
            protected ObjectStreamClass readClassDescriptor()
                    throws IOException, ClassNotFoundException {
                ObjectStreamClass descriptor = super.readClassDescriptor();

                if (descriptor.getName().equals("com.sv.fincore.model.Transaccion")) {
                    return ObjectStreamClass.lookup(TransaccionAnterior.class);
                }
                return descriptor;
            }
        }) {
            return (Map<String, Cuenta>) entrada.readObject();
        }
    }

    public void guardar(Cuenta c) throws IOException {
        cuentas.put(c.getNumeroCuenta(), c);
        persistir();
    }

    public Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    public List<Cuenta> listar() {
        return new ArrayList<>(cuentas.values());
    }

    public List<Cuenta> listarPorDui(String duiCliente) {
        return cuentas.values().stream()
                .filter(c -> c.getDuiCliente().equals(duiCliente))
                .collect(Collectors.toList());
    }

    public boolean actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo)
            throws IOException {
        Cuenta c = cuentas.get(numeroCuenta);
        if (c == null) return false;

        c.setSaldo(nuevoSaldo);
        persistir();
        return true;
    }

    public boolean eliminar(String numeroCuenta) throws IOException {
        if (cuentas.remove(numeroCuenta) != null) {
            persistir();
            return true;
        }
        return false;
    }

    private void persistir() throws IOException {
        ArchivoUtil.guardar(RUTA, cuentas);
    }
}
