package edu.rb.ejemploservlets.data;

import edu.rb.ejemploservlets.db.DatabaseConnection;
import edu.rb.ejemploservlets.models.Tarea;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TareaDB {
    public List<Tarea> getAll() throws SQLException {
        String sql = "SELECT id, titulo, completado FROM todos ORDER BY id DESC";
        List<Tarea> lista = new ArrayList<>();

        try (
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
                ){
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public Tarea getById(int id) throws SQLException {
        String sql = "SELECT id, titulo, completado FROM todos WHERE id = ?";

        try (
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
                ){
            ps.setInt(1, id);                    // reemplaza el primer "?" con el id

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;  // ID no encontrado
    }

    public Tarea create(String titulo) throws SQLException {
        String sql = "INSERT INTO todos (titulo, completado) VALUES (?, 0)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, titulo);
            ps.executeUpdate();

            // Obtener el ID que MySQL genero automaticamente
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int idGenerado = keys.getInt(1);
                    Tarea nueva = new Tarea(idGenerado, titulo);
                    return nueva;
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para la nueva tarea.");
    }

    public Tarea update(int id, String titulo, boolean completado) throws SQLException {
        String sql = "UPDATE todos SET titulo = ?, completado = ? WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, titulo);
            ps.setBoolean(2, completado);
            ps.setInt(3, id);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                // Retornar el objeto actualizado
                Tarea actualizado = new Tarea(id, titulo);
                actualizado.setCompletada(completado);
                return actualizado;
            }
        }
        return null;  // ID no encontrado
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM todos WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    private Tarea mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String titulo = rs.getString("titulo");
        boolean completado = rs.getBoolean("completado");

        Tarea tarea = new Tarea(id, titulo, completado);
        return tarea;
    }
}
