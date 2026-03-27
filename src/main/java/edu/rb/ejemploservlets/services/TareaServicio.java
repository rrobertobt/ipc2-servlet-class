package edu.rb.ejemploservlets.services;

import edu.rb.ejemploservlets.data.TareaDB;
import edu.rb.ejemploservlets.data.UsuarioDB;
import edu.rb.ejemploservlets.models.Tarea;
import edu.rb.ejemploservlets.models.Usuario;
import edu.rb.ejemploservlets.utils.DBException;

import java.sql.SQLException;
import java.util.List;

public class TareaServicio {
    private final TareaDB tareaDB = new TareaDB();
    private final UsuarioDB usuarioDB = new UsuarioDB();

    /**
     * Resuelve el username a su ID en la base de datos.
     * Lanza excepción si el usuario no existe (no debería ocurrir si el token es válido).
     */
    private int resolverUsuarioId(String username) throws SQLException {
        Usuario usuario = usuarioDB.findByUsername(username);
        if (usuario == null) {
            throw new DBException("Usuario no encontrado: " + username, null);
        }
        return usuario.getId();
    }

    public List<Tarea> obtenerTodas(String username) {
        try {
            int usuarioId = resolverUsuarioId(username);
            return tareaDB.getAllByUsuario(usuarioId);
        } catch (SQLException e) {
            throw new DBException("Error al obtener tareas", e);
        }
    }

    public Tarea obtenerPorId(int id, String username) {
        try {
            int usuarioId = resolverUsuarioId(username);
            return tareaDB.getByIdAndUsuario(id, usuarioId);
        } catch (SQLException e) {
            throw new DBException("Error al obtener tarea por ID", e);
        }
    }

    public Tarea crearTarea(String titulo, String username) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        try {
            int usuarioId = resolverUsuarioId(username);
            return tareaDB.create(usuarioId, titulo.trim());
        } catch (SQLException e) {
            throw new DBException("Error al crear tarea", e);
        }
    }

    public Tarea actualizarTarea(int id, String titulo, Boolean completado, String username) {
        if (titulo != null && titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        try {
            int usuarioId = resolverUsuarioId(username);
            return tareaDB.update(id, usuarioId, titulo != null ? titulo.trim() : null, completado);
        } catch (SQLException e) {
            throw new DBException("Error al actualizar tarea", e);
        }
    }

    public boolean eliminarTarea(int id, String username) {
        try {
            int usuarioId = resolverUsuarioId(username);
            return tareaDB.delete(id, usuarioId);
        } catch (SQLException e) {
            throw new DBException("Error al eliminar tarea", e);
        }
    }
}
