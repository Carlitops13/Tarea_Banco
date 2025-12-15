-- Crear esquema para PostgreSQL: usuarios y cuentas
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS accounts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  balance NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_accounts_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);

-- Usuario de ejemplo (contraseña en texto plano solo para pruebas)
INSERT INTO users(username, password, active)
VALUES ('cliente123','clave123', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Crear cuenta inicial para el usuario de ejemplo si no tiene ya una
INSERT INTO accounts(user_id, balance)
SELECT u.id, 100.00
FROM users u
WHERE u.username = 'cliente123'
  AND NOT EXISTS (
    SELECT 1 FROM accounts a WHERE a.user_id = u.id
  );
