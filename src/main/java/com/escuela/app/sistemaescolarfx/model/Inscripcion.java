package com.escuela.app.sistemaescolarfx.model;

import java.time.LocalDate;

public class Inscripcion {
    private int idInscripcion;
    private int idPersona;
    private int idMateria;
    private String nombrePersona;
    private String nombreMateria;
    private LocalDate fechaInscripcion;

    public Inscripcion(int idInscripcion, int idPersona, int idMateria,
                       String nombrePersona, String nombreMateria, LocalDate fechaInscripcion) {
        this.idInscripcion = idInscripcion;
        this.idPersona = idPersona;
        this.idMateria = idMateria;
        this.nombrePersona = nombrePersona;
        this.nombreMateria = nombreMateria;
        this.fechaInscripcion = fechaInscripcion;
    }

    public int getIdInscripcion() { return idInscripcion; }
    public int getIdPersona() { return idPersona; }
    public int getIdMateria() { return idMateria; }
    public String getNombrePersona() { return nombrePersona; }
    public String getNombreMateria() { return nombreMateria; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
}
