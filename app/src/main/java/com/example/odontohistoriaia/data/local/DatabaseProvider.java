package com.example.odontohistoriaia.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseProvider {

    // Versión 1 -> 2: se agrega remoteId sin borrar los datos que ya hay en el teléfono
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE pacientes ADD COLUMN remoteId INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static AppDatabase instancia;
    public static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public static AppDatabase getDatabase(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "odontohistoria_db"
            ).addMigrations(MIGRATION_1_2).build();
        }
        return instancia;
    }
}