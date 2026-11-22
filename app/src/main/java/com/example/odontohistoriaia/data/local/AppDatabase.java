package com.example.odontohistoriaia.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.odontohistoriaia.model.Paciente;

@Database(entities = {Paciente.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PacienteDao pacienteDao();
}