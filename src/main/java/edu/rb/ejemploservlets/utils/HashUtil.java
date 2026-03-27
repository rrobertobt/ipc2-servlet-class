package edu.rb.ejemploservlets.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para hacer hash de contraseñas con SHA-256.
 * En producción se recomienda usar BCrypt o Argon2 ya que son
 * algoritmos diseñados específicamente para contraseñas (lentos a propósito).
 * SHA-256 se usa aquí por simplicidad didáctica.
 */
public class HashUtil {

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convertir el array de bytes a una cadena hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
