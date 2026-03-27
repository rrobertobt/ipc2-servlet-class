package edu.rb.ejemploservlets.filters;

import edu.rb.ejemploservlets.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro de autenticación JWT.
 *
 * Intercepta todas las peticiones a /api/* y verifica que incluyan
 * un token JWT válido en el header Authorization.
 *
 * Excepciones (pasan sin verificar token):
 *   - Peticiones OPTIONS (preflight CORS): el navegador las envía antes de cada
 *     petición cross-origin para negociar los headers permitidos. No llevan token.
 *   - El endpoint de login (/api/auth/login): aún no hay token disponible.
 *
 * Flujo:
 *   1. Si es OPTIONS o login → dejar pasar sin verificar.
 *   2. Leer el header "Authorization" con formato "Bearer <token>".
 *   3. Extraer el token (sin el prefijo "Bearer ").
 *   4. Validar el token con JwtUtil.
 *   5. Si es válido → guardar el username en el request y continuar la cadena.
 *   6. Si es inválido o falta → responder 401 Unauthorized.
 */
@WebFilter(urlPatterns = "/api/*")
public class AuthFilter implements Filter {

    private static final String LOGIN_PATH = "/api/auth/login";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Paso 1a: las peticiones OPTIONS son preflight de CORS — no llevan token,
        // el CORSFilter ya se encargó de responderlas, solo dejamos pasar.
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Paso 1b: el login no requiere token, dejamos pasar sin verificar
        if (LOGIN_PATH.equals(req.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }

        // Paso 2: leemos el header Authorization
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No hay token o el formato es incorrecto
            enviarNoAutorizado(res, "Token no proporcionado.");
            return;
        }

        // Paso 3: extraemos el token quitando "Bearer " (7 caracteres)
        String token = authHeader.substring(7);

        try {
            // Paso 4 y 5: validamos el token y obtenemos el username
            String username = JwtUtil.validarYObtenerUsername(token);

            // Guardamos el username como atributo del request para que
            // los servlets puedan acceder a él con req.getAttribute("username")
            req.setAttribute("username", username);

            // El token es válido: continuamos con el siguiente filtro/servlet
            chain.doFilter(request, response);

        } catch (JwtException e) {
            // Paso 6: token inválido, expirado o con firma incorrecta
            enviarNoAutorizado(res, "Token invalido o expirado.");
        }
    }

    private void enviarNoAutorizado(HttpServletResponse res, String mensaje) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
