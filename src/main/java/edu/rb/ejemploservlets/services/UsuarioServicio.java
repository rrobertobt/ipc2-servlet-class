package edu.rb.ejemploservlets.services;

import edu.rb.ejemploservlets.data.UsuarioDB;
import edu.rb.ejemploservlets.models.Usuario;
import edu.rb.ejemploservlets.utils.DBException;
import edu.rb.ejemploservlets.utils.HashUtil;
import edu.rb.ejemploservlets.utils.JwtUtil;

import java.sql.SQLException;

public class UsuarioServicio {
    private final UsuarioDB usuarioDB = new UsuarioDB();

    /**
     * Valida las credenciales del usuario y, si son correctas, genera un JWT.
     *
     * @param username  nombre de usuario enviado en el login
     * @param password  contraseña en texto plano enviada en el login
     * @return          JWT si las credenciales son válidas, null si no
     */
    public String login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username y password son requeridos");
        }

        try {
            // Hasheamos la contraseña antes de compararla con la BD
            String passwordHash = HashUtil.sha256(password);
            Usuario usuario = usuarioDB.findByUsernameAndPassword(username, passwordHash);

            if (usuario == null) {
                return null; // credenciales incorrectas
            }

            // Las credenciales son válidas: generamos y devolvemos el JWT
            return JwtUtil.generarToken(usuario.getUsername());

        } catch (SQLException e) {
            throw new DBException("Error al validar credenciales", e);
        }
    }
}
