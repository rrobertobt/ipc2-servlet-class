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
}
