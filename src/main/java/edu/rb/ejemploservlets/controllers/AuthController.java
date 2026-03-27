package edu.rb.ejemploservlets.controllers;

import com.google.gson.Gson;
import edu.rb.ejemploservlets.services.UsuarioServicio;
import edu.rb.ejemploservlets.utils.DBException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Endpoint de autenticación.
 * POST /api/auth/login  → recibe { "username": "...", "password": "..." }
 *                          responde { "token": "..." } o 401
 */
@WebServlet("/api/auth/login")
public class AuthController extends HttpServlet {

    private final Gson gson = new Gson();
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Leer las credenciales del body JSON
        LoginRequest body = gson.fromJson(req.getReader(), LoginRequest.class);

        if (body == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "Body requerido con username y password"));
            return;
        }

        try {
            String token = usuarioServicio.login(body.username, body.password);

            if (token == null) {
                // Credenciales incorrectas
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                escribirJson(res, Map.of("error", "Credenciales incorrectas"));
            } else {
                // Login exitoso: devolvemos el token
                escribirJson(res, Map.of("token", token));
            }

        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", e.getMessage()));
        } catch (DBException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirJson(res, Map.of("error", "Error interno del servidor"));
        }
    }

    private void escribirJson(HttpServletResponse res, Object data) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(gson.toJson(data));
    }

    // Clase interna para deserializar el body del login
    private static class LoginRequest {
        String username;
        String password;
    }
}
