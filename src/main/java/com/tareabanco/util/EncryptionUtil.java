package com.tareabanco.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Base64;

/**
 * Utilidad para encriptar y verificar contraseñas
 * Usa Bcrypt para hashing seguro
 */
public class EncryptionUtil {

    /**
     * Genera un hash seguro para una contraseña
     * @param plainText Contraseña en texto plano
     * @return Hash de la contraseña
     */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    /**
     * Verifica si una contraseña coincide con su hash
     * @param plainText Contraseña en texto plano
     * @param hashedPassword Hash de la contraseña
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verifyPassword(String plainText, String hashedPassword) {
        return BCrypt.checkpw(plainText, hashedPassword);
    }

    /**
     * Verifica si un texto dado parece ser un hash Bcrypt
     * @param text Texto a verificar
     * @return true si parece un hash Bcrypt, false en caso contrario
     */
    public static boolean isHashed(String text) {
        return text != null && text.startsWith("$2a$") && text.length() == 60;
    }

    /**
     * Encripta un texto plano usando codificación Base64
     * @param plainText Texto en texto plano
     * @return Texto encriptado
     */
    public static String encrypt(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    /**
     * Desencripta un texto codificado en Base64
     * @param encrypted Texto encriptado en Base64
     * @return Texto desencriptado
     */
    public static String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }
}
