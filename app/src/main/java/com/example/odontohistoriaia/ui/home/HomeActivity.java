package com.example.odontohistoriaia.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.odontohistoriaia.R;
import com.example.odontohistoriaia.ui.odontograma.OdontogramaActivity;
import com.example.odontohistoriaia.ui.historial.HistorialActivity;
import com.example.odontohistoriaia.ui.login.MainActivity;
import com.example.odontohistoriaia.data.local.AppDatabase;
import com.example.odontohistoriaia.data.local.DatabaseProvider;
import com.example.odontohistoriaia.model.Paciente;
import com.example.odontohistoriaia.model.Cita;

public class HomeActivity extends AppCompatActivity {

    private AppDatabase db;
    private HomeViewModel viewModel;
    private Paciente pacienteActual;
    private TextView tvSaludo;
    private TextView tvProximaCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvSaludo = findViewById(R.id.tvSaludo);
        tvProximaCita = findViewById(R.id.tvProximaCita);
        TextView btnEditarCita = findViewById(R.id.btnEditarCita);

        db = DatabaseProvider.getDatabase(this);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getPaciente().observe(this, paciente -> {
            pacienteActual = paciente;
            if (paciente != null) {
                tvSaludo.setText(getString(R.string.home_saludo_default) + ", " + paciente.nombre.split(" ")[0]);
                tvProximaCita.setText(paciente.proximaCita);
            }
        });

        viewModel.getMensaje().observe(this, texto -> {
            if (texto != null) {
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
                viewModel.limpiarMensaje();
            }
        });

        viewModel.cargarPaciente(db);
        viewModel.sincronizarConServidor(db);

        btnEditarCita.setOnClickListener(v -> mostrarDialogoEditar());

        Button btnVerOdontograma = findViewById(R.id.btnVerOdontograma);
        Button btnVerHistorial = findViewById(R.id.btnVerHistorial);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnVerOdontograma.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OdontogramaActivity.class);
            intent.putExtra(OdontogramaActivity.EXTRA_PACIENTE_ID, idEnServidor());
            startActivity(intent);
        });

        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistorialActivity.class);
            intent.putExtra(HistorialActivity.EXTRA_PACIENTE_ID, idEnServidor());
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }

    // Si al abrir la app el servidor estaba apagado, reintenta al volver a esta pantalla
    @Override
    protected void onResume() {
        super.onResume();
        if (pacienteActual != null && pacienteActual.remoteId <= 0) {
            viewModel.sincronizarConServidor(db);
        }
    }

    // id del paciente en el backend (0 si todavía no se sincronizó)
    private int idEnServidor() {
        return pacienteActual != null ? pacienteActual.remoteId : 0;
    }

    private void mostrarDialogoEditar() {
        if (pacienteActual == null) return;

        EditText input = new EditText(this);
        input.setText(pacienteActual.proximaCita);

        new AlertDialog.Builder(this)
                .setTitle("Editar próxima cita")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoTexto = input.getText().toString().trim();
                    if (!nuevoTexto.isEmpty()) {
                        pacienteActual.proximaCita = nuevoTexto;
                        viewModel.actualizarPaciente(db, pacienteActual);
                        viewModel.actualizarPacienteEnServidor(pacienteActual);

                        Cita citaParaEnviar = new Cita();
                        citaParaEnviar.paciente = pacienteActual.nombre;
                        citaParaEnviar.fecha = "2026-09-20";
                        citaParaEnviar.hora = "10:00 am";
                        citaParaEnviar.odontologo = nuevoTexto;
                        viewModel.enviarCitaAlServidor(citaParaEnviar);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("Esto eliminará los datos guardados en este dispositivo. ¿Continuar?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        if (pacienteActual == null) return;
        viewModel.eliminarPaciente(db, pacienteActual, () -> runOnUiThread(() -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }));
    }
}