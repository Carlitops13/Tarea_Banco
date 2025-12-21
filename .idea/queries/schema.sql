
-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL, -- en producción almacena passwords hasheadas (bcrypt, argon2, etc.)
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de cuentas
CREATE TABLE IF NOT EXISTS accounts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  balance NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_accounts_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índice para búsquedas por usuario
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);

-- Ejemplo de inserción de usuario (si ya existe, no hace nada)
INSERT INTO users(username, password, active)
VALUES ('cliente123','clave123', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Crear una cuenta inicial vinculada al usuario de ejemplo sólo si no existe ya una cuenta para ese usuario
INSERT INTO accounts(user_id, balance)
SELECT u.id, 100.00
FROM users u
WHERE u.username = 'cliente123'
  AND NOT EXISTS (
    SELECT 1 FROM accounts a WHERE a.user_id = u.id
  );

-- NOTA:
-- 1) Recomendado: no almacenar contraseñas en texto plano. Usa una librería para hashear la contraseña antes de INSERT (BCrypt, Argon2).
-- 2) Para establecer el saldo inicial desde la aplicación, realiza un INSERT en accounts con el user_id correspondiente o bien UPDATE si la cuenta ya existe.
-- 3) Este script está pensado para ejecutarse en PostgreSQL (psql, pgAdmin, etc.).
