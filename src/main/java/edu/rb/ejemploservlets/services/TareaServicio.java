package edu.rb.ejemploservlets.services;

import edu.rb.ejemploservlets.data.TareaDB;
import edu.rb.ejemploservlets.models.Tarea;
import edu.rb.ejemploservlets.utils.DBException;

import java.sql.SQLException;
import java.util.List;

public class TareaServicio {
    private final TareaDB tareaDB = new TareaDB();

    public List<Tarea> obtenerTodas() {
        try {
            return tareaDB.getAll();
        } catch (SQLException e) {
            throw new DBException("Error al obtener tareas", e);
        }
    }

    public Tarea obtenerPorId(int id) {
        try {
            return tareaDB.getById(id);
        } catch (SQLException e) {
            throw new DBException("Error al obtener tarea por ID", e);
        }
    }

    public Tarea crearTarea(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        try {
            return tareaDB.create(titulo.trim());
        } catch (SQLException e) {
            throw new DBException("Error al crear tarea", e);
        }
    }

    public Tarea actualizarTarea(int id, String titulo, Boolean completado) {
        if (titulo != null && titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        try {
            return tareaDB.update(id, titulo != null ? titulo.trim() : null, completado);
        } catch (SQLException e) {
            throw new DBException("Error al actualizar tarea", e);
        }
    }

    public void eliminarTarea(int id) {
        try {
            tareaDB.delete(id);
        } catch (SQLException e) {
            throw new DBException("Error al eliminar tarea", e);
        }
    }
}
