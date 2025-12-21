package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                try (InputStream fis = new FileInputStream("db.properties")) {
                    props.load(fis);
                } catch (IOException e) {
                    System.err.println("No se encontró db.properties en classpath ni en el directorio de trabajo. Usando valores por defecto.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer db.properties: " + e.getMessage());
        }

        url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/tarea_banco");
        user = props.getProperty("db.user", "postgres");
        password = props.getProperty("db.password", "postgres");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver org.postgresql.Driver no encontrado en el classpath. Asegúrate de incluir postgresql-x.x.x.jar en el classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
