package edu.rb.ejemploservlets.data;

import edu.rb.ejemploservlets.db.DatabaseConnection;
import edu.rb.ejemploservlets.models.Usuario;

import java.sql.*;

public class UsuarioDB {

    /**
     * Busca un usuario por username y contraseña (ya hasheada).
     * Retorna el Usuario si las credenciales coinciden, null si no.
     */
    public Usuario findByUsernameAndPassword(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id, username, password FROM usuarios WHERE username = ? AND password = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca un usuario solo por username (sin importar la contraseña).
     * Útil para verificar si el usuario existe.
     */
    public Usuario findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password FROM usuarios WHERE username = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password")
        );
    }
}
