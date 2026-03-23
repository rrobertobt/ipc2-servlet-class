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

                escribirJson(res, tarea);
            }
        } catch (DBException de) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            escribirJson(res, Map.of("error", "Tarea con ID " + id + " no encontrada"));
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
