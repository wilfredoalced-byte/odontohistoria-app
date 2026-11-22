package com.example.odontohistoriaia.model;

// Una pieza marcada del odontograma (numeración FDI). Las piezas sin registro se consideran sanas.
public class PiezaOdontograma {
    public int id;
    public int pacienteId;
    public int pieza;
    public String estado; // "Sano", "Caries" o "Tratado"
}
