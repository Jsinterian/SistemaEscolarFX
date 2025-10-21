package com.escuela.app.sistemaescolarfx.model;

import java.time.LocalDate;

public class Asistencia {
    private int idAsistencia;
    private int idInscripcion;
    private String alumno;
    private String materia;
    private LocalDate fechaAsistencia;
    private boolean presente;

    public Asistencia(int idAsistencia, int idInscripcion, String alumno, String materia,
                      LocalDate fechaAsistencia, boolean presente) {
        this.idAsistencia = idAsistencia;
        this.idInscripcion = idInscripcion;
        this.alumno = alumno;
        this.materia = materia;
        this.fechaAsistencia = fechaAsistencia;
        this.presente = presente;
    }

    public int getIdAsistencia() { return idAsistencia; }
    public int getIdInscripcion() { return idInscripcion; }
    public String getAlumno() { return alumno; }
    public String getMateria() { return materia; }
    public LocalDate getFechaAsistencia() { return fechaAsistencia; }
    public boolean isPresente() { return presente; }

    public void setPresente(boolean presente) { this.presente = presente; }
}
