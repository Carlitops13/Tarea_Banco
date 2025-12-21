package com.tareabanco;

import com.tareabanco.model.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utilidad para probar la conexión a la base de datos
 * Ejecuta: mvn exec:java -Dexec.mainClass="com.tareabanco.TestDBConnection"
 */
public class TestDBConnection {
    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("PRUEBA DE CONEXIÓN A BASE DE DATOS");
        System.out.println("=".repeat(50));
        System.out.println();

        if (!DBConnection.testConnection()) {
            System.err.println("\n✗ No se pudo conectar a la base de datos");
            System.exit(1);
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Probar si existen las tablas
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("\n Tablas en la base de datos:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("table_name"));
                }
            }

            // Contar usuarios
            String userCountSql = "SELECT COUNT(*) as count FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(userCountSql)) {
                if (rs.next()) {
                    System.out.println("\n Usuarios registrados: " + rs.getInt("count"));
                }
            }

            // Contar cuentas
            String accountCountSql = "SELECT COUNT(*) as count FROM accounts";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(accountCountSql)) {
                if (rs.next()) {
                    System.out.println("Cuentas registradas: " + rs.getInt("count"));
                }
            }

            // Mostrar saldo total
            String totalBalanceSql = "SELECT SUM(balance) as total FROM accounts";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalBalanceSql)) {
                if (rs.next()) {
                    Double total = rs.getDouble("total");
                    System.out.println(" Saldo total en el sistema: $" + String.format("%.2f", total != 0 ? total : 0.0));
                }
            }

            System.out.println("\n✓ ¡Conexión exitosa y base de datos funcional!");
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("\n✗ Error durante las pruebas:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}

