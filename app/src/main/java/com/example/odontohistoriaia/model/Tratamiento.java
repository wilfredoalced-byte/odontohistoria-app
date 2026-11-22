package com.example.odontohistoriaia.model;

// Tratamiento del paciente (GET /tratamientos?pacienteId=...)
public class Tratamiento {
    public int id;
    public int pacienteId;
    public String nombre;
    public String fecha;
    public String estado; // "Pendiente", "En curso" o "Realizado"
}
