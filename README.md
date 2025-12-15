# Tarea_Banco — Documentación para estudiantes

Este repositorio contiene una pequeña aplicación de escritorio en Java (Swing) que simula operaciones básicas de un banco: inicio de sesión, creación de cuentas y operaciones de depósito/retiro/transferencia. A continuación se documenta el propósito, los formularios principales, el flujo de datos y las instrucciones para ejecutar la aplicación en un equipo 

## Estructura relevante

- `src/Main.java` — punto de entrada. Inicializa la interfaz en el Event Dispatch Thread.
- `src/model/DBConnection.java` — clase centralizada para obtener conexiones a PostgreSQL. Lee variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` o `db.properties` como fallback.
- `src/forms/loginForm.java` — formulario de inicio de sesión.
- `src/forms/CreateAccountForm.java` — formulario para crear usuario + cuenta con saldo inicial.
- `src/forms/bancoForm.java` — formulario principal que muestra saldo y permite operaciones.
- `docker-init/init.sql` y `.idea/queries/schema.sql` — scripts SQL para crear las tablas `users` y `accounts` y datos de ejemplo.
- `db.properties` — archivo de configuración de conexión (opcional).
- `lib/postgresql-*.jar` — driver JDBC de PostgreSQL (si no se usa Maven).
- `run.ps1` / `run.bat` — scripts para compilar y ejecutar incluyendo el driver JDBC.

## Flujo de datos y conexión a la base de datos

1. `DBConnection.getConnection()` es el único punto responsable de crear `java.sql.Connection` a PostgreSQL. Los parámetros pueden venir de:
   - Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
   - Archivo `db.properties` en la raíz del proyecto.
   - Valores por defecto: `jdbc:postgresql://localhost:5432/tarea_banco`, usuario `postgres`, contraseña `postgres`.

2. `loginForm` realiza una consulta:
   - SQL: `SELECT id, password, active FROM users WHERE username = ?`.
   - Si el usuario existe y `active = true`, compara la contraseña (actualmente en texto plano para pruebas). Si coincide, abre `bancoForm`.
   - Si las credenciales fallan, incrementa un contador `failedAttempts` y bloquea (deshabilita el botón) tras 3 intentos.

3. `bancoForm` recibe (o determina) un `userId` y carga la primera cuenta asociada a ese usuario:
   - Si no existe cuenta, inserta una fila en `accounts` con `balance = 0`.
   - Muestra `lblSaldo` con el balance.
   - En depósitos/retiros/transferencias actualiza la variable `saldo` en memoria y llama a `UPDATE accounts SET balance = ? WHERE user_id = ?` para persistir el nuevo balance.

4. `CreateAccountForm` permite crear un nuevo usuario y una cuenta con saldo inicial:
   - Inserta el usuario (si no existe) con `INSERT ... ON CONFLICT DO NOTHING` y obtiene/recupera `userId`.
   - Inserta la cuenta asociada con el saldo inicial usando `INSERT ... SELECT ... WHERE NOT EXISTS` para no duplicar cuentas.

## Descripción detallada de cada formulario

### `loginForm` (src/forms/loginForm.java)
- Campos principales:
  - `textField1` — campo de usuario (JTextField).
  - `passwordField1` — campo de contraseña (JPasswordField).
  - `button1` — botón "Ingresar".
  - `CREARCUENTAButton` — botón "Crear cuenta" que abre `CreateAccountForm` y no cierra el login.
- Acciones:
  - Al pulsar "Ingresar": valida que los campos no estén vacíos, consulta la tabla `users` mediante `DBConnection` y comprueba `password` y `active`.
  - Maneja un contador de intentos erróneos y bloquea el login tras 3 intentos.
- Conexión a la lógica:
  - Si login es correcto: `dispose()` (cierra la ventana de login) y `new bancoForm()`.
  - Si fallo: muestra mensajes con `JOptionPane`.

### `CreateAccountForm` (src/forms/CreateAccountForm.java)
- Campos principales:
  - `txtUsername` — usuario a crear.
  - `txtPassword` — contraseña (en texto plano en pruebas).
  - `txtInitialBalance` — saldo inicial (texto convertido a número).
  - `btnCreate` — botón para crear usuario y cuenta.
- Acciones:
  - Valida inputs básicos (no vacíos, saldo numérico).
  - Crea usuario en `users` (si no existe) y crea la cuenta en `accounts` con el saldo inicial.
- Conexión a la lógica:
  - Usa `DBConnection.getConnection()` para las operaciones SQL.
  - Muestra resultados y cierra el formulario al finalizar.

### `bancoForm` (src/forms/bancoForm.java)
- Campos principales:
  - `lblSaldo` — etiqueta que muestra el saldo.
  - `btnDeposito`, `btnRetiro`, `btnTransferencia` — botones para operaciones.
  - `txtHistorial` — área de texto donde se registran transacciones en memoria.
- Acciones:
  - Al abrir: carga saldo desde `accounts` (primera cuenta del `user_id`) o crea una cuenta si no existe.
  - Depósito/Retiro/Transferencia: valida montos, actualiza `saldo`, actualiza la BD (`UPDATE accounts SET balance = ? WHERE user_id = ?`) y registra transacción en `txtHistorial`.
- Conexión a la lógica:
  - Todas las operaciones que cambian el saldo llaman a `actualizarSaldoEnBD()` para persistir el balance.

## Scripts SQL y datos de ejemplo

Puedes usar el script `docker-init/init.sql` o crear `schema.sql` con el siguiente contenido (PostgreSQL):

```sql
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

INSERT INTO users(username, password, active)
VALUES ('cliente123','clave123', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO accounts(user_id, balance)
SELECT u.id, 100.00
FROM users u
WHERE u.username = 'cliente123'
  AND NOT EXISTS (
    SELECT 1 FROM accounts a WHERE a.user_id = u.id
  );
```

## Ejecutar la aplicación (opciones)

### Opción A — Usar Docker (recomendado en equipos de la universidad si no quieres instalar Postgres)
1. Asegúrate de tener Docker y Docker Compose instalados.
2. Desde la raíz del proyecto, inicia la base de datos:

```bash
docker-compose up -d
```

3. Esto levantará un servicio Postgres en `localhost:5432` con usuario `postgres` y contraseña `postgres` y aplicará `docker-init/init.sql`.
4. Ejecuta la app (desde IDE o con los scripts):

PowerShell:
```powershell
.\run.ps1
```

CMD:
```
run.bat
```

### Opción B — Postgres instalado localmente
1. Crear la base de datos si no existe:

```powershell
createdb -U postgres tarea_banco
```

2. Ejecutar el script `schema.sql`:

```powershell
psql -U postgres -d tarea_banco -f ".\docker-init\init.sql"
```

3. Compilar/ejecutar la app con `run.bat` o desde tu IDE.

### Opción C — Conexión a un servidor remoto
- Ajusta `db.properties` o exporta las variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` para apuntar al servidor remoto.

## Configuración de conexión

- Archivo `db.properties` (opcional) en la raíz del proyecto. Ejemplo:

```
db.url=jdbc:postgresql://localhost:5432/tarea_banco
db.user=postgres
db.password=postgres
```

- También puedes exportar variables de entorno:

Linux / macOS:
```bash
export DB_URL=jdbc:postgresql://host:5432/tarea_banco
export DB_USER=miusuario
export DB_PASSWORD=miclave
```

Windows PowerShell:
```powershell
$env:DB_URL = 'jdbc:postgresql://host:5432/tarea_banco'
$env:DB_USER = 'miusuario'
$env:DB_PASSWORD = 'miclave'
```

## Credenciales de prueba

- Usuario: `cliente123`
- Contraseña: `clave123`
- Saldo inicial de ejemplo: `100.00`



