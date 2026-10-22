-- escribir usuarios de prueba
INSERT INTO usuarios (id, nombre, email, password) VALUES 
(1, 'Alex Morales', 'alexmorales@gmail.com', 'contrasena123'),
(2, 'Juana Romero', 'juanaromero@yahoo.com', 'juana123');

-- escribir cuentas bancarias de prueba 
INSERT INTO cuentas (id, numero_cuenta, saldo, tipo_cuenta, usuario_id) VALUES 
(1, '20260115', 1508.00, 'AHORROS', 1),
(2, '84485657', 3970.50, 'CORRIENTE', 2);