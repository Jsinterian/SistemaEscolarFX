package com.escuela.app.sistemaescolarfx.model;

import java.time.LocalDate;

public class Persona {
    private int idPersona;
    private String nombre;
    private String apellido;
    private String sexo;
    private LocalDate fechaNacimiento;
    private String rol;

    public Persona(int idPersona, String nombre, String apellido, String sexo, LocalDate fechaNacimiento, String rol) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
    }

    public int getIdPersona() { return idPersona; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getSexo() { return sexo; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getRol() { return rol; }
}
