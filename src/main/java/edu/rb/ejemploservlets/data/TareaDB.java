package edu.rb.ejemploservlets.data;

import edu.rb.ejemploservlets.db.DatabaseConnection;
import edu.rb.ejemploservlets.models.Tarea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        return null; // o lanzar una excepción personalizada
    }

    private Tarea mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String titulo = rs.getString("titulo");
        boolean completado = rs.getBoolean("completado");

        Tarea tarea = new Tarea(id, titulo, completado);
        return tarea;
    }
}
