-- FinCore H2 schema. No DROP or destructive migration on startup.
CREATE TABLE IF NOT EXISTS clientes (
    id VARCHAR(64) PRIMARY KEY,
    dui VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    correo VARCHAR(180),
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    fecha_nacimiento DATE
);

CREATE TABLE IF NOT EXISTS cuentas (
    numero_cuenta VARCHAR(64) PRIMARY KEY,
    dui_cliente VARCHAR(20) NOT NULL,
    saldo DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    tipo_cuenta VARCHAR(40) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (dui_cliente) REFERENCES clientes(dui)
);

CREATE TABLE IF NOT EXISTS transacciones (
    id_transaccion VARCHAR(64) PRIMARY KEY,
    numero_cuenta VARCHAR(64) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    monto DECIMAL(18,2) NOT NULL CHECK (monto > 0),
    saldo_posterior DECIMAL(18,2) NOT NULL CHECK (saldo_posterior >= 0),
    descripcion VARCHAR(255),
    fecha TIMESTAMP NOT NULL,
    cuenta_origen VARCHAR(64),
    cuenta_destino VARCHAR(64),
    CONSTRAINT fk_transacciones_cuenta FOREIGN KEY (numero_cuenta) REFERENCES cuentas(numero_cuenta)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(40) NOT NULL,
    cliente_id VARCHAR(64),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
