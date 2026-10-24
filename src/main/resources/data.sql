-- Demo data is opt-in (FINCORE_DB_SEED=true) and idempotent.
-- No passwords are shipped; users must be provisioned separately.
INSERT INTO clientes (id, dui, nombre, apellidos, correo, telefono, direccion, fecha_nacimiento)
SELECT 'demo-cliente-1', '00000000-0', 'Cliente', 'Demostracion', 'demo@example.invalid', '00000000', 'Demo', DATE '2000-01-01'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 'demo-cliente-1');

INSERT INTO cuentas (numero_cuenta, dui_cliente, saldo, tipo_cuenta, activa)
SELECT 'DEMO-001', '00000000-0', 0.00, 'Ahorro', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cuentas WHERE numero_cuenta = 'DEMO-001');

INSERT INTO usuarios (id, username, password_hash, rol, activo)
SELECT 'demo-user-disabled', 'demo-disabled', '!unconfigured', 'CAJERO', FALSE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 'demo-user-disabled');
