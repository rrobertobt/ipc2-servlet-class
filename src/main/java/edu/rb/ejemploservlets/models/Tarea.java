package edu.rb.ejemploservlets.models;

public class Tarea {
    private int id;
    private int usuarioId;
    private String titulo;
    private boolean completada;

    public Tarea(int id, int usuarioId, String titulo, boolean completada) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.completada = completada;
    }

    public Tarea(int id, int usuarioId, String titulo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.completada = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
