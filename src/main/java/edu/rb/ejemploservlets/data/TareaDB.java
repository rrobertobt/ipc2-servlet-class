package edu.rb.ejemploservlets.data;

import edu.rb.ejemploservlets.db.DatabaseConnection;
import edu.rb.ejemploservlets.models.Tarea;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TareaDB {

    /**
     * Devuelve todas las tareas que pertenecen al usuario indicado.
     */
    public List<Tarea> getAllByUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT id, usuario_id, titulo, completado FROM todos WHERE usuario_id = ? ORDER BY id DESC";
        List<Tarea> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca una tarea por ID y verifica que pertenezca al usuario indicado.
     * Retorna null si no existe o si pertenece a otro usuario.
     */
    public Tarea getByIdAndUsuario(int id, int usuarioId) throws SQLException {
        String sql = "SELECT id, usuario_id, titulo, completado FROM todos WHERE id = ? AND usuario_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Crea una tarea asociada al usuario indicado.
     */
    public Tarea create(int usuarioId, String titulo) throws SQLException {
        String sql = "INSERT INTO todos (usuario_id, titulo, completado) VALUES (?, ?, 0)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, titulo);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int idGenerado = keys.getInt(1);
                    return new Tarea(idGenerado, usuarioId, titulo);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para la nueva tarea.");
    }

    /**
     * Actualiza una tarea solo si pertenece al usuario indicado.
     * Retorna la tarea actualizada, o null si no existe o pertenece a otro usuario.
     */
    public Tarea update(int id, int usuarioId, String titulo, boolean completado) throws SQLException {
        String sql = "UPDATE todos SET titulo = ?, completado = ? WHERE id = ? AND usuario_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, titulo);
            ps.setBoolean(2, completado);
            ps.setInt(3, id);
            ps.setInt(4, usuarioId);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                Tarea actualizada = new Tarea(id, usuarioId, titulo, completado);
                return actualizada;
            }
        }
        return null;
    }

    /**
     * Elimina una tarea solo si pertenece al usuario indicado.
     * Retorna true si se eliminó, false si no existía o pertenece a otro usuario.
     */
    public boolean delete(int id, int usuarioId) throws SQLException {
        String sql = "DELETE FROM todos WHERE id = ? AND usuario_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    private Tarea mapRow(ResultSet rs) throws SQLException {
        return new Tarea(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("titulo"),
                rs.getBoolean("completado")
        );
    }
}
