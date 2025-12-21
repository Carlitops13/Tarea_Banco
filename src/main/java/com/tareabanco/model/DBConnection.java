package com.tareabanco.model;

import com.tareabanco.util.EncryptionUtil;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final Properties props = new Properties();
    private static final String url;
    private static final String user;
    private static final String password;

    // Reemplazo de printStackTrace con logging más robusto
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DBConnection.class.getName());

    static {
        try (InputStream is = DBConnection.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("✓ application.properties cargado correctamente");
            } else {
                System.err.println("⚠️  application.properties no encontrado en classpath, usando valores por defecto o variables de entorno");
            }
        } catch (IOException e) {
            System.err.println("Error cargando application.properties: " + e.getMessage());
        }

        // Primero intenta obtener de variables de entorno (Railway)
        String envDatabaseUrl = System.getenv("DATABASE_URL");
        String envUser = System.getenv("PGUSER");
        String envPassword = System.getenv("PGPASSWORD");
        String envPasswordEncrypted = System.getenv("DB_PASSWORD_ENCRYPTED");

        // Si existe DATABASE_URL de Railway, la convierte a JDBC format
        if (envDatabaseUrl != null && !envDatabaseUrl.isEmpty()) {
            url = envDatabaseUrl.startsWith("jdbc:")
                ? envDatabaseUrl
                : "jdbc:" + envDatabaseUrl;
            System.out.println("✓ DATABASE_URL de variables de entorno (Railway)");
        } else {
            url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/tarea_banco");
        }

        user = envUser != null && !envUser.isEmpty()
            ? envUser
            : props.getProperty("db.user", "postgres");

        // Intentar obtener contraseña encriptada primero (más seguro)
        if (envPasswordEncrypted != null && !envPasswordEncrypted.isEmpty()) {
            if (EncryptionUtil.isHashed(envPasswordEncrypted)) {
                password = envPasswordEncrypted;
                System.out.println("✓ Contraseña encriptada detectada y cargada desde DB_PASSWORD_ENCRYPTED");
            } else {
                System.err.println("⚠️  El valor de DB_PASSWORD_ENCRYPTED no es un hash válido, usando valor plano");
                password = envPassword != null && !envPassword.isEmpty()
                    ? envPassword
                    : props.getProperty("db.password", "postgres");
            }
        } else {
            // Usar contraseña plana si no está encriptada
            password = envPassword != null && !envPassword.isEmpty()
                ? envPassword
                : props.getProperty("db.password", "postgres");
        }

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📊 Configuración de Base de Datos");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("URL: " + url);
        System.out.println("Usuario: " + user);
        System.out.println("Contraseña: " + (password != null && !password.isEmpty() ? "***[PROTEGIDA]" : "vacía"));
        System.out.println("═══════════════════════════════════════════════════════\n");

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ Driver PostgreSQL cargado correctamente\n");
        } catch (ClassNotFoundException e) {
            LOGGER.severe(" ERROR: Driver org.postgresql.Driver no encontrado en el classpath. " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Método adicional para verificar la conexión
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Conexión a la base de datos exitosa");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.severe("✗ Error de conexión a la base de datos: " + e.getMessage());
        }
        return false;
    }
}
