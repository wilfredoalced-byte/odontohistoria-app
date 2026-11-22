package com.example.odontohistoriaia.model;

// Paciente tal como lo entrega y recibe el backend (GET/POST/PUT /pacientes)
public class PacienteRemoto {
    public int id;
    public String nombre;
    public String motivoConsulta;
    public String proximaCita;

    public PacienteRemoto() { }

    public PacienteRemoto(String nombre, String motivoConsulta, String proximaCita) {
        this.nombre = nombre;
        this.motivoConsulta = motivoConsulta;
        this.proximaCita = proximaCita;
    }
}
