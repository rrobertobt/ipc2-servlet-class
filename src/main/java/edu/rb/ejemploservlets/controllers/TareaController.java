package edu.rb.ejemploservlets.controllers;

import com.google.gson.Gson;
import edu.rb.ejemploservlets.models.Tarea;
import edu.rb.ejemploservlets.services.TareaServicio;
import edu.rb.ejemploservlets.utils.DBException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

// Ahora vive bajo /api/* para que el AuthFilter lo proteja automáticamente
@WebServlet("/api/tareas/*")
public class TareaController extends HttpServlet {

    private final Gson gson = new Gson();
    private final TareaServicio tareaServicio = new TareaServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = (String) req.getAttribute("username");
        int id = obtenerIdDeLaRuta(req);

        try {
            if (id == -1) {
                // Devolver solo las tareas del usuario autenticado
                escribirJson(res, tareaServicio.obtenerTodas(username));
            } else {
                Tarea tarea = tareaServicio.obtenerPorId(id, username);

                if (tarea == null) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    escribirJson(res, Map.of("error", "Tarea con ID " + id + " no encontrada"));
                    return;
                }

                escribirJson(res, tarea);
            }
        } catch (DBException de) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirJson(res, Map.of("error", "Error de base de datos: " + de.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = (String) req.getAttribute("username");

        Tarea incoming = gson.fromJson(req.getReader(), Tarea.class);
        String titulo = (incoming != null) ? incoming.getTitulo() : null;

        try {
            Tarea creada = tareaServicio.crearTarea(titulo, username);
            res.setStatus(HttpServletResponse.SC_CREATED);
            escribirJson(res, creada);
        } catch (DBException de) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirJson(res, Map.of("error", "Error de base de datos: " + de.getMessage()));
        } catch (IllegalArgumentException iae) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "Datos inválidos: " + iae.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = (String) req.getAttribute("username");

        int id = obtenerIdDeLaRuta(req);
        if (id == -1) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "ID inválido en la URL"));
            return;
        }

        Tarea incoming = gson.fromJson(req.getReader(), Tarea.class);
        String titulo = (incoming != null) ? incoming.getTitulo() : null;
        Boolean completado = (incoming != null) ? incoming.isCompletada() : null;

        try {
            Tarea actualizada = tareaServicio.actualizarTarea(id, titulo, completado, username);

            if (actualizada == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                escribirJson(res, Map.of("error", "Tarea con ID " + id + " no encontrada"));
                return;
            }

            escribirJson(res, actualizada);
        } catch (DBException de) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirJson(res, Map.of("error", "Error de base de datos: " + de.getMessage()));
        } catch (IllegalArgumentException iae) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "Datos inválidos: " + iae.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = (String) req.getAttribute("username");

        int id = obtenerIdDeLaRuta(req);
        if (id == -1) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "ID inválido en la URL"));
            return;
        }

        try {
            boolean eliminada = tareaServicio.eliminarTarea(id, username);

            if (!eliminada) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                escribirJson(res, Map.of("error", "Tarea con ID " + id + " no encontrada"));
                return;
            }

            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (DBException de) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirJson(res, Map.of("error", "Error de base de datos: " + de.getMessage()));
        }
    }

    private void escribirJson(HttpServletResponse res, Object data) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(gson.toJson(data));
    }

    private int obtenerIdDeLaRuta(HttpServletRequest req) {
        String ruta = req.getPathInfo();

        if (ruta == null || ruta.equals("/")) return -1;

        try {
            return Integer.parseInt(ruta.substring(1));
        } catch (NumberFormatException ne) {
            return -1;
        }
    }
}
