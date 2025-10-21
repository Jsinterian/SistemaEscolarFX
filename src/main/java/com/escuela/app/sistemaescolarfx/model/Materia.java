package com.escuela.app.sistemaescolarfx.model;

public class Materia {
    private int idMateria;
    private String nombreMateria;
    private String descripcion;

    public Materia(int idMateria, String nombreMateria, String descripcion) {
        this.idMateria = idMateria;
        this.nombreMateria = nombreMateria;
        this.descripcion = descripcion;
    }

    public int getIdMateria() { return idMateria; }
    public String getNombreMateria() { return nombreMateria; }
    public String getDescripcion() { return descripcion; }
}
