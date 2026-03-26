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

// /tareas
// /tareas/123
@WebServlet("/tareas/*")
public class TareaController extends HttpServlet {
    // GET, POST, PUT, DELETE

    // Gson
    private final Gson gson = new Gson();

    // Servicio
    private final TareaServicio tareaServicio = new TareaServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // - Obtener ID de la URL (si existe)
        // - Devolver lista completa de tareas o una tarea en especifico
        // - Responder con JSON

        int id = obtenerIdDeLaRuta(req);

        try {
            if (id == -1) {
                // devolver lista completa EN FORMATO JSON
                escribirJson(res, tareaServicio.obtenerTodas());
            } else {
                // devolver tarea con ese ID EN FORMATO JSON
                Tarea tarea = tareaServicio.obtenerPorId(id);

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
        // - Leer el titulo del body (JSON)
        // - Crear la tarea
        // - Responder con la tarea creada en JSON

        Tarea incoming = gson.fromJson(req.getReader(), Tarea.class);
        String titulo = (incoming != null) ? incoming.getTitulo() : null;

        try {
            Tarea creada = tareaServicio.crearTarea(titulo);
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
        // - Leer ID de la URL
        // - Leer titulo y completado del body (JSON)
        // - Actualizar la tarea
        // - Responder con la tarea actualizada en JSON

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
            Tarea actualizada = tareaServicio.actualizarTarea(id, titulo, completado);

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
        // - Leer ID de la URL
        // - Eliminar la tarea
        // - Responder con 204 No Content

        int id = obtenerIdDeLaRuta(req);
        if (id == -1) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirJson(res, Map.of("error", "ID inválido en la URL"));
            return;
        }

        try {
            tareaServicio.eliminarTarea(id);
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
        String ruta = req.getPathInfo(); // /123, /cualquier-cosa, null

        if (ruta == null || ruta.equals("/")) return -1;

        try {
            return Integer.parseInt(ruta.substring(1)); // "123" -> 123 | "/123" -> "123"
        } catch (NumberFormatException ne) {
            return -1;
        }
    }
}
