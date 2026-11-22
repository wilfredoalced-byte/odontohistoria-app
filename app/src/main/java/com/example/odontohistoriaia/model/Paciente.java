package com.example.odontohistoriaia.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pacientes")
public class Paciente {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String motivoConsulta;
    public String proximaCita;

    // id del mismo paciente en el backend (0 = todavía no sincronizado)
    @ColumnInfo(defaultValue = "0")
    public int remoteId;

    public Paciente(String nombre, String motivoConsulta, String proximaCita) {
        this.nombre = nombre;
        this.motivoConsulta = motivoConsulta;
        this.proximaCita = proximaCita;
    }
}