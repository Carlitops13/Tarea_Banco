package com.tareabanco.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Scanner;

/**
 * Herramienta para generar clave maestra y encriptar contraseña de Railway
 * Uso: mvn exec:java -Dexec.mainClass="com.tareabanco.util.PasswordEncryptor"
 */
public class PasswordEncryptor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     HERRAMIENTA DE ENCRIPTACIÓN DE CONTRASEÑAS RAILWAY     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        System.out.println("¿Qué deseas hacer?");
        System.out.println("1. Generar nueva MASTER_KEY");
        System.out.println("2. Encriptar contraseña de PostgreSQL");
        System.out.println("3. Desencriptar contraseña");
        System.out.print("\nSelecciona opción (1-3): ");

        String option = scanner.nextLine().trim();

        switch (option) {
            case "1":
                generateMasterKey();
                break;
            case "2":
                encryptPassword(scanner);
                break;
            case "3":
                decryptPassword(scanner);
                break;
            default:
                System.out.println("❌ Opción inválida");
        }

        scanner.close();
    }

    /**
     * Genera una nueva MASTER_KEY de 256 bits
     */
    private static void generateMasterKey() {
        System.out.println("\n📝 Generando MASTER_KEY de 256 bits...\n");

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            System.out.println("✅ MASTER_KEY generada exitosamente:\n");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println(encodedKey);
            System.out.println("═══════════════════════════════════════════════════════════\n");

            System.out.println("📋 INSTRUCCIONES PARA RAILWAY:\n");
            System.out.println("1. Ve a tu proyecto en https://railway.app");
            System.out.println("2. Selecciona tu aplicación Java");
            System.out.println("3. Pestaña 'Variables' → Agregar nueva variable");
            System.out.println("4. Nombre: MASTER_KEY");
            System.out.println("5. Valor: " + encodedKey);
            System.out.println("6. Presiona 'Deploy'");
            System.out.println("\n✓ Copia y pega la clave arriba en Railway\n");

        } catch (Exception e) {
            System.err.println("❌ Error generando MASTER_KEY: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Encripta una contraseña de PostgreSQL
     */
    private static void encryptPassword(Scanner scanner) {
        System.out.println("\n🔐 Encriptación de Contraseña\n");
        System.out.print("Ingresa la contraseña a encriptar: ");
        String password = scanner.nextLine();

        String encrypted = EncryptionUtil.encrypt(password);


        System.out.println("\n✅ Contraseña encriptada:\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println(encrypted);
        System.out.println("═══════════════════════════════════════════════════════════\n");

        System.out.println("📋 INSTRUCCIONES:\n");
        System.out.println("1. Copia la contraseña encriptada arriba");
        System.out.println("2. Ve a Railway → Tu aplicación → Variables");
        System.out.println("3. Busca la variable 'DB_PASSWORD_ENCRYPTED'");
        System.out.println("4. Reemplaza su valor con la contraseña encriptada");
        System.out.println("5. Presiona 'Deploy'\n");

        System.out.println("O actualiza en tu archivo .env:");
        System.out.println("DB_PASSWORD_ENCRYPTED=" + encrypted + "\n");
    }

    /**
     * Desencripta una contraseña (para pruebas)
     */
    private static void decryptPassword(Scanner scanner) {
        System.out.println("\n🔓 Desencriptación de Contraseña (PRUEBAS SOLO)\n");
        System.out.print("Ingresa la contraseña encriptada (Base64): ");
        String encrypted = scanner.nextLine();

        String decrypted = EncryptionUtil.decrypt(encrypted);

        System.out.println("\n✅ Contraseña desencriptada:\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println(decrypted);
        System.out.println("═══════════════════════════════════════════════════════════\n");

        System.out.println("⚠️  NOTA: La contraseña desencriptada es: " + decrypted + "\n");
    }
}

