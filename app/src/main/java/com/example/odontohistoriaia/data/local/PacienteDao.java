package com.example.odontohistoriaia.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.odontohistoriaia.model.Paciente;
import java.util.List;

@Dao
public interface PacienteDao {

    @Insert
    void insertar(Paciente paciente);

    @Update
    void actualizar(Paciente paciente);

    @Delete
    void eliminar(Paciente paciente);

    @Query("SELECT * FROM pacientes LIMIT 1")
    Paciente obtenerPrimero();

    @Query("SELECT * FROM pacientes")
    List<Paciente> obtenerTodos();
}